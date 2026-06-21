package com.couriertracking.infrastructure.adapter;

import com.couriertracking.domain.port.out.StoreEntranceLogRepository;
import com.couriertracking.domain.valueobject.*;
import com.couriertracking.infrastructure.persistence.cassandra.EntranceLogCassandraRepository;
import com.couriertracking.infrastructure.persistence.cassandra.EntranceLogEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StoreEntranceLogRepositoryAdapter implements StoreEntranceLogRepository {

    private final EntranceLogCassandraRepository repository;

    public StoreEntranceLogRepositoryAdapter(EntranceLogCassandraRepository repository) {
        this.repository = repository;
    }

    @Override
    public void append(EntranceLog entrance) {
        repository.save(new EntranceLogEntity(
                entrance.courierId().value(),
                entrance.occurredAt().value(),
                entrance.storeName().value(),
                entrance.location().latitude(),
                entrance.location().longitude()));
    }

    @Override
    public List<EntranceLog> findByCourier(CourierId courierId) {
        return repository.findByCourierId(courierId.value()).stream()
                .map(entranceLogEntity -> new EntranceLog(
                        new CourierId(entranceLogEntity.getCourierId()),
                        new StoreName(entranceLogEntity.getStoreName()),
                        new GeoPoint(entranceLogEntity.getLatitude(), entranceLogEntity.getLongitude()),
                        new OccurredAt(entranceLogEntity.getOccurredAt())))
                .toList();
    }
}
