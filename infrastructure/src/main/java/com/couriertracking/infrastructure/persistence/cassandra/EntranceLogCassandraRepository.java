package com.couriertracking.infrastructure.persistence.cassandra;

import org.springframework.data.cassandra.repository.MapIdCassandraRepository;

import java.time.Instant;
import java.util.List;

public interface EntranceLogCassandraRepository extends MapIdCassandraRepository<EntranceLogEntity> {


    List<EntranceLogEntity> findByCourierIdAndOccurredAtGreaterThanEqual(String courierId, Instant since);
}
