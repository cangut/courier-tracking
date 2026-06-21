package com.couriertracking.bootstrap.config;

import com.couriertracking.application.command.ReceiveCourierLocationService;
import com.couriertracking.application.query.GetStoreEntrancesService;
import com.couriertracking.application.query.GetTotalTravelDistanceService;
import com.couriertracking.domain.event.DomainEventPublisher;
import com.couriertracking.domain.port.in.GetStoreEntrancesUseCase;
import com.couriertracking.domain.port.in.GetTotalTravelDistanceUseCase;
import com.couriertracking.domain.port.in.ReceiveCourierLocationUseCase;
import com.couriertracking.domain.port.out.*;
import com.couriertracking.domain.service.DistanceCalculator;
import com.couriertracking.domain.service.EntranceDetector;
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
    public EntranceDetector entranceDetector(DistanceCalculator distanceCalculator) {
        return new EntranceDetector(distanceCalculator);
    }

    @Bean
    public ReceiveCourierLocationUseCase receiveCourierLocationUseCase(StoreRepository storeRepository,
                                                                       DistanceCalculator distanceCalculator,
                                                                       EntranceDetector entranceDetector,
                                                                       CourierStoreEntryRepository courierStoreEntryRepository,
                                                                       LastPositionRepository lastPositionRepository,
                                                                       DistanceCounter distanceCounter,
                                                                       DomainEventPublisher eventPublisher) {
        return new ReceiveCourierLocationService(storeRepository, distanceCalculator, entranceDetector,
                courierStoreEntryRepository, lastPositionRepository, distanceCounter, eventPublisher);
    }

    @Bean
    public GetTotalTravelDistanceUseCase getTotalTravelDistanceUseCase(DistanceCounter distanceCounter) {
        return new GetTotalTravelDistanceService(distanceCounter);
    }

    @Bean
    public GetStoreEntrancesUseCase getEntrancesUseCase(StoreEntranceLogRepository storeEntranceLogRepository) {
        return new GetStoreEntrancesService(storeEntranceLogRepository);
    }
}
