package com.couriertracking.infrastructure.api.dto;

import java.time.Instant;

public record LocationRequest(String courierId, Double latitude, Double longitude, Instant occurredAt) {
}
