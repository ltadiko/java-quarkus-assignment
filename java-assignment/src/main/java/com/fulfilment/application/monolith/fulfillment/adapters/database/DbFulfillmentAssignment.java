package com.fulfilment.application.monolith.fulfillment.adapters.database;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;

/**
 * Database entity for fulfillment assignments.
 *
 * Links warehouses to products for specific stores, enabling tracking of
 * which warehouses fulfill which products for which stores.
 */
@Entity
@Table(
    name = "fulfillment_assignment",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"warehouseCode", "productId", "storeId"})
    }
)
@Cacheable
public class DbFulfillmentAssignment {

    @Id
    @GeneratedValue
    public Long id;

    @Column(nullable = false)
    public String warehouseCode;

    @Column(nullable = false)
    public Long productId;

    @Column(nullable = false)
    public Long storeId;

    @Column(nullable = false)
    public LocalDateTime createdAt;

    public DbFulfillmentAssignment() {}

    public DbFulfillmentAssignment(String warehouseCode, Long productId, Long storeId) {
        this.warehouseCode = warehouseCode;
        this.productId = productId;
        this.storeId = storeId;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Converts this database entity to a domain model.
     */
    public FulfillmentAssignment toFulfillmentAssignment() {
        FulfillmentAssignment assignment = new FulfillmentAssignment();
        assignment.id = this.id;
        assignment.warehouseCode = this.warehouseCode;
        assignment.productId = this.productId;
        assignment.storeId = this.storeId;
        assignment.createdAt = this.createdAt;
        return assignment;
    }

    /**
     * Creates a database entity from a domain model.
     */
    public static DbFulfillmentAssignment fromFulfillmentAssignment(FulfillmentAssignment assignment) {
        DbFulfillmentAssignment db = new DbFulfillmentAssignment();
        db.id = assignment.id;
        db.warehouseCode = assignment.warehouseCode;
        db.productId = assignment.productId;
        db.storeId = assignment.storeId;
        db.createdAt = assignment.createdAt != null ? assignment.createdAt : LocalDateTime.now();
        return db;
    }
}

