package com.couriertracking.domain.event;

import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.GeoPoint;
import com.couriertracking.domain.valueobject.OccurredAt;
import com.couriertracking.domain.valueobject.StoreName;

public record StoreEntranceDetected(
        CourierId courierId,
        StoreName storeName,
        GeoPoint location,
        OccurredAt occurredAt) {

    public StoreEntranceDetected {
        if (courierId == null || storeName == null || location == null || occurredAt == null) {
            throw new IllegalArgumentException("StoreEntranceDetected fields must not be null");
        }
    }
}
