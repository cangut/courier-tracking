package com.couriertracking.infrastructure.api.dto;

import com.couriertracking.domain.valueobject.EntranceLog;

import java.time.Instant;

public record EntranceResponse(String storeName, double lat, double lng, Instant occurredAt) {

    public static EntranceResponse from(EntranceLog log) {
        return new EntranceResponse(
                log.storeName().value(),
                log.location().latitude(),
                log.location().longitude(),
                log.occurredAt().value());
    }
}
