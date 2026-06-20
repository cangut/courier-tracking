package com.couriertracking.domain.aggregate;

import com.couriertracking.domain.service.DistanceCalculator;
import com.couriertracking.domain.service.HaversineDistanceCalculator;
import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.Distance;
import com.couriertracking.domain.valueobject.GeoPoint;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CourierTest {

    private final DistanceCalculator calc = new HaversineDistanceCalculator();

    @Test
    void first_location_request_contributes_zero_distance() {
        Courier courier = Courier.startingAt(CourierId.of("c1"));
        Distance increment = courier.moveTo(new GeoPoint(41.0, 29.0), calc);
        assertThat(increment.meters()).isZero();
        assertThat(courier.lastPosition()).contains(new GeoPoint(41.0, 29.0));
    }

    @Test
    void subsequent_location_request_returns_distance_from_previous_position() {
        Courier courier = new Courier(CourierId.of("c1"), new GeoPoint(41.0, 29.0));
        Distance increment = courier.moveTo(new GeoPoint(41.001, 29.0), calc);
        assertThat(increment.meters()).isCloseTo(111.19, org.assertj.core.api.Assertions.within(0.5));
        assertThat(courier.lastPosition()).contains(new GeoPoint(41.001, 29.0));
    }

}