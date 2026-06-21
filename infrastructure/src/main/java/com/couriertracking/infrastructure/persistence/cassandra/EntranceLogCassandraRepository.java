package com.couriertracking.infrastructure.persistence.cassandra;

import org.springframework.data.cassandra.repository.MapIdCassandraRepository;

import java.time.Instant;
import java.util.List;

public interface EntranceLogCassandraRepository extends MapIdCassandraRepository<EntranceLogEntity> {

    // WHERE courier_id = ? AND occurred_at >= ? — a clustering-column slice, returned newest-first
    // by the table's occurred_at DESC clustering order. No ALLOW FILTERING needed.
    List<EntranceLogEntity> findByCourierIdAndOccurredAtGreaterThanEqual(String courierId, Instant since);
}
