package com.couriertracking.domain.event;

public interface DomainEventPublisher {

    void publish(StoreEntranceDetected event);
}
