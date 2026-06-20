package com.couriertracking.application.query;

import com.couriertracking.domain.port.in.GetTotalTravelDistanceUseCase;
import com.couriertracking.domain.port.out.DistanceCounter;
import com.couriertracking.domain.valueobject.CourierId;

public class GetTotalTravelDistanceService implements GetTotalTravelDistanceUseCase {

    private final DistanceCounter distanceCounter;

    public GetTotalTravelDistanceService(DistanceCounter distanceCounter) {
        this.distanceCounter = distanceCounter;
    }

    @Override
    public Double getTotalTravelDistance(String courierId) {
        return distanceCounter.total(CourierId.of(courierId)).meters();
    }
}
