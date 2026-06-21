package com.couriertracking.infrastructure.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record LocationRequest(
        @Schema(description = "Identifies the courier", example = "courier-1")
        String courierId,

        @Schema(description = "Latitude in [-90, 90]", example = "40.9923307", requiredMode = Schema.RequiredMode.REQUIRED)
        Double latitude,

        @Schema(description = "Longitude in [-180, 180]", example = "29.1244229", requiredMode = Schema.RequiredMode.REQUIRED)
        Double longitude,

        @Schema(description = "ISO-8601 instant; optional, defaults to server time", example = "2026-06-20T10:15:30Z")
        Instant occurredAt) {
}
