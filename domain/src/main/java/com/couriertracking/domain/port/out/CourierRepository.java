package com.couriertracking.domain.port.out;

import com.couriertracking.domain.aggregate.Courier;
import com.couriertracking.domain.valueobject.CourierId;

import java.util.Optional;

public interface CourierRepository {

    Optional<Courier> find(CourierId courierId);

    void save(Courier courier);
}
