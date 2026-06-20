package com.couriertracking.domain.valueobject;

public record EntranceLog(CourierId courierId, StoreName storeName, GeoPoint location, OccurredAt occurredAt) {

    public EntranceLog {
        if (courierId == null || storeName == null || location == null || occurredAt == null) {
            throw new IllegalArgumentException("EntranceLog fields must not be null");
        }
    }
}
