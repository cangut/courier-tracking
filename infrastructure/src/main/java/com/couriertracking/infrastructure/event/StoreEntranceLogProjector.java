package com.couriertracking.infrastructure.event;

import com.couriertracking.domain.event.StoreEntranceDetected;
import com.couriertracking.domain.port.out.StoreEntranceLogRepository;
import com.couriertracking.domain.valueobject.EntranceLog;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StoreEntranceLogProjector {

    private final StoreEntranceLogRepository storeEntranceLogRepository;

    public StoreEntranceLogProjector(StoreEntranceLogRepository storeEntranceLogRepository) {
        this.storeEntranceLogRepository = storeEntranceLogRepository;
    }

    @EventListener
    public void on(StoreEntranceDetected event) {
        storeEntranceLogRepository.append(new EntranceLog(
                event.courierId(),
                event.storeName(),
                event.location(),
                event.occurredAt()));
    }

}
