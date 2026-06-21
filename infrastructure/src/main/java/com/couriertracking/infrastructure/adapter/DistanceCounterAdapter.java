package com.couriertracking.infrastructure.adapter;

import com.couriertracking.domain.port.out.DistanceCounter;
import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.Distance;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class DistanceCounterAdapter implements DistanceCounter {

    private final StringRedisTemplate redis;

    public DistanceCounterAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private static String key(CourierId courierId) {
        return "courier:" + courierId.value() + ":distance";
    }

    @Override
    public Distance increment(CourierId courierId, Distance delta) {
        Double total = redis.opsForValue().increment(key(courierId), delta.meters());
        return Distance.ofMeters(total == null ? 0.0 : total);
    }

    @Override
    public Distance total(CourierId courierId) {
        String raw = redis.opsForValue().get(key(courierId));
        return Distance.ofMeters(raw == null ? 0.0 : Double.parseDouble(raw));
    }
}
