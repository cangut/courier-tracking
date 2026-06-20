package com.couriertracking.domain.port.out;

import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.Distance;

public interface DistanceCounter {
    Distance increment(CourierId courierId, Distance delta);

    Distance total(CourierId courierId);
}
