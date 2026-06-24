package com.couriertracking.bootstrap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CourierTrackingIntegrationTest {

    @Container
    @SuppressWarnings({"rawtypes", "resource"})
    static final CassandraContainer CASSANDRA = new CassandraContainer(DockerImageName.parse("cassandra:4.1"));

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    @DynamicPropertySource
    static void connectionProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cassandra.contact-points", CASSANDRA::getHost);
        registry.add("spring.cassandra.port", () -> CASSANDRA.getMappedPort(9042));
        registry.add("spring.cassandra.local-datacenter", CASSANDRA::getLocalDatacenter);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @Autowired
    private TestRestTemplate rest;

    @Test
    void persists_distance_in_redis_and_entrance_in_cassandra_with_de_dup() {
        String courier = "it-courier-1";
        double storeLat = 40.9923307;
        double storeLng = 29.1244229;

        assertThat(postLocation(courier, storeLat, storeLng).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(totalDistance(courier)).isZero();
        assertThat(entranceCount(courier)).isEqualTo(1);

        assertThat(postLocation(courier, 40.9927800, storeLng).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(entranceCount(courier)).isEqualTo(1);
        assertThat(totalDistance(courier)).isGreaterThan(0.0);
    }

    @Test
    void unknown_courier_reads_are_empty() {
        assertThat(totalDistance("it-unknown")).isZero();
        assertThat(entranceCount("it-unknown")).isZero();
    }

    private ResponseEntity<Void> postLocation(String courierId, double lat, double lng) {
        return rest.postForEntity(
                "/api/couriers/location",
                Map.of("courierId", courierId, "latitude", lat, "longitude", lng),
                Void.class);
    }

    @SuppressWarnings("unchecked")
    private double totalDistance(String courierId) {
        Map<String, Object> body = rest.getForObject(
                "/api/couriers/" + courierId + "/total-distance", Map.class);
        return ((Number) body.get("totalDistanceMeters")).doubleValue();
    }

    @SuppressWarnings("unchecked")
    private int entranceCount(String courierId) {
        List<Object> body = rest.getForObject("/api/couriers/" + courierId + "/entrances", List.class);
        return body.size();
    }
}
