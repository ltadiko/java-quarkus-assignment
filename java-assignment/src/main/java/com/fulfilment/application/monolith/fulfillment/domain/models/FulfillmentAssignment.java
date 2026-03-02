package com.fulfilment.application.monolith.fulfillment.domain.models;

/**
 * Represents a fulfillment assignment linking a Warehouse to a Product for a specific Store.
 *
 * This entity captures the relationship where a Warehouse acts as a fulfillment unit
 * for delivering a specific Product to a specific Store.
 *
 * Business Rules:
 * - Each Product can be fulfilled by max 2 different Warehouses per Store
 * - Each Store can be fulfilled by max 3 different Warehouses
 * - Each Warehouse can store max 5 types of Products
 */
public class FulfillmentAssignment {

    public Long id;

    /**
     * The business unit code of the warehouse fulfilling this assignment.
     */
    public String warehouseCode;

    /**
     * The ID of the product being fulfilled.
     */
    public Long productId;

    /**
     * The ID of the store receiving fulfillment.
     */
    public Long storeId;

    /**
     * Timestamp when this assignment was created.
     */
    public java.time.LocalDateTime createdAt;

    public FulfillmentAssignment() {}

    public FulfillmentAssignment(String warehouseCode, Long productId, Long storeId) {
        this.warehouseCode = warehouseCode;
        this.productId = productId;
        this.storeId = storeId;
        this.createdAt = java.time.LocalDateTime.now();
    }
}

