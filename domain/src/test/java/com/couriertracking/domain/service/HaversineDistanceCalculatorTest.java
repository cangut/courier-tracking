package com.couriertracking.domain.service;


import com.couriertracking.domain.valueobject.Distance;
import com.couriertracking.domain.valueobject.GeoPoint;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class HaversineDistanceCalculatorTest {

    private final DistanceCalculator calculator = new HaversineDistanceCalculator();

    @Test
    void distance_to_same_point_is_zero() {
        GeoPoint p = new GeoPoint(40.99, 29.12);
        assertThat(calculator.distance(p, p).meters()).isZero();
    }

    @Test
    void one_degree_of_latitude_is_about_111_2_km() {
        Distance d = calculator.distance(new GeoPoint(0, 0), new GeoPoint(1, 0));
        assertThat(d.meters()).isCloseTo(111_194.93, within(1.0));
    }

    @Test
    void is_symmetric() {
        GeoPoint a = new GeoPoint(40.9923307, 29.1244229);
        GeoPoint b = new GeoPoint(40.986106, 29.1161293);
        assertThat(calculator.distance(a, b).meters())
                .isCloseTo(calculator.distance(b, a).meters(), within(1e-9));
    }

    @Test
    void short_known_distance_within_tolerance() {
        Distance d = calculator.distance(new GeoPoint(41.0, 29.0), new GeoPoint(41.001, 29.0));
        assertThat(d.meters()).isCloseTo(111.19, within(0.5));
    }
}