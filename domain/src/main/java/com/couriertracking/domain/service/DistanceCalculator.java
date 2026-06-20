package com.couriertracking.domain.service;

import com.couriertracking.domain.valueobject.Distance;
import com.couriertracking.domain.valueobject.GeoPoint;

public interface DistanceCalculator {
    Distance distance(GeoPoint from, GeoPoint to);
}
