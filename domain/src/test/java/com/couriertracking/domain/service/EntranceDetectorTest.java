package com.couriertracking.domain.service;


import com.couriertracking.domain.entity.Store;
import com.couriertracking.domain.valueobject.Distance;
import com.couriertracking.domain.valueobject.GeoPoint;
import com.couriertracking.domain.valueobject.StoreName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EntranceDetectorTest {

    private final GeoPoint courier = new GeoPoint(41.0, 29.0);

    private final Store atExactly100m = new Store(StoreName.of("Boundary"), new GeoPoint(41.0, 29.001));
    private final Store justOver100m = new Store(StoreName.of("JustOver"), new GeoPoint(41.0, 29.002));
    private final Store within = new Store(StoreName.of("Within"), new GeoPoint(41.0, 29.0001));

    @Test
    void should_detect_entrance_exactly_100m_and_return_stores() {
        DistanceCalculator exact = (from, to) -> Distance.ofMeters(100.0);
        EntranceDetector detector = new EntranceDetector(exact);

        List<Store> candidates = detector.detectEntrance(courier, List.of(atExactly100m));

        assertThat(candidates).containsExactly(atExactly100m);
    }

    @Test
    void store_just_over_100m_is_not_an_entrance() {
        DistanceCalculator justOver = (from, to) -> Distance.ofMeters(100.0001);
        EntranceDetector detector = new EntranceDetector(justOver);

        List<Store> candidates = detector.detectEntrance(courier, List.of(justOver100m));

        assertThat(candidates).isEmpty();
    }

    @Test
    void filters_stores_by_radius_with_real_haversine() {
        EntranceDetector detector = new EntranceDetector(new HaversineDistanceCalculator());

        List<Store> candidates = detector.detectEntrance(courier, List.of(within, justOver100m));

        assertThat(candidates).containsExactly(within);
    }
}