package com.couriertracking.infrastructure.event;

import com.couriertracking.domain.event.DomainEventPublisher;
import com.couriertracking.domain.event.StoreEntranceDetected;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher publisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(StoreEntranceDetected event) {
        publisher.publishEvent(event);
    }
}
