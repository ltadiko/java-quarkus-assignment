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
}

