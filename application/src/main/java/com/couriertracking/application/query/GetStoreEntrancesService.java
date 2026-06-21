package com.couriertracking.application.query;

import com.couriertracking.domain.port.in.GetStoreEntrancesUseCase;
import com.couriertracking.domain.port.out.StoreEntranceLogRepository;
import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.EntranceLog;
import com.couriertracking.domain.valueobject.OccurredAt;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class GetStoreEntrancesService implements GetStoreEntrancesUseCase {
    static final Duration LOOKBACK = Duration.ofDays(7);

    private final StoreEntranceLogRepository storeEntranceLogRepository;

    public GetStoreEntrancesService(StoreEntranceLogRepository storeEntranceLogRepository) {
        this.storeEntranceLogRepository = storeEntranceLogRepository;
    }

    @Override
    public List<EntranceLog> getEntrances(String courierId) {
        OccurredAt since = OccurredAt.of(Instant.now().minus(LOOKBACK));
        return storeEntranceLogRepository.findByCourierSince(CourierId.of(courierId), since);
    }
}
