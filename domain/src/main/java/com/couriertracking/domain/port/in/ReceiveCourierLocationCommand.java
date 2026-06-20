package com.couriertracking.domain.port.in;

import java.time.Instant;

public record ReceiveCourierLocationCommand(String courierId, double lat, double lng, Instant occurredAt) {
}
