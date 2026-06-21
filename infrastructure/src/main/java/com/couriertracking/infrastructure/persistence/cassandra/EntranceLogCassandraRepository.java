package com.couriertracking.infrastructure.persistence.cassandra;

import org.springframework.data.cassandra.repository.MapIdCassandraRepository;

import java.util.List;

public interface EntranceLogCassandraRepository extends MapIdCassandraRepository<EntranceLogEntity> {

    List<EntranceLogEntity> findByCourierId(String courierId);
}
