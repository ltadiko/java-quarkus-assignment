package com.fulfilment.application.monolith.warehouses.domain.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Location domain model.
 * Tests the Location POJO and its fields.
 */
public class LocationModelTest {

    @Test
    @DisplayName("Should create location with all fields")
    void testLocationCreation() {
        Location location = new Location("AMSTERDAM-001", 5, 200);

        assertEquals("AMSTERDAM-001", location.identification);
        assertEquals(5, location.maxNumberOfWarehouses);
        assertEquals(200, location.maxCapacity);
    }

    @Test
    @DisplayName("Should create location with different values")
    void testLocationVariants() {
        Location location1 = new Location("ZWOLLE-001", 3, 150);
        Location location2 = new Location("TILBURG-001", 4, 180);

        assertNotEquals(location1.identification, location2.identification);
        assertNotEquals(location1.maxNumberOfWarehouses, location2.maxNumberOfWarehouses);
        assertNotEquals(location1.maxCapacity, location2.maxCapacity);
    }

    @Test
    @DisplayName("Should throw exception for zero max warehouses")
    void testZeroMaxWarehousesThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Location("ZERO-WH", 0, 100)
        );
    }

    @Test
    @DisplayName("Should throw exception for zero max capacity")
    void testZeroMaxCapacityThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Location("ZERO-CAP", 5, 0)
        );
    }

    @Test
    @DisplayName("Should handle large values")
    void testLargeValues() {
        Location location = new Location("LARGE", Integer.MAX_VALUE, Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, location.maxNumberOfWarehouses);
        assertEquals(Integer.MAX_VALUE, location.maxCapacity);
    }

    @Test
    @DisplayName("Should throw exception for empty identification")
    void testEmptyIdentificationThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Location("", 1, 100)
        );
    }

    @Test
    @DisplayName("Should throw exception for null identification")
    void testNullIdentificationThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Location(null, 1, 100)
        );
    }

    @Test
    @DisplayName("Should handle special characters in identification")
    void testSpecialCharactersIdentification() {
        Location location = new Location("NYC-WAREHOUSE_01.v2", 2, 50);

        assertEquals("NYC-WAREHOUSE_01.v2", location.identification);
    }

    @Test
    @DisplayName("Fields should be accessible")
    void testFieldAccess() {
        Location location = new Location("TEST", 10, 500);

        // All fields should be public and accessible
        String id = location.identification;
        int maxWh = location.maxNumberOfWarehouses;
        int maxCap = location.maxCapacity;

        assertNotNull(id);
        assertTrue(maxWh > 0);
        assertTrue(maxCap > 0);
    }

    // ==================== canAddWarehouse TESTS ====================

    @Test
    @DisplayName("Should allow adding warehouse when below limit")
    void testCanAddWarehouseBelowLimit() {
        Location location = new Location("TEST", 3, 100);
        assertTrue(location.canAddWarehouse(0));
        assertTrue(location.canAddWarehouse(1));
        assertTrue(location.canAddWarehouse(2));
    }

    @Test
    @DisplayName("Should not allow adding warehouse when at limit")
    void testCanAddWarehouseAtLimit() {
        Location location = new Location("TEST", 3, 100);
        assertFalse(location.canAddWarehouse(3));
        assertFalse(location.canAddWarehouse(4));
    }

    // ==================== isCapacityValid TESTS ====================

    @Test
    @DisplayName("Should validate capacity within limits")
    void testIsCapacityValidWithinLimits() {
        Location location = new Location("TEST", 1, 100);
        assertTrue(location.isCapacityValid(1));
        assertTrue(location.isCapacityValid(50));
        assertTrue(location.isCapacityValid(100));
    }

    @Test
    @DisplayName("Should reject invalid capacity")
    void testIsCapacityValidOutOfLimits() {
        Location location = new Location("TEST", 1, 100);
        assertFalse(location.isCapacityValid(0));
        assertFalse(location.isCapacityValid(-1));
        assertFalse(location.isCapacityValid(101));
    }

    // ==================== equals / hashCode / toString TESTS ====================

    @Test
    @DisplayName("Should be equal when same identification")
    void testEqualsWithSameIdentification() {
        Location loc1 = new Location("SAME-ID", 1, 50);
        Location loc2 = new Location("SAME-ID", 5, 200);
        assertEquals(loc1, loc2);
        assertEquals(loc1.hashCode(), loc2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal when different identification")
    void testEqualsWithDifferentIdentification() {
        Location loc1 = new Location("ID-A", 1, 50);
        Location loc2 = new Location("ID-B", 1, 50);
        assertNotEquals(loc1, loc2);
    }

    @Test
    @DisplayName("Should be equal to itself")
    void testEqualsSameInstance() {
        Location loc = new Location("SELF", 1, 50);
        assertEquals(loc, loc);
    }

    @Test
    @DisplayName("Should not be equal to null or different type")
    void testEqualsNullAndDifferentType() {
        Location loc = new Location("TEST", 1, 50);
        assertNotEquals(null, loc);
        assertNotEquals("a string", loc);
    }

    @Test
    @DisplayName("toString should contain identification")
    void testToString() {
        Location loc = new Location("AMSTERDAM-001", 5, 100);
        String str = loc.toString();
        assertTrue(str.contains("AMSTERDAM-001"));
        assertTrue(str.contains("5"));
        assertTrue(str.contains("100"));
    }

    // ==================== Constructor negative value TESTS ====================

    @Test
    @DisplayName("Should throw exception for negative max warehouses")
    void testNegativeMaxWarehousesThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Location("NEG-WH", -1, 100)
        );
    }

    @Test
    @DisplayName("Should throw exception for negative max capacity")
    void testNegativeMaxCapacityThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            new Location("NEG-CAP", 5, -10)
        );
    }
}
