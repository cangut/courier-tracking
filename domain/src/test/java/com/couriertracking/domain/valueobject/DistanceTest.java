package com.couriertracking.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DistanceTest {

    @Test
    void accepts_zero_meters() {
        assertThat(new Distance(0.0).meters()).isEqualTo(0.0);
    }

    @Test
    void accepts_positive_meters() {
        assertThat(Distance.ofMeters(150.5).meters()).isEqualTo(150.5);
    }

    @Test
    void zero_constant_is_zero_meters() {
        assertThat(Distance.ZERO.meters()).isEqualTo(0.0);
    }

    @Test
    void rejects_negative_meters() {
        assertThatThrownBy(() -> new Distance(-0.1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_nan_meters() {
        assertThatThrownBy(() -> new Distance(Double.NaN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void plus_sums_meters() {
        Distance result = Distance.ofMeters(100.0).plus(Distance.ofMeters(50.0));
        assertThat(result.meters()).isEqualTo(150.0);
    }

    @Test
    void is_within_returns_true_when_below_radius() {
        assertThat(Distance.ofMeters(80.0).isWithin(Distance.ofMeters(100.0))).isTrue();
    }

    @Test
    void is_within_returns_true_on_exact_radius() {
        assertThat(Distance.ofMeters(100.0).isWithin(Distance.ofMeters(100.0))).isTrue();
    }

    @Test
    void is_within_returns_false_when_above_radius() {
        assertThat(Distance.ofMeters(120.0).isWithin(Distance.ofMeters(100.0))).isFalse();
    }

    @Test
    void equals_by_value() {
        assertThat(Distance.ofMeters(42.0)).isEqualTo(Distance.ofMeters(42.0));
    }

}
