package com.couriertracking.infrastructure.adapter;

import com.couriertracking.domain.entity.Store;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class StoreRepositoryAdapterTest {

    @Test
    void loads_the_five_migros_stores_from_json() {
        StoreRepositoryAdapter catalog =
                new StoreRepositoryAdapter(new ClassPathResource("stores.json"), new ObjectMapper());

        Collection<Store> stores = catalog.findAll();

        assertThat(stores).hasSize(5);
        assertThat(stores).extracting(s -> s.name().value())
                .contains("Ataşehir MMM Migros", "Caddebostan MMM Migros");
    }
}
