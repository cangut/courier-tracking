package com.couriertracking.domain.event;

import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.GeoPoint;
import com.couriertracking.domain.valueobject.OccurredAt;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocationEventTest {

    private final CourierId courierId = new CourierId("courier-1");
    private final GeoPoint location = new GeoPoint(40.9923307, 29.1244229);
    private final OccurredAt occurredAt = OccurredAt.of(Instant.parse("2026-06-20T10:15:30Z"));

    @Test
    void accepts_valid_fields() {
        LocationEvent event = new LocationEvent(courierId, location, occurredAt);
        assertThat(event.courierId()).isEqualTo(courierId);
        assertThat(event.location()).isEqualTo(location);
        assertThat(event.occurredAt()).isEqualTo(occurredAt);
    }

    @Test
    void rejects_null_courier_id() {
        assertThatThrownBy(() -> new LocationEvent(null, location, occurredAt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_null_location() {
        assertThatThrownBy(() -> new LocationEvent(courierId, null, occurredAt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_null_occurred_at() {
        assertThatThrownBy(() -> new LocationEvent(courierId, location, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equals_by_value() {
        LocationEvent first = new LocationEvent(courierId, location, occurredAt);
        LocationEvent second = new LocationEvent(courierId, location, occurredAt);
        assertThat(first).isEqualTo(second);
    }

}
