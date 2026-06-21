package com.couriertracking.application.command;

import com.couriertracking.domain.aggregate.Courier;
import com.couriertracking.domain.entity.Store;
import com.couriertracking.domain.port.in.ReceiveCourierLocationCommand;
import com.couriertracking.domain.port.out.CourierRepository;
import com.couriertracking.domain.port.out.StoreEntranceLockRepository;
import com.couriertracking.domain.port.out.StoreEntranceLogRepository;
import com.couriertracking.domain.port.out.StoreRepository;
import com.couriertracking.domain.service.DistanceCalculator;
import com.couriertracking.domain.service.HaversineDistanceCalculator;
import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.Distance;
import com.couriertracking.domain.valueobject.EntranceLog;
import com.couriertracking.domain.valueobject.GeoPoint;
import com.couriertracking.domain.valueobject.OccurredAt;
import com.couriertracking.domain.valueobject.StoreName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiveCourierLocationServiceTest {

    @Mock
    private CourierRepository courierRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private StoreEntranceLockRepository storeEntranceLockRepository;
    @Mock
    private StoreEntranceLogRepository storeEntranceLogRepository;

    // Real calculator: the distance/entrance assertions depend on actual Haversine geometry.
    private final DistanceCalculator distanceCalculator = new HaversineDistanceCalculator();

    private ReceiveCourierLocationService service;

    private final Instant occurredAt = Instant.parse("2026-06-20T10:15:30Z");
    private final CourierId courierId = new CourierId("courier-1");
    private final GeoPoint position = new GeoPoint(40.9923307, 29.1244229);
    private final ReceiveCourierLocationCommand command =
            new ReceiveCourierLocationCommand("courier-1", 40.9923307, 29.1244229, occurredAt);

    @BeforeEach
    void setUp() {
        service = new ReceiveCourierLocationService(
                courierRepository, distanceCalculator, storeRepository, storeEntranceLockRepository, storeEntranceLogRepository);
    }

    @Test
    void first_position_saves_new_courier_with_zero_distance_and_no_entrances() {
        service.receive(command);

        ArgumentCaptor<Courier> captor = ArgumentCaptor.forClass(Courier.class);
        verify(courierRepository).save(captor.capture());
        Courier saved = captor.getValue();
        assertThat(saved.lastPosition()).contains(position);
        assertThat(saved.totalDistance()).isEqualTo(Distance.ZERO);
        verifyNoInteractions(storeEntranceLockRepository, storeEntranceLogRepository);
    }

    @Test
    void existing_courier_accumulates_distance_onto_running_total() {
        GeoPoint last = new GeoPoint(40.0, 29.0);
        Courier existing = new Courier(courierId, last, Distance.ofMeters(1000.0));
        when(courierRepository.find(courierId)).thenReturn(Optional.of(existing));

        service.receive(command);

        ArgumentCaptor<Courier> captor = ArgumentCaptor.forClass(Courier.class);
        verify(courierRepository).save(captor.capture());
        Courier saved = captor.getValue();
        assertThat(saved.lastPosition()).contains(position);
        assertThat(saved.totalDistance().meters()).isGreaterThan(1000.0);
    }

    @Test
    void registers_and_logs_an_entrance_for_a_store_within_range() {
        Store store = new Store(StoreName.of("Ataşehir MMM Migros"), position);
        when(storeRepository.findAll()).thenReturn(List.of(store));
        when(storeEntranceLockRepository.registerIfAbsent(eq(courierId), eq(store.name()))).thenReturn(true);

        service.receive(command);

        verify(storeEntranceLockRepository).registerIfAbsent(courierId, store.name());
        verify(storeEntranceLogRepository).append(
                new EntranceLog(courierId, store.name(), position, OccurredAt.of(occurredAt)));
    }

    @Test
    void does_not_log_when_entrance_is_a_duplicate_within_the_window() {
        Store store = new Store(StoreName.of("Ataşehir MMM Migros"), position);
        when(storeRepository.findAll()).thenReturn(List.of(store));
        when(storeEntranceLockRepository.registerIfAbsent(eq(courierId), eq(store.name()))).thenReturn(false);

        service.receive(command);

        verify(storeEntranceLockRepository).registerIfAbsent(courierId, store.name());
        verify(storeEntranceLogRepository, never()).append(any());
    }
}
