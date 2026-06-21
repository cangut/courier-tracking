package com.couriertracking.infrastructure.adapter;

import com.couriertracking.domain.aggregate.Courier;
import com.couriertracking.domain.port.out.CourierRepository;
import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.Distance;
import com.couriertracking.domain.valueobject.GeoPoint;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class CourierRepositoryAdapter implements CourierRepository {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public CourierRepositoryAdapter(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    private static String key(CourierId courierId) {
        return "courier:" + courierId.value();
    }

    @Override
    public Optional<Courier> find(CourierId courierId) {
        String json = redis.opsForValue().get(key(courierId));
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(read(json).toAggregate());
    }

    @Override
    public void save(Courier courier) {
        redis.opsForValue().set(key(courier.id()), write(CourierDocument.from(courier)));
    }

    private CourierDocument read(String json) {
        try {
            return objectMapper.readValue(json, CourierDocument.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize courier from Redis: " + json, e);
        }
    }

    private String write(CourierDocument document) {
        try {
            return objectMapper.writeValueAsString(document);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize courier " + document.courierId(), e);
        }
    }

    record CourierDocument(String courierId, Double latitude, Double longitude, double totalDistanceMeters) {

        static CourierDocument from(Courier courier) {
            Double latitude = courier.lastPosition().map(GeoPoint::latitude).orElse(null);
            Double longitude = courier.lastPosition().map(GeoPoint::longitude).orElse(null);
            return new CourierDocument(
                    courier.id().value(),
                    latitude,
                    longitude,
                    courier.totalDistance().meters());
        }

        Courier toAggregate() {
            GeoPoint lastPosition = (latitude == null || longitude == null)
                    ? null
                    : new GeoPoint(latitude, longitude);
            return new Courier(
                    CourierId.of(courierId),
                    lastPosition,
                    Distance.ofMeters(totalDistanceMeters));
        }
    }
}
