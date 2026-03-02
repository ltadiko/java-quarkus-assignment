package com.fulfilment.application.monolith.warehouses.domain.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Warehouse domain model.
 * Tests the Warehouse POJO and its fields.
 */
public class WarehouseModelTest {

    @Test
    @DisplayName("Should create warehouse with all fields")
    void testWarehouseCreation() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "WH-TEST-001";
        warehouse.location = "AMSTERDAM-001";
        warehouse.capacity = 100;
        warehouse.stock = 50;
        warehouse.createdAt = LocalDateTime.now();
        warehouse.archivedAt = null;

        assertEquals("WH-TEST-001", warehouse.businessUnitCode);
        assertEquals("AMSTERDAM-001", warehouse.location);
        assertEquals(Integer.valueOf(100), warehouse.capacity);
        assertEquals(Integer.valueOf(50), warehouse.stock);
        assertNotNull(warehouse.createdAt);
        assertNull(warehouse.archivedAt);
    }

    @Test
    @DisplayName("Should create warehouse with default values")
    void testWarehouseDefaults() {
        Warehouse warehouse = new Warehouse();

        assertNull(warehouse.businessUnitCode);
        assertNull(warehouse.location);
        assertNull(warehouse.capacity);
        assertNull(warehouse.stock);
        assertNull(warehouse.createdAt);
        assertNull(warehouse.archivedAt);
    }

    @Test
    @DisplayName("Should set archived timestamp")
    void testWarehouseArchiving() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "WH-ARCHIVE";
        warehouse.createdAt = LocalDateTime.now().minusDays(30);

        assertNull(warehouse.archivedAt);

        warehouse.archivedAt = LocalDateTime.now();

        assertNotNull(warehouse.archivedAt);
        assertTrue(warehouse.archivedAt.isAfter(warehouse.createdAt));
    }

    @Test
    @DisplayName("Should handle zero capacity")
    void testZeroCapacity() {
        Warehouse warehouse = new Warehouse();
        warehouse.capacity = 0;
        warehouse.stock = 0;

        assertEquals(Integer.valueOf(0), warehouse.capacity);
        assertEquals(Integer.valueOf(0), warehouse.stock);
    }

    @Test
    @DisplayName("Should handle large capacity values")
    void testLargeCapacity() {
        Warehouse warehouse = new Warehouse();
        warehouse.capacity = Integer.MAX_VALUE;
        warehouse.stock = Integer.MAX_VALUE;

        assertEquals(Integer.valueOf(Integer.MAX_VALUE), warehouse.capacity);
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), warehouse.stock);
    }

    @Test
    @DisplayName("Should allow stock equal to capacity")
    void testStockEqualsCapacity() {
        Warehouse warehouse = new Warehouse();
        warehouse.capacity = 100;
        warehouse.stock = 100;

        assertEquals(warehouse.capacity, warehouse.stock);
    }
}

