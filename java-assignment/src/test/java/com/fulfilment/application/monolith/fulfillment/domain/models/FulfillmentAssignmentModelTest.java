package com.fulfilment.application.monolith.fulfillment.domain.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FulfillmentAssignment domain model.
 */
public class FulfillmentAssignmentModelTest {

    @Test
    @DisplayName("Should create assignment with constructor")
    void testConstructorCreation() {
        FulfillmentAssignment assignment = new FulfillmentAssignment("WH-001", 1L, 1L);

        assertEquals("WH-001", assignment.warehouseCode);
        assertEquals(1L, assignment.productId);
        assertEquals(1L, assignment.storeId);
        assertNull(assignment.id);
        assertNotNull(assignment.createdAt); // Constructor sets createdAt
    }

    @Test
    @DisplayName("Should create assignment with default constructor")
    void testDefaultConstructor() {
        FulfillmentAssignment assignment = new FulfillmentAssignment();

        assertNull(assignment.id);
        assertNull(assignment.warehouseCode);
        assertNull(assignment.productId);
        assertNull(assignment.storeId);
        assertNull(assignment.createdAt);
    }

    @Test
    @DisplayName("Should set all fields")
    void testSetAllFields() {
        FulfillmentAssignment assignment = new FulfillmentAssignment();
        assignment.id = 100L;
        assignment.warehouseCode = "WH-TEST";
        assignment.productId = 5L;
        assignment.storeId = 10L;
        assignment.createdAt = LocalDateTime.now();

        assertEquals(100L, assignment.id);
        assertEquals("WH-TEST", assignment.warehouseCode);
        assertEquals(5L, assignment.productId);
        assertEquals(10L, assignment.storeId);
        assertNotNull(assignment.createdAt);
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        FulfillmentAssignment assignment = new FulfillmentAssignment(null, null, null);

        assertNull(assignment.warehouseCode);
        assertNull(assignment.productId);
        assertNull(assignment.storeId);
    }

    @Test
    @DisplayName("Should handle different warehouse codes")
    void testDifferentWarehouseCodes() {
        FulfillmentAssignment a1 = new FulfillmentAssignment("MWH.001", 1L, 1L);
        FulfillmentAssignment a2 = new FulfillmentAssignment("MWH.012", 1L, 1L);
        FulfillmentAssignment a3 = new FulfillmentAssignment("CUSTOM-WH-999", 1L, 1L);

        assertEquals("MWH.001", a1.warehouseCode);
        assertEquals("MWH.012", a2.warehouseCode);
        assertEquals("CUSTOM-WH-999", a3.warehouseCode);
    }

    @Test
    @DisplayName("Should handle large IDs")
    void testLargeIds() {
        FulfillmentAssignment assignment = new FulfillmentAssignment("WH", Long.MAX_VALUE, Long.MAX_VALUE);

        assertEquals(Long.MAX_VALUE, assignment.productId);
        assertEquals(Long.MAX_VALUE, assignment.storeId);
    }

    @Test
    @DisplayName("CreatedAt should be settable")
    void testCreatedAtSettable() {
        FulfillmentAssignment assignment = new FulfillmentAssignment("WH-001", 1L, 1L);
        LocalDateTime now = LocalDateTime.now();
        assignment.createdAt = now;

        assertEquals(now, assignment.createdAt);
    }
}

