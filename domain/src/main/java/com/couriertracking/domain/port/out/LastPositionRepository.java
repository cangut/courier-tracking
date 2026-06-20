package com.couriertracking.domain.port.out;

import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.GeoPoint;

import java.util.Optional;

public interface LastPositionRepository {
    Optional<GeoPoint> find(CourierId courierId);

    void save(CourierId courierId, GeoPoint position);
}
