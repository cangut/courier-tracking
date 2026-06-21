package com.couriertracking.infrastructure.persistence.cassandra;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;

@Table("store_entrances")
public class EntranceLogEntity {

    @PrimaryKeyColumn(name = "courier_id", type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    private String courierId;

    @PrimaryKeyColumn(name = "occurred_at", type = PrimaryKeyType.CLUSTERED, ordinal = 1, ordering = Ordering.DESCENDING)
    private Instant occurredAt;

    @PrimaryKeyColumn(name = "store_name", type = PrimaryKeyType.CLUSTERED, ordinal = 2, ordering = Ordering.ASCENDING)
    private String storeName;

    @Column("latitude")
    private double latitude;

    @Column("longitude")
    private double longitude;

    public EntranceLogEntity(String courierId, Instant occurredAt, String storeName, double latitude, double longitude) {
        this.courierId = courierId;
        this.occurredAt = occurredAt;
        this.storeName = storeName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getCourierId() {
        return courierId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getStoreName() {
        return storeName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
