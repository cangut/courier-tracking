package com.couriertracking.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourierIdTest {

    @Test
    void accepts_valid_value() {
        CourierId id = new CourierId("courier-1");
        assertThat(id.value()).isEqualTo("courier-1");
    }

    @Test
    void rejects_null_value() {
        assertThatThrownBy(() -> new CourierId(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_empty_value() {
        assertThatThrownBy(() -> new CourierId(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_blank_value() {
        assertThatThrownBy(() -> new CourierId("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equals_by_value() {
        assertThat(new CourierId("c-1")).isEqualTo(new CourierId("c-1"));
    }

}
