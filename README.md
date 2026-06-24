# Courier Tracking Service

A backend service that ingests **real‑time GPS location updates** from couriers, calculates how far each courier has travelled, and detects when a courier **enters the circumference of a Migros store**.

It is built with **Java 21 + Spring Boot 3.5**, organised as a **Hexagonal (Ports & Adapters) multi‑module Maven project**, and backed by **Redis** (hot state: the courier aggregate — total distance & last position — plus re‑entry de‑duplication) and **Cassandra** (durable store‑entrance history).

---

## TL;DR — for reviewers

Everything needed to run and test the service is here; the rest of the document is reference detail.

**Run the service** — app + Redis + Cassandra, app on `:8080`:

```bash
docker compose up -d --build
```

Then explore the live API at **http://localhost:8080/swagger-ui.html**.

### Unit tests

Fast, Docker‑free — domain, application, and REST layers. Need **JDK 21**.

```bash
mvn clean verify
```

| Layer | Focus |
|-------|-------|
| Domain | Value‑object validation, Haversine accuracy, 100 m store filtering, distance accumulation. |
| Application | Write‑path orchestration with all ports mocked (distance increment, save, entrance append, 60 s de‑dup branch). |
| Infrastructure | Store‑catalog loading, REST endpoints via MockMvc (202 / 400 / read APIs), error mapping. |

> ⚠️ If `mvn` fails with *"release version 21 not supported"*, your `JAVA_HOME` points at an older JDK — switch it to a JDK 21 install.

### Integration test (real Redis + Cassandra via Testcontainers)

`CourierTrackingIntegrationTest` boots the app against **real** Redis + Cassandra containers and asserts the full vertical: aggregate JSON in Redis, the `SET NX EX` de‑duplication lock, and the Cassandra entrance log. Opt‑in, so the default build stays Docker‑free.

```bash
mvn verify -Pit        # needs a running Docker daemon; first Cassandra boot ~30–60 s
```

### Load & stress testing (Locust)

Simulates many couriers emitting GPS pings (write‑heavy) plus reads, loitering near the seeded stores so entrance detection and de‑dup are exercised. Locust runs as a Compose service behind the `loadtest` profile — **no local Python needed**.

```bash
docker compose --profile loadtest up --build    # stack + Locust UI on :8089
```

Open **http://localhost:8089**, fill in the fields, and click **Start**:

| Field | Meaning |
|-------|---------|
| **Number of users** | Total simultaneous virtual couriers (peak concurrency). |
| **Ramp up** | How many users are added per second (e.g. 200 users + ramp 20 → peak in 10 s). |
| **Host** | Target under test — **leave blank** (already wired to `http://app:8080`). |
| **Runtime** *(Advanced)* | Test duration (e.g. `2m`, `30s`). Blank = run until you press **Stop**. |
| **Profile** *(Advanced)* | Optional label for this run in the report (e.g. `baseline`). Does not change behaviour. |

> ⚠️ **Leave the Host field blank** (or set it to exactly `http://app:8080`). `http://localhost:8080` fails with `ConnectionRefused(111)` — Locust runs inside a container, where `localhost` is the Locust container, not the app. Don't append a path either (the locustfile already does).

**Sample result (no run needed).** A full Locust HTML report from a **1000‑user / 200‑ramp‑up** run is checked in at [`loadtest/load-test-result-with-1000-user.html`](loadtest/load-test-result-with-1000-user-request-mixed.html). Just open the file in any browser — double‑click it. It shows per‑endpoint RPS, p50/p95/p99 latency, and failure counts for that run.

---

## Table of Contents

