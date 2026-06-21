package com.couriertracking.bootstrap.config;

import com.couriertracking.application.command.ReceiveCourierLocationService;
import com.couriertracking.application.query.GetStoreEntrancesService;
import com.couriertracking.application.query.GetTotalTravelDistanceService;
import com.couriertracking.domain.port.in.GetStoreEntrancesUseCase;
import com.couriertracking.domain.port.in.GetTotalTravelDistanceUseCase;
import com.couriertracking.domain.port.in.ReceiveCourierLocationUseCase;
import com.couriertracking.domain.port.out.*;
import com.couriertracking.domain.service.DistanceCalculator;
import com.couriertracking.domain.service.HaversineDistanceCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfiguration {

    @Bean
    public DistanceCalculator distanceCalculator() {
        return new HaversineDistanceCalculator();
    }

    @Bean
    public ReceiveCourierLocationUseCase receiveCourierLocationUseCase(CourierRepository courierRepository,
                                                                       DistanceCalculator distanceCalculator,
                                                                       StoreRepository storeRepository,
                                                                       StoreEntranceLockRepository storeEntranceLockRepository,
                                                                       StoreEntranceLogRepository storeEntranceLogRepository) {
        return new ReceiveCourierLocationService(courierRepository, distanceCalculator, storeRepository,
                storeEntranceLockRepository, storeEntranceLogRepository);
    }

    @Bean
    public GetTotalTravelDistanceUseCase getTotalTravelDistanceUseCase(CourierRepository courierRepository) {
        return new GetTotalTravelDistanceService(courierRepository);
    }

    @Bean
    public GetStoreEntrancesUseCase getEntrancesUseCase(StoreEntranceLogRepository storeEntranceLogRepository) {
        return new GetStoreEntrancesService(storeEntranceLogRepository);
    }
}
