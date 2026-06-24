"""
Load test for the Courier Tracking Service.

Simulates many couriers emitting GPS pings. By default this drives the WRITE path only:
the read tasks are disabled (weight 0), so every request is a POST /location and
per-courier throughput equals the write rate -> total = N / 3 req/s. Couriers loiter near
the seeded Migros stores so the run actually exercises entrance detection AND the 60 s
re-entry de-duplication, not just empty writes. (Re-enable the reads by giving the two
GET tasks a non-zero weight — see the note above them.)

Two knobs, both set in this file (the Locust UI only exposes users (-u), spawn rate (-r),
runtime (-t) and host — it cannot change either of these):

  * Cadence — FIXED at one request every 3 s per courier via `constant_pacing(3)`.
  * Worst/Average/Best case — expressed as spatial CONTENTION (how often a courier sits
    inside a store's 100 m radius), not cadence, because this system's cost is entrance
    detection + Cassandra writes (partitioned per courier). Edit HOVER_PROBABILITY below
    (default 0.5 = average).

Run:
    pip install locust
    # start the app first:  docker compose up -d --build
    locust -f loadtest/locustfile.py --host http://localhost:8080
    # then open http://localhost:8089 and set users / spawn rate / runtime

Headless example (100 users, 20/s spawn, 2 minutes):
    locust -f loadtest/locustfile.py --host http://localhost:8080 \
           --headless -u 100 -r 20 -t 2m --html loadtest/report-N100.html
"""

import random

from locust import HttpUser, constant_pacing, task

# Seeded stores — bootstrap/src/main/resources/stores.json
STORES = [
    (40.9923307, 29.1244229),  # Ataşehir MMM Migros
    (40.9861060, 29.1161293),  # Novada MMM Migros
    (41.0066851, 28.6552262),  # Beylikdüzü 5M Migros
    (41.0557830, 29.0210292),  # Ortaköy MMM Migros
    (40.9632463, 29.0630908),  # Caddebostan MMM Migros
]

_METERS_PER_DEGREE = 111_111.0

# Probability a given location ping lands inside a store's 100 m radius — i.e. how much
# entrance-detection + Cassandra-write work the run generates. This is the worst/avg/best knob:
#   0.1 = best-case    (couriers mostly drift in open space -> few entrances, light write load)
#   0.5 = average-case (realistic mix -> the default)
#   1.0 = worst-case   (couriers always hover next to a store -> max entrance + de-dup + writes)
HOVER_PROBABILITY = 0.5


def jitter(coord: float, meters: float = 80.0) -> float:
    """Nudge a coordinate by up to +/- `meters` (rough degrees near Istanbul)."""
    return coord + random.uniform(-meters, meters) / _METERS_PER_DEGREE


class CourierUser(HttpUser):
    # Fixed GPS reporting cadence: one request every 3 s, regardless of server latency.
    # constant_pacing (not `between`) keeps the offered load steady even as the service
    # slows under load — an open-loop-style probe that avoids coordinated omission.
    wait_time = constant_pacing(3.0)

    def on_start(self) -> None:
        self.courier_id = f"load-{random.randint(0, 1_000_000)}"
        self.lat, self.lng = random.choice(STORES)

    @task(10)
    def report_location(self) -> None:
        # Hover near a store (triggers entrance + de-dup) with HOVER_PROBABILITY;
        # otherwise drift through open space.
        if random.random() < HOVER_PROBABILITY:
            base_lat, base_lng = random.choice(STORES)
            self.lat, self.lng = jitter(base_lat), jitter(base_lng)
        else:
            self.lat, self.lng = jitter(self.lat, 200), jitter(self.lng, 200)

        self.client.post(
            "/api/couriers/location",
            json={"courierId": self.courier_id, "latitude": self.lat, "longitude": self.lng},
            name="POST /location",
        )

    # Read tasks are DISABLED by default (weight 0) so every request is a location write
    # -> total throughput == write throughput == N / 3 req/s. To also exercise the read
    # paths, bump these weights back to e.g. 2 and 1 (a 10:2:1 write:read mix).
    @task(0)
    def get_total_distance(self) -> None:
        self.client.get(
            f"/api/couriers/{self.courier_id}/total-distance",
            name="GET /total-distance",
        )

    @task(0)
    def get_entrances(self) -> None:
        self.client.get(
            f"/api/couriers/{self.courier_id}/entrances",
            name="GET /entrances",
        )
