package com.couriertracking.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntranceLogTest {

    private final CourierId courierId = new CourierId("courier-1");
    private final StoreName storeName = new StoreName("Ataşehir MMM Migros");
    private final GeoPoint location = new GeoPoint(40.9923307, 29.1244229);
    private final OccurredAt occurredAt = OccurredAt.of(Instant.parse("2026-06-20T10:15:30Z"));

    @Test
    void accepts_valid_fields() {
        EntranceLog log = new EntranceLog(courierId, storeName, location, occurredAt);
        assertThat(log.courierId()).isEqualTo(courierId);
        assertThat(log.storeName()).isEqualTo(storeName);
        assertThat(log.location()).isEqualTo(location);
        assertThat(log.occurredAt()).isEqualTo(occurredAt);
    }

    @Test
    void rejects_null_courier_id() {
        assertThatThrownBy(() -> new EntranceLog(null, storeName, location, occurredAt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_null_store_name() {
        assertThatThrownBy(() -> new EntranceLog(courierId, null, location, occurredAt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_null_location() {
        assertThatThrownBy(() -> new EntranceLog(courierId, storeName, null, occurredAt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_null_occurred_at() {
        assertThatThrownBy(() -> new EntranceLog(courierId, storeName, location, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equals_by_value() {
        EntranceLog first = new EntranceLog(courierId, storeName, location, occurredAt);
        EntranceLog second = new EntranceLog(courierId, storeName, location, occurredAt);
        assertThat(first).isEqualTo(second);
    }

}
