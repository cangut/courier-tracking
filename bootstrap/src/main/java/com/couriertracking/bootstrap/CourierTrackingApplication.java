package com.couriertracking.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.couriertracking")
public class CourierTrackingApplication {
    public static void main(String[] args) {
        SpringApplication.run(CourierTrackingApplication.class, args);
    }
}