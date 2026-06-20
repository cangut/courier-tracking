package com.couriertracking.domain.valueobject;

public record Distance(double meters) {

    public Distance {
        if (Double.isNaN(meters) || meters < 0.0) {
            throw new IllegalArgumentException("distance meters must be >= 0: " + meters);
        }
    }

    public static final Distance ZERO = new Distance(0.0);

    public static Distance ofMeters(double meters) {
        return new Distance(meters);
    }

    public Distance plus(Distance other) {
        return new Distance(this.meters + other.meters);
    }

    public boolean isWithin(Distance radius) {
        return this.meters <= radius.meters;
    }
}
