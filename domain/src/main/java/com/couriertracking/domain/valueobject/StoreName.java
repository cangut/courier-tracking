package com.couriertracking.domain.valueobject;

public record StoreName(String value) {

    public StoreName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("storeName must not be blank");
        }
    }
}
