package com.couriertracking.infrastructure.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI courierTrackingOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Courier Tracking Service API")
                .version("1.0.0")
                .description("Ingests courier GPS locations, accumulates travelled distance, "
                        + "and records store entrances when a courier comes within 100 m of a Migros store.")
                .license(new License().name("MIT")));
    }
}
