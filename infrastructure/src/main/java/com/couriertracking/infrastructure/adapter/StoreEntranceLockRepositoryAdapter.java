package com.couriertracking.infrastructure.adapter;

import com.couriertracking.domain.port.out.StoreEntranceLockRepository;
import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.StoreName;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class StoreEntranceLockRepositoryAdapter implements StoreEntranceLockRepository {

    public static final Duration TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redis;

    public StoreEntranceLockRepositoryAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private static String key(CourierId courierId, StoreName storeName) {
        return "entrance:" + courierId.value() + ":" + storeName.value();
    }

    @Override
    public boolean registerIfAbsent(CourierId courierId, StoreName storeName) {
        Boolean acquired = redis.opsForValue().setIfAbsent(key(courierId, storeName), "1", TTL);
        return Boolean.TRUE.equals(acquired);
    }
}
