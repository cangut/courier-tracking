package com.couriertracking.domain.service;

import com.couriertracking.domain.entity.Store;
import com.couriertracking.domain.valueobject.Distance;
import com.couriertracking.domain.valueobject.GeoPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntranceDetector {
    public static final Distance ENTRANCE_RADIUS = Distance.ofMeters(100.0);

    private final DistanceCalculator distanceCalculator;

    public EntranceDetector(DistanceCalculator distanceCalculator) {
        if (distanceCalculator == null) {
            throw new IllegalArgumentException("distanceCalculator must not be null");
        }
        this.distanceCalculator = distanceCalculator;
    }

    public List<Store> detectEntrance(GeoPoint courierPosition, Collection<Store> catalog) {
        List<Store> candidates = new ArrayList<>();
        for (Store store : catalog) {
            Distance distance = distanceCalculator.distance(courierPosition, store.location());
            if (distance.isWithin(ENTRANCE_RADIUS)) {
                candidates.add(store);
            }
        }
        return candidates;
    }
}
