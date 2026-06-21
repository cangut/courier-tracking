package com.couriertracking.domain.port.out;

import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.EntranceLog;
import com.couriertracking.domain.valueobject.OccurredAt;

import java.util.List;

public interface StoreEntranceLogRepository {

    void append(EntranceLog entrance);

    List<EntranceLog> findByCourierSince(CourierId courierId, OccurredAt since);
}
