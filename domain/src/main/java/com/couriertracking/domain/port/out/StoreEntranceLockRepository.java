package com.couriertracking.domain.port.out;

import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.StoreName;

public interface StoreEntranceLockRepository {

    boolean registerIfAbsent(CourierId courierId, StoreName storeName);
}
