package com.couriertracking.infrastructure.adapter;

import com.couriertracking.domain.port.out.LastPositionRepository;
import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.GeoPoint;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LastPositionRepositoryAdapter implements LastPositionRepository {

    private final StringRedisTemplate redis;

    public LastPositionRepositoryAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private static String key(CourierId courierId) {
        return "courier:" + courierId.value() + ":last-position";
    }


    @Override
    public Optional<GeoPoint> find(CourierId courierId) {
        String rawValue = redis.opsForValue().get(key(courierId));
        if (rawValue == null || rawValue.isBlank()) {
            return Optional.empty();
        }
        int sep = rawValue.indexOf(':');
        double latitude = Double.parseDouble(rawValue.substring(0, sep));
        double longitude = Double.parseDouble(rawValue.substring(sep + 1));
        return Optional.of(new GeoPoint(latitude, longitude));
    }

    @Override
    public void save(CourierId courierId, GeoPoint position) {
        redis.opsForValue().set(key(courierId), position.latitude() + ":" + position.longitude());
    }
}
