package com.couriertracking.domain.valueobject;

public record CourierId(String value) {

    public CourierId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("courierId must not be blank");
        }
    }

    public static CourierId of(String value) {
        return new CourierId(value);
    }
}
