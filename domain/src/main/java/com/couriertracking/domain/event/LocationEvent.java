package com.couriertracking.domain.event;

import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.GeoPoint;
import com.couriertracking.domain.valueobject.OccurredAt;

public record LocationEvent(CourierId courierId, GeoPoint location, OccurredAt occurredAt) {

    public LocationEvent {
        if (courierId == null || location == null || occurredAt == null) {
            throw new IllegalArgumentException("LocationEvent fields must not be null");
        }
    }
}
