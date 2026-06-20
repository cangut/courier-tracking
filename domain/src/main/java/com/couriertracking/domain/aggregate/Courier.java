package com.couriertracking.domain.aggregate;

import com.couriertracking.domain.service.DistanceCalculator;
import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.Distance;
import com.couriertracking.domain.valueobject.GeoPoint;

import java.util.Optional;

public final class Courier {

    private final CourierId id;
    private GeoPoint lastPosition;

    public Courier(CourierId id, GeoPoint lastPosition) {
        if (id == null) {
            throw new IllegalArgumentException("courier id must not be null");
        }
        this.id = id;
        this.lastPosition = lastPosition;
    }

    public static Courier startingAt(CourierId id) {
        return new Courier(id, null);
    }

    public Optional<GeoPoint> lastPosition() {
        return Optional.ofNullable(lastPosition);
    }

    public Distance moveTo(GeoPoint newPosition, DistanceCalculator calculator) {
        if (newPosition == null) {
            throw new IllegalArgumentException("new position must not be null");
        }
        Distance increment = (lastPosition == null)
                ? Distance.ZERO
                : calculator.distance(lastPosition, newPosition);
        this.lastPosition = newPosition;
        return increment;
    }
}
