package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DbWarehouse entity.
 * Tests entity mapping and conversion to domain model.
 */
public class DbWarehouseTest {

    @Test
    @DisplayName("Should create DbWarehouse with default constructor")
    void testDefaultConstructor() {
        DbWarehouse dbWarehouse = new DbWarehouse();

        assertNull(dbWarehouse.id);
        assertNull(dbWarehouse.businessUnitCode);
        assertNull(dbWarehouse.location);
        assertNull(dbWarehouse.capacity);
        assertNull(dbWarehouse.stock);
        assertNull(dbWarehouse.createdAt);
        assertNull(dbWarehouse.archivedAt);
    }

    @Test
    @DisplayName("Should set all fields")
    void testSetAllFields() {
        DbWarehouse dbWarehouse = new DbWarehouse();
        dbWarehouse.id = 1L;
        dbWarehouse.businessUnitCode = "WH-TEST-001";
        dbWarehouse.location = "AMSTERDAM-001";
        dbWarehouse.capacity = 100;
        dbWarehouse.stock = 50;
        dbWarehouse.createdAt = LocalDateTime.now();
        dbWarehouse.archivedAt = null;

        assertEquals(1L, dbWarehouse.id);
        assertEquals("WH-TEST-001", dbWarehouse.businessUnitCode);
        assertEquals("AMSTERDAM-001", dbWarehouse.location);
        assertEquals(100, dbWarehouse.capacity);
        assertEquals(50, dbWarehouse.stock);
        assertNotNull(dbWarehouse.createdAt);
        assertNull(dbWarehouse.archivedAt);
    }

    @Test
    @DisplayName("Should convert to Warehouse domain model")
    void testToWarehouse() {
        DbWarehouse dbWarehouse = new DbWarehouse();
        dbWarehouse.id = 1L;
        dbWarehouse.businessUnitCode = "WH-CONVERT";
        dbWarehouse.location = "TILBURG-001";
        dbWarehouse.capacity = 200;
        dbWarehouse.stock = 100;
        dbWarehouse.createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        dbWarehouse.archivedAt = LocalDateTime.of(2024, 6, 1, 10, 0);

        Warehouse warehouse = dbWarehouse.toWarehouse();

        assertEquals("WH-CONVERT", warehouse.businessUnitCode);
        assertEquals("TILBURG-001", warehouse.location);
        assertEquals(200, warehouse.capacity);
        assertEquals(100, warehouse.stock);
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), warehouse.createdAt);
        assertEquals(LocalDateTime.of(2024, 6, 1, 10, 0), warehouse.archivedAt);
    }

    @Test
    @DisplayName("Should convert to Warehouse with null archivedAt")
    void testToWarehouseNotArchived() {
        DbWarehouse dbWarehouse = new DbWarehouse();
        dbWarehouse.businessUnitCode = "WH-ACTIVE";
        dbWarehouse.location = "ZWOLLE-001";
        dbWarehouse.capacity = 50;
        dbWarehouse.stock = 25;
        dbWarehouse.createdAt = LocalDateTime.now();
        dbWarehouse.archivedAt = null;

        Warehouse warehouse = dbWarehouse.toWarehouse();

        assertNull(warehouse.archivedAt);
        assertNotNull(warehouse.createdAt);
    }

    @Test
    @DisplayName("Should convert with null values")
    void testToWarehouseWithNulls() {
        DbWarehouse dbWarehouse = new DbWarehouse();
        // All fields null except required ones

        Warehouse warehouse = dbWarehouse.toWarehouse();

        assertNull(warehouse.businessUnitCode);
        assertNull(warehouse.location);
        assertNull(warehouse.createdAt);
        assertNull(warehouse.archivedAt);
    }

    @Test
    @DisplayName("Should handle zero capacity and stock")
    void testZeroCapacityAndStock() {
        DbWarehouse dbWarehouse = new DbWarehouse();
        dbWarehouse.capacity = 0;
        dbWarehouse.stock = 0;

        Warehouse warehouse = dbWarehouse.toWarehouse();

        assertEquals(0, warehouse.capacity);
        assertEquals(0, warehouse.stock);
    }

    @Test
    @DisplayName("Should preserve ID in entity but not in domain")
    void testIdHandling() {
        DbWarehouse dbWarehouse = new DbWarehouse();
        dbWarehouse.id = 999L;
        dbWarehouse.businessUnitCode = "WH-ID-TEST";

        Warehouse warehouse = dbWarehouse.toWarehouse();

        // DbWarehouse has ID
        assertEquals(999L, dbWarehouse.id);
        // Warehouse domain model doesn't copy ID (based on current implementation)
        // The domain model uses businessUnitCode as identifier
        assertEquals("WH-ID-TEST", warehouse.businessUnitCode);
    }
}

