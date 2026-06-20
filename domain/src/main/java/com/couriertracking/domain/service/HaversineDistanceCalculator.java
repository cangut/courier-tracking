package com.couriertracking.domain.service;

import com.couriertracking.domain.valueobject.Distance;
import com.couriertracking.domain.valueobject.GeoPoint;

public class HaversineDistanceCalculator implements DistanceCalculator {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    @Override
    public Distance distance(GeoPoint from, GeoPoint to) {
        double lat1 = Math.toRadians(from.latitude());
        double lat2 = Math.toRadians(to.latitude());
        double dLat = Math.toRadians(to.latitude() - from.latitude());
        double dLng = Math.toRadians(to.longitude() - from.longitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Distance.ofMeters(EARTH_RADIUS_METERS * c);
    }
}
