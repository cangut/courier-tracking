package com.couriertracking.application.query;

import com.couriertracking.domain.port.in.GetTotalTravelDistanceUseCase;
import com.couriertracking.domain.port.out.CourierRepository;
import com.couriertracking.domain.valueobject.CourierId;

public class GetTotalTravelDistanceService implements GetTotalTravelDistanceUseCase {

    private final CourierRepository courierRepository;

    public GetTotalTravelDistanceService(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Override
    public Double getTotalTravelDistance(String courierId) {
        return courierRepository.find(CourierId.of(courierId))
                .map(courier -> courier.totalDistance().meters())
                .orElse(0.0);
    }
}
