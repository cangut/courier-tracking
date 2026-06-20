package com.couriertracking.domain.port.out;

import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.StoreName;

public interface CourierStoreEntranceRepository {

    boolean isValidEntrance(CourierId courierId, StoreName storeName);
}
