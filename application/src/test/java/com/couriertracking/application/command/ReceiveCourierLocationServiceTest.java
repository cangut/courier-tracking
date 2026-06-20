package com.couriertracking.application.command;

import com.couriertracking.domain.entity.Store;
import com.couriertracking.domain.event.DomainEventPublisher;
import com.couriertracking.domain.event.StoreEntranceDetected;
import com.couriertracking.domain.port.in.ReceiveCourierLocationCommand;
import com.couriertracking.domain.port.out.CourierStoreEntryRepository;
import com.couriertracking.domain.port.out.DistanceCounter;
import com.couriertracking.domain.port.out.LastPositionRepository;
import com.couriertracking.domain.port.out.StoreRepository;
import com.couriertracking.domain.service.DistanceCalculator;
import com.couriertracking.domain.service.EntranceDetector;
import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.Distance;
import com.couriertracking.domain.valueobject.GeoPoint;
import com.couriertracking.domain.valueobject.OccurredAt;
import com.couriertracking.domain.valueobject.StoreName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiveCourierLocationServiceTest {

    @Mock
    private StoreRepository storeRepository;
    @Mock
    private DistanceCalculator distanceCalculator;
    @Mock
    private EntranceDetector entranceDetector;
    @Mock
    private CourierStoreEntryRepository courierStoreEntryRepository;
    @Mock
    private LastPositionRepository lastPositionRepository;
    @Mock
    private DistanceCounter distanceCounter;
    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private ReceiveCourierLocationService service;

    private final Instant occurredAt = Instant.parse("2026-06-20T10:15:30Z");
    private final CourierId courierId = new CourierId("courier-1");
    private final GeoPoint position = new GeoPoint(40.9923307, 29.1244229);
    private final ReceiveCourierLocationCommand command =
            new ReceiveCourierLocationCommand("courier-1", 40.9923307, 29.1244229, occurredAt);

    @Test
    void first_position_increments_zero_distance_and_saves_position() {
        service.receive(command);

        verify(distanceCounter).increment(courierId, Distance.ZERO);
        verify(lastPositionRepository).save(courierId, position);
        verifyNoInteractions(domainEventPublisher);
    }

    @Test
    void existing_last_position_increments_calculated_distance() {
        GeoPoint last = new GeoPoint(40.0, 29.0);
        when(lastPositionRepository.find(courierId)).thenReturn(Optional.of(last));
        when(distanceCalculator.distance(last, position)).thenReturn(Distance.ofMeters(250.0));

        service.receive(command);

        verify(distanceCounter).increment(courierId, Distance.ofMeters(250.0));
        verify(lastPositionRepository).save(courierId, position);
    }

    @Test
    void publishes_event_when_entrance_is_valid() {
        Store store = new Store(new StoreName("Ataşehir MMM Migros"), new GeoPoint(40.9920, 29.1240));
        when(entranceDetector.detectEntrance(eq(position), any())).thenReturn(List.of(store));
        when(courierStoreEntryRepository.isValidEntry(courierId, store.name())).thenReturn(true);

        service.receive(command);

        ArgumentCaptor<StoreEntranceDetected> captor = ArgumentCaptor.forClass(StoreEntranceDetected.class);
        verify(domainEventPublisher).publish(captor.capture());
        StoreEntranceDetected published = captor.getValue();
        assertThat(published.courierId()).isEqualTo(courierId);
        assertThat(published.storeName()).isEqualTo(store.name());
        assertThat(published.location()).isEqualTo(position);
        assertThat(published.occurredAt()).isEqualTo(OccurredAt.of(occurredAt));
    }

    @Test
    void does_not_publish_when_entry_is_invalid() {
        Store store = new Store(new StoreName("Ataşehir MMM Migros"), new GeoPoint(40.9920, 29.1240));
        when(entranceDetector.detectEntrance(eq(position), any())).thenReturn(List.of(store));
        when(courierStoreEntryRepository.isValidEntry(courierId, store.name())).thenReturn(false);

        service.receive(command);

        verifyNoInteractions(domainEventPublisher);
    }

    @Test
    void publishes_only_valid_entries_for_multiple_stores() {
        Store valid = new Store(new StoreName("Novada"), new GeoPoint(40.9921, 29.1241));
        Store invalid = new Store(new StoreName("Beylikdüzü Migros"), new GeoPoint(40.9922, 29.1242));
        when(entranceDetector.detectEntrance(eq(position), any())).thenReturn(List.of(valid, invalid));
        when(courierStoreEntryRepository.isValidEntry(courierId, valid.name())).thenReturn(true);
        when(courierStoreEntryRepository.isValidEntry(courierId, invalid.name())).thenReturn(false);

        service.receive(command);

        ArgumentCaptor<StoreEntranceDetected> captor = ArgumentCaptor.forClass(StoreEntranceDetected.class);
        verify(domainEventPublisher, times(1)).publish(captor.capture());
        assertThat(captor.getValue().storeName()).isEqualTo(valid.name());
    }

    @Test
    void detects_entrances_against_full_store_catalog() {
        Store store = new Store(new StoreName("Ataşehir MMM Migros"), new GeoPoint(40.9920, 29.1240));
        when(storeRepository.findAll()).thenReturn(List.of(store));
        when(entranceDetector.detectEntrance(eq(position), eq(List.of(store)))).thenReturn(List.of());

        service.receive(command);

        verify(storeRepository).findAll();
        verify(entranceDetector).detectEntrance(position, List.of(store));
        verifyNoInteractions(domainEventPublisher);
    }
}
