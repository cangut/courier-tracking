package com.couriertracking.domain.event;

import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.GeoPoint;
import com.couriertracking.domain.valueobject.OccurredAt;
import com.couriertracking.domain.valueobject.StoreName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoreEntranceDetectedTest {

    private final CourierId courierId = new CourierId("courier-1");
    private final StoreName storeName = new StoreName("Ataşehir MMM Migros");
    private final GeoPoint location = new GeoPoint(40.9923307, 29.1244229);
    private final OccurredAt occurredAt = OccurredAt.of(Instant.parse("2026-06-20T10:15:30Z"));

    @Test
    void accepts_valid_fields() {
        StoreEntranceDetected event = new StoreEntranceDetected(courierId, storeName, location, occurredAt);
        assertThat(event.courierId()).isEqualTo(courierId);
        assertThat(event.storeName()).isEqualTo(storeName);
        assertThat(event.location()).isEqualTo(location);
        assertThat(event.occurredAt()).isEqualTo(occurredAt);
    }

    @Test
    void rejects_null_courier_id() {
        assertThatThrownBy(() -> new StoreEntranceDetected(null, storeName, location, occurredAt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_null_store_name() {
        assertThatThrownBy(() -> new StoreEntranceDetected(courierId, null, location, occurredAt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_null_location() {
        assertThatThrownBy(() -> new StoreEntranceDetected(courierId, storeName, null, occurredAt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_null_occurred_at() {
        assertThatThrownBy(() -> new StoreEntranceDetected(courierId, storeName, location, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equals_by_value() {
        StoreEntranceDetected first = new StoreEntranceDetected(courierId, storeName, location, occurredAt);
        StoreEntranceDetected second = new StoreEntranceDetected(courierId, storeName, location, occurredAt);
        assertThat(first).isEqualTo(second);
    }

}
