package com.couriertracking.domain.aggregate;

import com.couriertracking.domain.entity.Store;
import com.couriertracking.domain.service.DistanceCalculator;
import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.Distance;
import com.couriertracking.domain.valueobject.GeoPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class Courier {
    public static final Distance ENTRANCE_RADIUS = Distance.ofMeters(100.0);

    private final CourierId id;
    private GeoPoint lastPosition;
    private Distance totalDistance;
    private List<Store> stores = List.of();

    public Courier(CourierId id, GeoPoint lastPosition, Distance totalDistance) {
        if (id == null) {
            throw new IllegalArgumentException("courier id must not be null");
        }
        this.id = id;
        this.lastPosition = lastPosition;
        this.totalDistance = (totalDistance == null) ? Distance.ZERO : totalDistance;
    }

    public static Courier startingAt(CourierId id) {
        return new Courier(id, null, Distance.ZERO);
    }

    public CourierId id() {
        return id;
    }

    public Optional<GeoPoint> lastPosition() {
        return Optional.ofNullable(lastPosition);
    }

    public Distance totalDistance() {
        return totalDistance;
    }

    public List<Store> getStores() {
        return this.stores;
    }

    public void moveTo(GeoPoint newPosition, DistanceCalculator distanceCalculator) {
        if (newPosition == null) {
            throw new IllegalArgumentException("new position must not be null");
        }

        Distance increment = (lastPosition == null)
                ? Distance.ZERO
                : distanceCalculator.distance(lastPosition, newPosition);

        this.lastPosition = newPosition;
        this.totalDistance = this.totalDistance.plus(increment);

        this.stores = stores.stream()
                .filter(store -> distanceCalculator.distance(lastPosition, store.location()).isWithin(ENTRANCE_RADIUS))
                .toList();
    }

    public void addStores(Collection<Store> stores) {
        this.stores = new ArrayList<>(stores);
    }
}
