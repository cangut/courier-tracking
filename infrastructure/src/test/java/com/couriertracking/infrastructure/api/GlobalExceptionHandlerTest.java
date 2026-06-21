package com.couriertracking.infrastructure.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void maps_illegal_argument_to_bad_request_body() {
        ResponseEntity<Map<String, String>> response =
                handler.onIllegalArgument(new IllegalArgumentException("latitude and longitude are required"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "bad_request")
                .containsEntry("message", "latitude and longitude are required");
    }
}