1. [What this service does](#what-this-service-does)
2. [Business rules](#business-rules)
3. [Architecture](#architecture)
4. [Technology stack](#technology-stack)
5. [Project structure](#project-structure)
6. [Prerequisites](#prerequisites)
7. [Quick start (Docker Compose)](#quick-start-docker-compose)
8. [Running locally (for development)](#running-locally-for-development)
9. [REST API reference](#rest-api-reference)
10. [OpenAPI specification](#openapi-specification)
11. [End-to-end scenario walkthrough](#end-to-end-scenario-walkthrough)
12. [How a location update is processed](#how-a-location-update-is-processed)
13. [Data model](#data-model)
14. [Troubleshooting](#troubleshooting)

---

## What this service does

Couriers move around the city and periodically report their GPS position. For every reported position the service:

1. **Accumulates the total distance** the courier has travelled (sum of the straight‑line distance between each consecutive pair of reported points).
2. **Detects store entrances** — whenever a courier comes within **100 metres** of one of the known Migros stores, that visit is recorded.

Two read APIs are exposed on top of this:

- **Total travelled distance** for a courier.
- **The list of store entrances** for a courier.

The known stores are a small, static catalog of five Istanbul Migros branches loaded from [`stores.json`](bootstrap/src/main/resources/stores.json).

---

## Business rules

| # | Rule | Where it lives |
|---|------|----------------|
| 1 | A store **entrance** is detected when the courier is within **100 m** of a store, measured with the **Haversine** great‑circle formula. | [`Courier.moveTo`](domain/src/main/java/com/couriertracking/domain/aggregate/Courier.java) (keeps the stores within `Courier.ENTRANCE_RADIUS`), [`HaversineDistanceCalculator`](domain/src/main/java/com/couriertracking/domain/service/HaversineDistanceCalculator.java) |
| 2 | **Re‑entry de‑duplication:** if a courier enters the *same* store again **within 60 seconds**, it is **not** logged a second time. Leaving and coming back after the window *is* a new entrance. | [`ReceiveCourierLocationService`](application/src/main/java/com/couriertracking/application/command/ReceiveCourierLocationService.java) (gates the entrance write on the lock) via [`StoreEntranceLockRepository`](domain/src/main/java/com/couriertracking/domain/port/out/StoreEntranceLockRepository.java) → [`StoreEntranceLockRepositoryAdapter`](infrastructure/src/main/java/com/couriertracking/infrastructure/adapter/StoreEntranceLockRepositoryAdapter.java) (Redis `SET key value NX EX 60`; the adapter owns the 60 s TTL) |
| 3 | **Total distance** is the running sum of Haversine distances between consecutive reported positions for a courier. The very first position adds **0 m** (there is no previous point to measure from). The total is accumulated **inside the `Courier` aggregate** and persisted with it. | [`Courier.moveTo`](domain/src/main/java/com/couriertracking/domain/aggregate/Courier.java), [`CourierRepositoryAdapter`](infrastructure/src/main/java/com/couriertracking/infrastructure/adapter/CourierRepositoryAdapter.java) |
| 4 | A single reported position may be near **several** stores at once — each qualifying store produces its own (de‑duplicated) entrance. | [`ReceiveCourierLocationService`](application/src/main/java/com/couriertracking/application/command/ReceiveCourierLocationService.java) |
| 5 | `latitude` and `longitude` are **required**; coordinates must be valid (`lat ∈ [-90, 90]`, `lng ∈ [-180, 180]`). The entrance timestamp is stamped by the server (`Instant.now()`), not supplied by the client. | [`CourierRestController`](infrastructure/src/main/java/com/couriertracking/infrastructure/api/CourierRestController.java), [`GeoPoint`](domain/src/main/java/com/couriertracking/domain/valueobject/GeoPoint.java) |

> The 100 m radius (`Courier.ENTRANCE_RADIUS`, in the domain) and the 60 s re‑entry window (`StoreEntranceLockRepositoryAdapter.TTL`, in the Redis adapter) are the two knobs that define entrance behaviour.

---

## Architecture

The project follows **Hexagonal Architecture (Ports & Adapters)** with the dependency rule pointing **inward**: the domain knows nothing about Spring, the web, Redis, or Cassandra. Outer layers depend on inner layers, never the reverse.

```mermaid
flowchart TB
    subgraph IN["Driving side (inbound)"]
        REST["CourierRestController<br/>(REST adapter)"]
    end

    subgraph CORE["Core (framework-free)"]
        APP["application<br/>Use-case services<br/>(command / query)"]
        DOM["domain<br/>Entities · Value objects<br/>Domain services · Ports"]
    end

    subgraph OUT["Driven side (outbound)"]
        REDIS_A["Redis adapters<br/>courier aggregate · re-entry"]
        CASS_A["Cassandra adapter<br/>entrance history"]
        STORE_A["Store catalog adapter<br/>(stores.json + Caffeine)"]
    end

    REST --> APP --> DOM
    APP -->|out ports| REDIS_A
    APP -->|out ports| CASS_A
    APP -->|out ports| STORE_A
```

**Module dependency direction:** `bootstrap → infrastructure → application → domain`

- **`domain`** — pure business model. Aggregate (`Courier` — accumulates total distance and keeps the stores within the 100 m entrance radius) and entity (`Store`), value objects (`GeoPoint`, `Distance`, `CourierId`, …), domain services (`HaversineDistanceCalculator` behind the `DistanceCalculator` port), and the **ports** (`port.in` use‑case interfaces, `port.out` repository/gateway interfaces). No framework imports.
- **`application`** — orchestrates use cases. Split into **commands** (write side: `ReceiveCourierLocationService`) and **queries** (read side: `GetTotalTravelDistanceService`, `GetStoreEntrancesService`). Depends only on `domain`.
- **`infrastructure`** — the adapters that implement the ports: REST controller + DTOs, Redis adapters, the Cassandra repository, and the `stores.json` loader. Depends on `application`.
- **`bootstrap`** — the Spring Boot application entry point and wiring (`UseCaseConfiguration` builds the framework‑free use‑case beans; `CassandraConfig` sets up the keyspace/schema).

**Read/write separation (Command Query Seperation):** the command path appends each entrance straight to Cassandra through the `StoreEntranceLogRepository` port; the read API ([`GetStoreEntrancesService`](application/src/main/java/com/couriertracking/application/query/GetStoreEntrancesService.java)) queries the same `store_entrances` table.

---

## Technology stack

| Concern | Choice |
|---------|--------|
| Language / runtime | **Java 21** |
| Framework | **Spring Boot 3.5.15** (Web, Data Cassandra, Data Redis) |
| Build | **Maven** (multi‑module reactor) |
| Hot state store | **Redis 7** — courier aggregate (distance + last position), re‑entry window |
| Durable store | **Apache Cassandra 4.1** — store‑entrance history |
| In‑memory cache | **Caffeine** — caches the static store catalog |
| Testing | **JUnit 5**, **Mockito**, **AssertJ**, **Spring MockMvc** |
| Packaging / run | **Docker** + **Docker Compose** |

---

## Project structure

```
courier-tracking/
├── pom.xml                      # Parent reactor POM (Spring Boot parent, modules, dependency mgmt)
├── docker-compose.yml           # cassandra + redis + app
├── Dockerfile                   # multi-stage build of the boot jar
│
├── domain/                      # Pure business core (no framework)
│   └── src/main/java/com/couriertracking/domain/
│       ├── aggregate/Courier.java
│       ├── entity/Store.java
│       ├── valueobject/         # GeoPoint, Distance, CourierId, StoreName, OccurredAt, EntranceLog
│       ├── service/             # DistanceCalculator, HaversineDistanceCalculator
│       └── port/
│           ├── in/              # Use-case interfaces + ReceiveCourierLocationCommand
│           └── out/             # StoreRepository, CourierRepository,
│                                #   StoreEntranceLockRepository, StoreEntranceLogRepository
│
├── application/                 # Use-case orchestration (command/query)
│   └── src/main/java/com/couriertracking/application/
│       ├── command/ReceiveCourierLocationService.java
│       └── query/   GetTotalTravelDistanceService.java, GetStoreEntrancesService.java
│
├── infrastructure/              # Adapters that implement the ports
│   └── src/main/java/com/couriertracking/infrastructure/
│       ├── api/                 # CourierRestController, GlobalExceptionHandler, dto/
│       ├── adapter/             # Redis + store-catalog + entrance-log adapters
│       └── persistence/cassandra/  # EntranceLogEntity, EntranceLogCassandraRepository
│
└── bootstrap/                   # Spring Boot entry point + wiring
    └── src/main/
        ├── java/.../CourierTrackingApplication.java
        ├── java/.../config/     # UseCaseConfiguration, CassandraConfig
        └── resources/           # application.yml, stores.json
```

---

## Prerequisites

To **run everything in containers** (recommended), you only need:

- **Docker** and **Docker Compose**

To **build or develop locally**, you also need:

- **JDK 21** — the project targets Java 21 and will not compile on older JDKs.
  Make sure `JAVA_HOME` points at a 21 JDK:
  ```bash
  java -version        # should report 21.x
  echo $JAVA_HOME      # should point to a JDK 21 install
  ```
- **Maven 3.9+** (the project has no Maven wrapper, so a system Maven is required for local builds).
- A running **Redis** and **Cassandra** (the Compose file can provide these — see below).

---

## Quick start (Docker Compose)

This is the simplest way to get a fully working stack (app + Redis + Cassandra) with no local Java/Maven setup. The app image is built from source inside Docker.

```bash
# From the project root
docker compose up --build
```

What happens:

- **Cassandra** starts on `9042` and **Redis** on `6379`, each with a health check.
- The **app** waits until both are healthy, then starts on **http://localhost:8080**.
- On first boot the app **auto‑creates** the `courier_tracking` keyspace and the `store_entrances` table.

> ⏳ Cassandra can take **30–60 seconds** to become healthy on first start. The app deliberately waits for it via `depends_on: condition: service_healthy`.

Verify it is up:

```bash
curl -i http://localhost:8080/api/couriers/courier-1/total-distance
# => 200 OK  {"courierId":"courier-1","totalDistanceMeters":0.0}
```

Tear everything down (and wipe Cassandra data):

```bash
docker compose down -v
```

---

## Running locally (for development)

Run the **infrastructure** in containers but the **app** from your IDE/Maven — handy for fast iteration.

**1. Start only Redis and Cassandra:**

```bash
docker compose up -d cassandra redis
```

**2. Build the whole project and run the tests:**

```bash
mvn clean verify
```

**3. Run the application:**

```bash
mvn -pl bootstrap spring-boot:run
```

or run the packaged jar:

```bash
mvn clean package
java -jar bootstrap/target/courier-tracking.jar
```

The app connects to Redis/Cassandra on `127.0.0.1` by default (overridable via the `CASSANDRA_CONTACT_POINTS` / `REDIS_HOST` env vars, as `docker-compose.yml` does for the container network), so no extra environment variables are needed when the containers expose the default ports.

---

## REST API reference

Base URL: `http://localhost:8080`

### 1. Report a courier location

```
POST /api/couriers/location
Content-Type: application/json
```

**Request body**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `courierId` | string | yes | Identifies the courier. |
| `latitude` | number | **yes** | `[-90, 90]`. |
| `longitude` | number | **yes** | `[-180, 180]`. |

> The entrance timestamp is **not** part of the request — the server stamps each location with its current time.

**Responses**

- `202 Accepted` — accepted for processing (no body).
- `400 Bad Request` — missing/invalid coordinates, e.g.:
  ```json
  { "error": "bad_request", "message": "latitude and longitude are required" }
  ```

**Example**

```bash
curl -i -X POST http://localhost:8080/api/couriers/location \
  -H "Content-Type: application/json" \
  -d '{"courierId":"courier-1","latitude":40.9923307,"longitude":29.1244229}'
```

### 2. Get total travel distance

```
GET /api/couriers/{courierId}/total-distance
```

**Response** `200 OK`

```json
{ "courierId": "courier-1", "totalDistanceMeters": 1543.27 }
```

An unknown courier returns `0.0` (no error).

```bash
curl http://localhost:8080/api/couriers/courier-1/total-distance
```

### 3. Get store entrances

```
GET /api/couriers/{courierId}/entrances
```

Returns the courier's entrances from the **last 7 days** only (most recent first). The window bounds the Cassandra partition scan so the endpoint stays fast as a courier's history grows.

**Response** `200 OK` — array, most recent first:

```json
[
  {
    "storeName": "Ataşehir MMM Migros",
    "lat": 40.9923307,
    "lng": 29.1244229,
    "occurredAt": "2026-06-20T10:15:30Z"
  }
]
```

An unknown courier returns `[]`.

```bash
curl http://localhost:8080/api/couriers/courier-1/entrances
```

---

## OpenAPI specification

The running application serves **interactive API documentation** generated by [springdoc-openapi](https://springdoc.org/) — no extra setup needed once the app is up:

| Resource | URL |
|----------|-----|
| **Swagger UI** (browse & execute requests) | http://localhost:8080/swagger-ui.html |
| **OpenAPI 3 spec** (JSON) | http://localhost:8080/v3/api-docs |

> 💡 Reviewers: start the stack (`docker compose up --build`), open **http://localhost:8080/swagger-ui.html**, and use **“Try it out”** on each endpoint to call the live service.

A static **OpenAPI 3.1** copy of the same contract is committed at [`openapi.yaml`](openapi.yaml), so it can be read without running the app — open it in the [Swagger Editor](https://editor.swagger.io) or generate a client with `openapi-generator`.

> The live `/v3/api-docs` output is the source of truth; `openapi.yaml` is a hand-maintained copy for offline reading.

---

## End-to-end scenario walkthrough

This sequence exercises every business rule against the seeded stores. It assumes the stack is running (`docker compose up --build`).

The store **"Ataşehir MMM Migros"** is at `40.9923307, 29.1244229`.

> Each location is timestamped by the server at the moment it is received, so the de‑duplication window below plays out in **real wall‑clock time** — run the steps in quick succession.

**Step 1 — First location, exactly at the store.** Adds 0 m distance (no previous point) and records one entrance.

```bash
curl -X POST http://localhost:8080/api/couriers/location \
  -H "Content-Type: application/json" \
  -d '{"courierId":"courier-1","latitude":40.9923307,"longitude":29.1244229}'
```

**Step 2 — Move ~50 m away, still within 100 m, within 60 s.** Run this **right after** Step 1. Distance grows, but the entrance is **de‑duplicated** (Rule 2) → still only one entrance.

```bash
curl -X POST http://localhost:8080/api/couriers/location \
  -H "Content-Type: application/json" \
  -d '{"courierId":"courier-1","latitude":40.9927800,"longitude":29.1244229}'
```

**Step 3 — Move far away (no store nearby).** Distance grows; no new entrance.

```bash
curl -X POST http://localhost:8080/api/couriers/location \
  -H "Content-Type: application/json" \
  -d '{"courierId":"courier-1","latitude":41.0500000,"longitude":29.0200000}'
```

**Step 4 — Check the results.**

```bash
curl http://localhost:8080/api/couriers/courier-1/total-distance   # > 0 metres
curl http://localhost:8080/api/couriers/courier-1/entrances        # exactly ONE Ataşehir entrance
```

**Other scenarios to try**

- **Re‑entry after the window:** wait **more than 60 s** (the Redis de‑dup key expires) and repeat Step 1 → a **second** entrance is recorded.
- **Missing coordinates:** `POST` a body without `latitude` → `400 Bad Request` with the error envelope.
- **Multiple couriers:** use a different `courierId`; state (distance, entrances) is fully isolated per courier.

---

## How a location update is processed

```mermaid
sequenceDiagram
    participant C as Client
    participant R as CourierRestController
    participant S as ReceiveCourierLocationService
    participant CR as Redis: courier aggregate
    participant RE as Redis: entrance lock (NX EX 60)
    participant CS as Cassandra: store_entrances

    C->>R: POST /api/couriers/location
    R->>S: receive(command)
    S->>CR: find(courierId)
    S->>S: courier.addStores(catalog)
    S->>S: courier.moveTo(new) — sums distance, keeps stores within 100 m
    S->>CR: save(courier)
    loop each store within 100 m
        S->>RE: registerIfAbsent(courier, store)  (SET NX EX 60)
        alt first entry within window
            S->>CS: append entrance log
        else duplicate within 60 s
            Note over S,RE: skipped (no log)
        end
    end
    R-->>C: 202 Accepted
```

---

## Data model

### Redis (hot state, key per courier)

| Key pattern | Type | Written by | Meaning |
|-------------|------|-----------|---------|
| `courier:{id}` | string (JSON) | `CourierRepositoryAdapter` | The whole courier aggregate, serialized as one value: running total distance in metres plus the last reported position. |
| `entrance:{id}:{storeName}` | string, **TTL 60 s** | `StoreEntranceLockRepositoryAdapter` | Re‑entry de‑dup marker (`SET NX EX 60`; the adapter owns the 60 s TTL). |

### Cassandra (durable history)

Table `store_entrances` in keyspace `courier_tracking` (auto‑created on startup):

| Column | Kind | Notes |
|--------|------|-------|
| `courier_id` | partition key | All of a courier's entrances live together. |
| `occurred_at` | clustering (DESC) | Newest first — drives the API ordering. |
| `store_name` | clustering (ASC) | Tie‑break within the same instant. |
| `latitude`, `longitude` | columns | Where the entrance happened. |

---

## Troubleshooting

| Symptom | Cause / fix |
|---------|-------------|
| `mvn` fails: *release version 21 not supported* | `JAVA_HOME` points to an older JDK. Point it at a **JDK 21** install. |
| App exits at startup with a Cassandra connection error | Cassandra not ready yet. With Compose it waits for health; running standalone, start Cassandra first and retry. First boot can take 30–60 s. |
| `total-distance` always returns `0.0` | No locations posted for that `courierId`, or Redis is unreachable. Check `REDIS_HOST`/`REDIS_PORT`. |
| Entrances list is empty after posting near a store | You may be **>100 m** away, or hitting the **60 s** de‑dup window, or Cassandra is unreachable. Verify coordinates and check app logs. |
| Port `8080`/`9042`/`6379` already in use | Stop the conflicting process or change the published ports in `docker-compose.yml`. |
