package com.couriertracking.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OccurredAtTest {

    @Test
    void accepts_valid_instant() {
        Instant now = Instant.parse("2026-06-20T10:15:30Z");
        OccurredAt occurredAt = new OccurredAt(now);
        assertThat(occurredAt.value()).isEqualTo(now);
    }

    @Test
    void factory_creates_same_value() {
        Instant now = Instant.parse("2026-06-20T10:15:30Z");
        assertThat(OccurredAt.of(now)).isEqualTo(new OccurredAt(now));
    }

    @Test
    void rejects_null_value_in_constructor() {
        assertThatThrownBy(() -> new OccurredAt(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_null_value_in_factory() {
        assertThatThrownBy(() -> OccurredAt.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equals_by_value() {
        Instant now = Instant.parse("2026-06-20T10:15:30Z");
        assertThat(OccurredAt.of(now)).isEqualTo(OccurredAt.of(now));
    }

}
