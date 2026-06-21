package com.couriertracking.infrastructure.adapter;

import com.couriertracking.domain.entity.Store;
import com.couriertracking.domain.port.out.StoreRepository;
import com.couriertracking.domain.valueobject.GeoPoint;
import com.couriertracking.domain.valueobject.StoreName;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

@Component
public class StoreRepositoryAdapter implements StoreRepository {
    private static final String CACHE_KEY = "store-catalog";

    private final Cache<String, List<Store>> cache = Caffeine.newBuilder().build();

    public StoreRepositoryAdapter(@Value("${courier-tracking.stores-resource:classpath:stores.json}") Resource storesResource,
                                ObjectMapper objectMapper) {
        cache.put(CACHE_KEY, load(storesResource, objectMapper));
    }

    private static List<Store> load(Resource resource, ObjectMapper objectMapper) {
        try (InputStream in = resource.getInputStream()) {
            List<StoreJson> raw = objectMapper.readValue(in, new TypeReference<List<StoreJson>>() {});
            return raw.stream()
                    .map(s -> new Store(StoreName.of(s.name()), new GeoPoint(s.lat(), s.lng())))
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load store catalog from " + resource, e);
        }
    }

    @Override
    public Collection<Store> findAll() {
        return cache.getIfPresent(CACHE_KEY);
    }

    private record StoreJson(String name, double lat, double lng) {
    }
}
