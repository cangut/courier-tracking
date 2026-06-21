package com.couriertracking.infrastructure.adapter;

import com.couriertracking.domain.port.out.CourierStoreEntryRepository;
import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.StoreName;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CourierStoreEntryRepositoryAdapter implements CourierStoreEntryRepository {

    private static final Duration WINDOW = Duration.ofSeconds(60);
    private final StringRedisTemplate redis;

    public CourierStoreEntryRepositoryAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }


    @Override
    public boolean isValidEntry(CourierId courierId, StoreName storeName) {
        String key = "entrance:" + courierId.value() + ":" + storeName.value();
        Boolean acquired = redis.opsForValue().setIfAbsent(key, "1", WINDOW);
        return Boolean.TRUE.equals(acquired);
    }
}
