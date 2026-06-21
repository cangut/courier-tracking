package com.couriertracking.application.query;

import com.couriertracking.domain.port.in.GetStoreEntrancesUseCase;
import com.couriertracking.domain.port.out.StoreEntranceLogRepository;
import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.EntranceLog;

import java.util.List;

public class GetStoreEntrancesService implements GetStoreEntrancesUseCase {

    private final StoreEntranceLogRepository storeEntranceLogRepository;

    public GetStoreEntrancesService(StoreEntranceLogRepository storeEntranceLogRepository) {
        this.storeEntranceLogRepository = storeEntranceLogRepository;
    }

    @Override
    public List<EntranceLog> getEntrances(String courierId) {
        return storeEntranceLogRepository.findByCourier(CourierId.of(courierId));
    }
}
