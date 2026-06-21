package com.couriertracking.domain.aggregate;

import com.couriertracking.domain.entity.Store;
import com.couriertracking.domain.service.DistanceCalculator;
import com.couriertracking.domain.service.HaversineDistanceCalculator;
import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.Distance;
import com.couriertracking.domain.valueobject.GeoPoint;
import com.couriertracking.domain.valueobject.StoreName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class CourierTest {

    private final DistanceCalculator calc = new HaversineDistanceCalculator();

    @Test
    void first_location_request_contributes_zero_distance() {
        Courier courier = Courier.startingAt(CourierId.of("c1"));
        courier.moveTo(new GeoPoint(41.0, 29.0), calc);
        assertThat(courier.totalDistance().meters()).isZero();
        assertThat(courier.lastPosition()).contains(new GeoPoint(41.0, 29.0));
    }

    @Test
    void subsequent_location_request_adds_distance_from_previous_position() {
        Courier courier = new Courier(CourierId.of("c1"), new GeoPoint(41.0, 29.0), Distance.ZERO);
        courier.moveTo(new GeoPoint(41.001, 29.0), calc);
        assertThat(courier.totalDistance().meters()).isCloseTo(111.19, within(0.5));
        assertThat(courier.lastPosition()).contains(new GeoPoint(41.001, 29.0));
    }

    @Test
    void total_distance_accumulates_across_moves() {
        Courier courier = new Courier(CourierId.of("c1"), new GeoPoint(41.0, 29.0), Distance.ofMeters(1000.0));
        courier.moveTo(new GeoPoint(41.001, 29.0), calc);
        assertThat(courier.totalDistance().meters()).isCloseTo(1111.19, within(0.5));
    }

    @Test
    void first_move_keeps_total_distance_unchanged() {
        Courier courier = Courier.startingAt(CourierId.of("c1"));
        courier.moveTo(new GeoPoint(41.0, 29.0), calc);
        assertThat(courier.totalDistance()).isEqualTo(Distance.ZERO);
    }

    @Test
    void keeps_only_stores_within_entrance_radius_after_moving() {
        Courier courier = Courier.startingAt(CourierId.of("c1"));
        Store near = new Store(StoreName.of("Near"), new GeoPoint(41.0, 29.0));
        Store far = new Store(StoreName.of("Far"), new GeoPoint(42.0, 30.0));
        courier.addStores(List.of(near, far));

        courier.moveTo(new GeoPoint(41.0, 29.0), calc);

        assertThat(courier.getStores()).containsExactly(near);
    }

    @Test
    void reports_not_within_any_store_when_all_are_out_of_radius() {
        Courier courier = Courier.startingAt(CourierId.of("c1"));
        courier.addStores(List.of(new Store(StoreName.of("Far"), new GeoPoint(42.0, 30.0))));

        courier.moveTo(new GeoPoint(41.0, 29.0), calc);

        assertThat(courier.getStores()).isEmpty();
    }
}
