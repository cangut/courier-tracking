package com.couriertracking.domain.port.out;

import com.couriertracking.domain.entity.Store;

import java.util.Collection;

public interface StoreRepository {

    Collection<Store> findAll();
}
