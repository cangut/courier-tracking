package com.couriertracking.application.command;

import com.couriertracking.domain.aggregate.Courier;
import com.couriertracking.domain.entity.Store;
import com.couriertracking.domain.event.DomainEventPublisher;
import com.couriertracking.domain.event.StoreEntranceDetected;
import com.couriertracking.domain.port.in.ReceiveCourierLocationCommand;
import com.couriertracking.domain.port.in.ReceiveCourierLocationUseCase;
import com.couriertracking.domain.port.out.CourierStoreEntryRepository;
import com.couriertracking.domain.port.out.DistanceCounter;
import com.couriertracking.domain.port.out.LastPositionRepository;
import com.couriertracking.domain.port.out.StoreRepository;
import com.couriertracking.domain.service.DistanceCalculator;
import com.couriertracking.domain.service.EntranceDetector;
import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.Distance;
import com.couriertracking.domain.valueobject.GeoPoint;
import com.couriertracking.domain.valueobject.OccurredAt;

public class ReceiveCourierLocationService implements ReceiveCourierLocationUseCase {

    private final StoreRepository storeRepository;
    private final DistanceCalculator distanceCalculator;
    private final EntranceDetector entranceDetector;
    private final CourierStoreEntryRepository courierStoreEntryRepository;
    private final LastPositionRepository lastPositionRepository;
    private final DistanceCounter distanceCounter;
    private final DomainEventPublisher domainEventPublisher;


    public ReceiveCourierLocationService(StoreRepository storeRepository,
                                         DistanceCalculator distanceCalculator,
                                         EntranceDetector entranceDetector,
                                         CourierStoreEntryRepository courierStoreEntryRepository,
                                         LastPositionRepository lastPositionRepository,
                                         DistanceCounter distanceCounter,
                                         DomainEventPublisher domainEventPublisher) {
        this.storeRepository = storeRepository;
        this.distanceCalculator = distanceCalculator;
        this.entranceDetector = entranceDetector;
        this.courierStoreEntryRepository = courierStoreEntryRepository;
        this.lastPositionRepository = lastPositionRepository;
        this.distanceCounter = distanceCounter;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public void receive(ReceiveCourierLocationCommand command) {
        CourierId courierId = CourierId.of(command.courierId());
        GeoPoint position = new GeoPoint(command.lat(), command.lng());
        OccurredAt occurredAt = OccurredAt.of(command.occurredAt());

        Courier courier = new Courier(courierId, lastPositionRepository.find(courierId).orElse(null));
        Distance increment = courier.moveTo(position, distanceCalculator);
        distanceCounter.increment(courierId, increment);
        lastPositionRepository.save(courierId, position);

        for (Store store : entranceDetector.detectEntrance(position, storeRepository.findAll())) {
            if (courierStoreEntryRepository.isValidEntry(courierId, store.name())) {
                domainEventPublisher.publish(
                        new StoreEntranceDetected(courierId, store.name(), position, occurredAt));
            }
        }
    }
}
