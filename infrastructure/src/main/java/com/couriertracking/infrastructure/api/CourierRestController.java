package com.couriertracking.infrastructure.api;

import com.couriertracking.domain.port.in.GetStoreEntrancesUseCase;
import com.couriertracking.domain.port.in.GetTotalTravelDistanceUseCase;
import com.couriertracking.domain.port.in.ReceiveCourierLocationCommand;
import com.couriertracking.domain.port.in.ReceiveCourierLocationUseCase;
import com.couriertracking.infrastructure.api.dto.EntranceResponse;
import com.couriertracking.infrastructure.api.dto.LocationRequest;
import com.couriertracking.infrastructure.api.dto.TotalDistanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/couriers")
@Tag(name = "Couriers", description = "Report courier locations and query travel distance and store entrances.")
public class CourierRestController {

    private final ReceiveCourierLocationUseCase receiveCourierLocationUseCase;
    private final GetTotalTravelDistanceUseCase getTotalTravelDistanceUseCase;
    private final GetStoreEntrancesUseCase getStoreEntrancesUseCase;

    public CourierRestController(ReceiveCourierLocationUseCase receiveCourierLocationUseCase,
                                 GetTotalTravelDistanceUseCase getTotalTravelDistanceUseCase,
                                 GetStoreEntrancesUseCase getStoreEntrancesUseCase) {
        this.receiveCourierLocationUseCase = receiveCourierLocationUseCase;
        this.getTotalTravelDistanceUseCase = getTotalTravelDistanceUseCase;
        this.getStoreEntrancesUseCase = getStoreEntrancesUseCase;
    }

    @Operation(summary = "Report a courier location",
            description = "Updates the courier's total travelled distance and records a store entrance "
                    + "for every store within 100 m (de-duplicated per store for 60 seconds).")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Accepted for processing"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid coordinates")
    })
    @PostMapping("/location")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void receive(@RequestBody LocationRequest request) {
        if (request.latitude() == null || request.longitude() == null) {
            throw new IllegalArgumentException("latitude and longitude are required");
        }
        Instant occurredAt = request.occurredAt() != null ? request.occurredAt() : Instant.now();
        receiveCourierLocationUseCase.receive(new ReceiveCourierLocationCommand(
                request.courierId(), request.latitude(), request.longitude(), occurredAt));
    }

    @Operation(summary = "Get total travel distance",
            description = "Returns the courier's accumulated distance in metres (0.0 if the courier is unknown).")
    @GetMapping("/{courierId}/total-distance")
    public TotalDistanceResponse totalDistance(@PathVariable String courierId) {
        Double meters = getTotalTravelDistanceUseCase.getTotalTravelDistance(courierId);
        return new TotalDistanceResponse(courierId, meters);
    }

    @Operation(summary = "Get store entrances",
            description = "Returns the courier's recorded store entrances, most recent first (empty if unknown).")
    @GetMapping("/{courierId}/entrances")
    public List<EntranceResponse> entrances(@PathVariable String courierId) {
        return getStoreEntrancesUseCase.getEntrances(courierId).stream()
                .map(EntranceResponse::from)
                .toList();
    }
}
