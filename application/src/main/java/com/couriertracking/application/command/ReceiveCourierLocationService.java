package com.couriertracking.application.command;

import com.couriertracking.domain.aggregate.Courier;
import com.couriertracking.domain.entity.Store;
import com.couriertracking.domain.event.DomainEventPublisher;
import com.couriertracking.domain.event.StoreEntranceDetected;
import com.couriertracking.domain.port.in.ReceiveCourierLocationCommand;
import com.couriertracking.domain.port.in.ReceiveCourierLocationUseCase;
import com.couriertracking.domain.port.out.CourierRepository;
import com.couriertracking.domain.port.out.StoreEntranceLockRepository;
import com.couriertracking.domain.port.out.StoreRepository;
import com.couriertracking.domain.service.DistanceCalculator;
import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.GeoPoint;
import com.couriertracking.domain.valueobject.OccurredAt;

public class ReceiveCourierLocationService implements ReceiveCourierLocationUseCase {

    private final CourierRepository courierRepository;
    private final DistanceCalculator distanceCalculator;
    private final StoreRepository storeRepository;
    private final StoreEntranceLockRepository storeEntranceLockRepository;
    private final DomainEventPublisher domainEventPublisher;

    public ReceiveCourierLocationService(CourierRepository courierRepository,
                                         DistanceCalculator distanceCalculator,
                                         StoreRepository storeRepository,
                                         StoreEntranceLockRepository storeEntranceLockRepository,
                                         DomainEventPublisher domainEventPublisher) {
        this.courierRepository = courierRepository;
        this.distanceCalculator = distanceCalculator;
        this.storeRepository = storeRepository;
        this.storeEntranceLockRepository = storeEntranceLockRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public void receive(ReceiveCourierLocationCommand command) {
        CourierId courierId = CourierId.of(command.courierId());
        GeoPoint position = new GeoPoint(command.lat(), command.lng());
        OccurredAt occurredAt = OccurredAt.of(command.occurredAt());

        Courier courier = courierRepository.find(courierId).orElseGet(() -> Courier.startingAt(courierId));
        courier.addStores(storeRepository.findAll());
        courier.moveTo(position, distanceCalculator);
        courierRepository.save(courier);

        for (Store store : courier.getStores()) {
            if (storeEntranceLockRepository.registerIfAbsent(courier.id(), store.name())) {
                domainEventPublisher.publish(new StoreEntranceDetected(courierId, store.name(), position, occurredAt));
            }
        }
    }

}
