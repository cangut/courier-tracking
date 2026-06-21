package com.couriertracking.infrastructure.api.dto;

import com.couriertracking.domain.valueobject.EntranceLog;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record EntranceResponse(
        @Schema(example = "Ataşehir MMM Migros") String storeName,
        @Schema(example = "40.9923307") double lat,
        @Schema(example = "29.1244229") double lng,
        @Schema(example = "2026-06-20T10:15:30Z") Instant occurredAt) {

    public static EntranceResponse from(EntranceLog log) {
        return new EntranceResponse(
                log.storeName().value(),
                log.location().latitude(),
                log.location().longitude(),
                log.occurredAt().value());
    }
}
