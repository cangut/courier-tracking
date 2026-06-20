package com.couriertracking.domain.valueobject;

import java.time.Instant;

public record OccurredAt(Instant value) {

    public OccurredAt {
        if (value == null) {
            throw new IllegalArgumentException("occurredAt must not be null");
        }
    }

    public static OccurredAt of(Instant value) {
        return new OccurredAt(value);
    }
}
