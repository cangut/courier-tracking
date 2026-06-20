package com.couriertracking.domain.entity;

import com.couriertracking.domain.valueobject.GeoPoint;
import com.couriertracking.domain.valueobject.StoreName;

public record Store(StoreName name, GeoPoint location) {

    public Store {
        if (name == null) {
            throw new IllegalArgumentException("store name must not be null");
        }
        if (location == null) {
            throw new IllegalArgumentException("store location must not be null");
        }
    }
}
