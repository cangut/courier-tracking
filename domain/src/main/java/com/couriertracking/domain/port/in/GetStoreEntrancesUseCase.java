package com.couriertracking.domain.port.in;

import com.couriertracking.domain.valueobject.EntranceLog;

import java.util.List;

public interface GetStoreEntrancesUseCase {

    List<EntranceLog> getEntrances(String courierId);
}
