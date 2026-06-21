package com.couriertracking.infrastructure.api;

import com.couriertracking.domain.port.in.GetStoreEntrancesUseCase;
import com.couriertracking.domain.port.in.GetTotalTravelDistanceUseCase;
import com.couriertracking.domain.port.in.ReceiveCourierLocationCommand;
import com.couriertracking.domain.port.in.ReceiveCourierLocationUseCase;
import com.couriertracking.infrastructure.api.dto.EntranceResponse;
import com.couriertracking.infrastructure.api.dto.LocationRequest;
import com.couriertracking.infrastructure.api.dto.TotalDistanceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/couriers")
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

    @GetMapping("/{courierId}/total-distance")
    public TotalDistanceResponse totalDistance(@PathVariable String courierId) {
        Double meters = getTotalTravelDistanceUseCase.getTotalTravelDistance(courierId);
        return new TotalDistanceResponse(courierId, meters);
    }

    @GetMapping("/{courierId}/entrances")
    public List<EntranceResponse> entrances(@PathVariable String courierId) {
        return getStoreEntrancesUseCase.getEntrances(courierId).stream()
                .map(EntranceResponse::from)
                .toList();
    }
}
