package com.couriertracking.infrastructure.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TotalDistanceResponse(
        @Schema(example = "courier-1") String courierId,
        @Schema(description = "Accumulated travel distance in metres", example = "1543.27") Double totalDistanceMeters) {
}
