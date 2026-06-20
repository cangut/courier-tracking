package com.couriertracking.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeoPointTest {

    @Test
    void accepts_valid_coordinates() {
        GeoPoint p = new GeoPoint(40.9923307, 29.1244229);
        assertThat(p.latitude()).isEqualTo(40.9923307);
        assertThat(p.longitude()).isEqualTo(29.1244229);
    }

    @Test
    void accepts_exact_bounds() {
        assertThat(new GeoPoint(90.0, 180.0)).isNotNull();
        assertThat(new GeoPoint(-90.0, -180.0)).isNotNull();
    }

    @Test
    void rejects_latitude_out_of_range() {
        assertThatThrownBy(() -> new GeoPoint(90.0001, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GeoPoint(-91, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_longitude_out_of_range() {
        assertThatThrownBy(() -> new GeoPoint(0, 180.0001))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GeoPoint(0, -181))
                .isInstanceOf(IllegalArgumentException.class);
    }

}