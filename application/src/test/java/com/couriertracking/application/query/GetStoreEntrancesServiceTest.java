package com.couriertracking.application.query;

import com.couriertracking.domain.port.out.StoreEntranceLogRepository;
import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.OccurredAt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStoreEntrancesServiceTest {

    @Mock
    private StoreEntranceLogRepository storeEntranceLogRepository;

    @Test
    void queries_only_entrances_within_the_lookback_window() {
        GetStoreEntrancesService service = new GetStoreEntrancesService(storeEntranceLogRepository);
        when(storeEntranceLogRepository.findByCourierSince(eq(CourierId.of("courier-1")), any()))
                .thenReturn(List.of());

        Instant lowerBound = Instant.now().minus(GetStoreEntrancesService.LOOKBACK);
        service.getEntrances("courier-1");
        Instant upperBound = Instant.now().minus(GetStoreEntrancesService.LOOKBACK);

        ArgumentCaptor<OccurredAt> sinceCaptor = ArgumentCaptor.forClass(OccurredAt.class);
        verify(storeEntranceLogRepository).findByCourierSince(eq(CourierId.of("courier-1")), sinceCaptor.capture());
        assertThat(sinceCaptor.getValue().value()).isBetween(lowerBound, upperBound);
    }
}
