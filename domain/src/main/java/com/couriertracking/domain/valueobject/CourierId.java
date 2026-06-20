package com.couriertracking.domain.valueobject;

public record CourierId(String value) {

    public CourierId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("courierId must not be blank");
        }
    }
}
