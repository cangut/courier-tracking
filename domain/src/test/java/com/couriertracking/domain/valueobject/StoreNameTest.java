package com.couriertracking.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoreNameTest {

    @Test
    void accepts_valid_value() {
        StoreName name = new StoreName("Ataşehir MMM Migros");
        assertThat(name.value()).isEqualTo("Ataşehir MMM Migros");
    }

    @Test
    void rejects_null_value() {
        assertThatThrownBy(() -> new StoreName(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_empty_value() {
        assertThatThrownBy(() -> new StoreName(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_blank_value() {
        assertThatThrownBy(() -> new StoreName("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equals_by_value() {
        assertThat(new StoreName("Novada")).isEqualTo(new StoreName("Novada"));
    }

}
