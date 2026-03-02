package com.fulfilment.application.monolith.fulfillment.adapters.database;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DbFulfillmentAssignment entity.
 * Tests entity mapping and conversion to/from domain model.
 */
public class DbFulfillmentAssignmentTest {

    @Test
    @DisplayName("Should create DbFulfillmentAssignment with default constructor")
    void testDefaultConstructor() {
        DbFulfillmentAssignment db = new DbFulfillmentAssignment();

        assertNull(db.id);
        assertNull(db.warehouseCode);
        assertNull(db.productId);
        assertNull(db.storeId);
        assertNull(db.createdAt);
    }

    @Test
    @DisplayName("Should create DbFulfillmentAssignment with parameterized constructor")
    void testParameterizedConstructor() {
        DbFulfillmentAssignment db = new DbFulfillmentAssignment("WH-001", 1L, 1L);

        assertNull(db.id); // ID not set until persisted
        assertEquals("WH-001", db.warehouseCode);
        assertEquals(1L, db.productId);
        assertEquals(1L, db.storeId);
        assertNotNull(db.createdAt); // Constructor sets createdAt
    }

    @Test
    @DisplayName("Should convert to FulfillmentAssignment domain model")
    void testToFulfillmentAssignment() {
        DbFulfillmentAssignment db = new DbFulfillmentAssignment();
        db.id = 100L;
        db.warehouseCode = "MWH.001";
        db.productId = 5L;
        db.storeId = 10L;
        db.createdAt = LocalDateTime.of(2024, 3, 15, 14, 30);

        FulfillmentAssignment assignment = db.toFulfillmentAssignment();

        assertEquals(100L, assignment.id);
        assertEquals("MWH.001", assignment.warehouseCode);
        assertEquals(5L, assignment.productId);
        assertEquals(10L, assignment.storeId);
        assertEquals(LocalDateTime.of(2024, 3, 15, 14, 30), assignment.createdAt);
    }

    @Test
    @DisplayName("Should create from FulfillmentAssignment domain model")
    void testFromFulfillmentAssignment() {
        FulfillmentAssignment assignment = new FulfillmentAssignment();
        assignment.id = 50L;
        assignment.warehouseCode = "MWH.012";
        assignment.productId = 3L;
        assignment.storeId = 7L;
        assignment.createdAt = LocalDateTime.of(2024, 5, 20, 9, 0);

        DbFulfillmentAssignment db = DbFulfillmentAssignment.fromFulfillmentAssignment(assignment);

        assertEquals(50L, db.id);
        assertEquals("MWH.012", db.warehouseCode);
        assertEquals(3L, db.productId);
        assertEquals(7L, db.storeId);
        assertEquals(LocalDateTime.of(2024, 5, 20, 9, 0), db.createdAt);
    }

    @Test
    @DisplayName("Should set createdAt when null in domain model")
    void testFromFulfillmentAssignmentNullCreatedAt() {
        FulfillmentAssignment assignment = new FulfillmentAssignment("WH-NEW", 1L, 1L);
        // createdAt is null by default in domain model constructor

        DbFulfillmentAssignment db = DbFulfillmentAssignment.fromFulfillmentAssignment(assignment);

        assertNotNull(db.createdAt);
    }

    @Test
    @DisplayName("Should preserve createdAt when set in domain model")
    void testFromFulfillmentAssignmentWithCreatedAt() {
        FulfillmentAssignment assignment = new FulfillmentAssignment("WH-EXISTING", 2L, 2L);
        LocalDateTime existingTime = LocalDateTime.of(2023, 1, 1, 12, 0);
        assignment.createdAt = existingTime;

        DbFulfillmentAssignment db = DbFulfillmentAssignment.fromFulfillmentAssignment(assignment);

        assertEquals(existingTime, db.createdAt);
    }

    @Test
    @DisplayName("Should handle roundtrip conversion")
    void testRoundtripConversion() {
        // Start with db entity
        DbFulfillmentAssignment original = new DbFulfillmentAssignment("WH-ROUNDTRIP", 99L, 88L);
        original.id = 777L;

        // Convert to domain
        FulfillmentAssignment domain = original.toFulfillmentAssignment();

        // Convert back to db
        DbFulfillmentAssignment restored = DbFulfillmentAssignment.fromFulfillmentAssignment(domain);

        assertEquals(original.id, restored.id);
        assertEquals(original.warehouseCode, restored.warehouseCode);
        assertEquals(original.productId, restored.productId);
        assertEquals(original.storeId, restored.storeId);
        assertEquals(original.createdAt, restored.createdAt);
    }

    @Test
    @DisplayName("Should set all fields directly")
    void testDirectFieldAccess() {
        DbFulfillmentAssignment db = new DbFulfillmentAssignment();
        db.id = 1L;
        db.warehouseCode = "DIRECT-WH";
        db.productId = 100L;
        db.storeId = 200L;
        db.createdAt = LocalDateTime.now();

        assertEquals(1L, db.id);
        assertEquals("DIRECT-WH", db.warehouseCode);
        assertEquals(100L, db.productId);
        assertEquals(200L, db.storeId);
        assertNotNull(db.createdAt);
    }

    @Test
    @DisplayName("Should handle large IDs")
    void testLargeIds() {
        DbFulfillmentAssignment db = new DbFulfillmentAssignment("LARGE-WH", Long.MAX_VALUE, Long.MAX_VALUE);

        assertEquals(Long.MAX_VALUE, db.productId);
        assertEquals(Long.MAX_VALUE, db.storeId);
    }
}

