package com.couriertracking.infrastructure.api;

import com.couriertracking.domain.port.in.GetStoreEntrancesUseCase;
import com.couriertracking.domain.port.in.GetTotalTravelDistanceUseCase;
import com.couriertracking.domain.port.in.ReceiveCourierLocationCommand;
import com.couriertracking.domain.port.in.ReceiveCourierLocationUseCase;
import com.couriertracking.domain.valueobject.CourierId;
import com.couriertracking.domain.valueobject.EntranceLog;
import com.couriertracking.domain.valueobject.GeoPoint;
import com.couriertracking.domain.valueobject.OccurredAt;
import com.couriertracking.domain.valueobject.StoreName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CourierRestControllerTest {

    @Mock
    private ReceiveCourierLocationUseCase receiveCourierLocationUseCase;
    @Mock
    private GetTotalTravelDistanceUseCase getTotalTravelDistanceUseCase;
    @Mock
    private GetStoreEntrancesUseCase getStoreEntrancesUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        CourierRestController controller = new CourierRestController(
                receiveCourierLocationUseCase, getTotalTravelDistanceUseCase, getStoreEntrancesUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void receive_returns_202_and_forwards_command_with_server_timestamp() throws Exception {
        String body = "{\"courierId\":\"courier-1\",\"latitude\":40.9923307,\"longitude\":29.1244229}";

        Instant before = Instant.now();
        mockMvc.perform(post("/api/couriers/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted());
        Instant after = Instant.now();

        ArgumentCaptor<ReceiveCourierLocationCommand> captor =
                ArgumentCaptor.forClass(ReceiveCourierLocationCommand.class);
        verify(receiveCourierLocationUseCase).receive(captor.capture());
        ReceiveCourierLocationCommand command = captor.getValue();
        assertThat(command.courierId()).isEqualTo("courier-1");
        assertThat(command.lat()).isEqualTo(40.9923307);
        assertThat(command.lng()).isEqualTo(29.1244229);
        assertThat(command.occurredAt()).isBetween(before, after);
    }

    @Test
    void receive_returns_400_when_latitude_missing() throws Exception {
        String body = "{\"courierId\":\"courier-1\",\"longitude\":29.1244229}";

        mockMvc.perform(post("/api/couriers/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"))
                .andExpect(jsonPath("$.message").value("latitude and longitude are required"));

        verifyNoInteractions(receiveCourierLocationUseCase);
    }

    @Test
    void total_distance_returns_value() throws Exception {
        when(getTotalTravelDistanceUseCase.getTotalTravelDistance("courier-1")).thenReturn(1234.5);

        mockMvc.perform(get("/api/couriers/courier-1/total-distance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courierId").value("courier-1"))
                .andExpect(jsonPath("$.totalDistanceMeters").value(1234.5));
    }

    @Test
    void entrances_returns_mapped_logs() throws Exception {
        EntranceLog log = new EntranceLog(
                new CourierId("courier-1"),
                new StoreName("Ataşehir MMM Migros"),
                new GeoPoint(40.9923307, 29.1244229),
                OccurredAt.of(Instant.parse("2026-06-20T10:15:30Z")));
        when(getStoreEntrancesUseCase.getEntrances("courier-1")).thenReturn(List.of(log));

        mockMvc.perform(get("/api/couriers/courier-1/entrances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].storeName").value("Ataşehir MMM Migros"))
                .andExpect(jsonPath("$[0].lat").value(40.9923307))
                .andExpect(jsonPath("$[0].lng").value(29.1244229))
                .andExpect(jsonPath("$[0].occurredAt").value("2026-06-20T10:15:30Z"));
    }

    @Test
    void entrances_returns_empty_array_when_none() throws Exception {
        when(getStoreEntrancesUseCase.getEntrances("courier-1")).thenReturn(List.of());

        mockMvc.perform(get("/api/couriers/courier-1/entrances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
