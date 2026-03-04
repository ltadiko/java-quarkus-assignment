package com.fulfilment.application.monolith.warehouses.adapters.database;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WarehouseRepository CRUD operations.
 *
 * The repository is a thin persistence layer — business validations
 * (uniqueness, existence, null checks) are handled by the use case classes.
 * These tests verify only the persistence behavior.
 *
 * @see WarehouseRepository
 * @see DbWarehouse
 */
@QuarkusTest
@DisplayName("Warehouse Repository Tests")
public class WarehouseRepositoryTest {

  @Inject
  WarehouseRepository warehouseRepository;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean up test data before each test
    warehouseRepository.deleteAll();
  }

  // ==================== CREATE TESTS ====================

  @Test
  @Transactional
  @DisplayName("Should create warehouse successfully")
  void testCreateWarehouseSuccess() {
    var warehouse = createTestWarehouse("WH-001", "Main Warehouse");

    warehouseRepository.create(warehouse);

    var created = warehouseRepository.findByBusinessUnitCode("WH-001");
    assertNotNull(created, "Created warehouse should exist in database");
    assertEquals("WH-001", created.businessUnitCode);
    assertEquals("Main Warehouse", created.location);
    assertNotNull(created.createdAt, "Created timestamp should be set");
  }

  // ==================== UPDATE TESTS ====================

  @Test
  @Transactional
  @DisplayName("Should update warehouse successfully")
  void testUpdateWarehouseSuccess() {
    var original = createTestWarehouse("WH-001", "Original");
    warehouseRepository.create(original);

    var updated = createTestWarehouse("WH-001", "Updated Location");
    updated.capacity = 500;

    warehouseRepository.update(updated);

    var result = warehouseRepository.findByBusinessUnitCode("WH-001");
    assertNotNull(result);
    assertEquals("Updated Location", result.location);
    assertEquals(500, result.capacity);
  }

  @Test
  @Transactional
  @DisplayName("Should silently do nothing when updating non-existent warehouse")
  void testUpdateNonExistentWarehouseIsNoOp() {
    var warehouse = createTestWarehouse("WH-INVALID", "Test");

    // Should not throw — thin repository just logs a warning and returns
    assertDoesNotThrow(() -> warehouseRepository.update(warehouse));
  }

  // ==================== REMOVE TESTS ====================

  @Test
  @Transactional
  @DisplayName("Should soft-delete warehouse successfully")
  void testRemoveWarehouseSuccess() {
    var warehouse = createTestWarehouse("WH-001", "NYC");
    warehouseRepository.create(warehouse);

    var toRemove = createTestWarehouse("WH-001", "NYC");

    warehouseRepository.remove(toRemove);

    var removed = warehouseRepository.findByBusinessUnitCode("WH-001");
    assertNotNull(removed, "Removed warehouse should still exist (soft-delete)");
    assertNotNull(removed.archivedAt, "Archived timestamp should be set");
  }

  @Test
  @Transactional
  @DisplayName("Should silently do nothing when removing non-existent warehouse")
  void testRemoveNonExistentWarehouseIsNoOp() {
    var warehouse = createTestWarehouse("WH-INVALID", "Test");

    // Should not throw — thin repository just logs a warning and returns
    assertDoesNotThrow(() -> warehouseRepository.remove(warehouse));
  }

  // ==================== FIND BY CODE TESTS ====================

  @Test
  @Transactional
  @DisplayName("Should find warehouse by business unit code")
  void testFindByBusinessUnitCodeSuccess() {
    var warehouse = createTestWarehouse("WH-001", "Test Warehouse");
    warehouseRepository.create(warehouse);

    var found = warehouseRepository.findByBusinessUnitCode("WH-001");

    assertNotNull(found);
    assertEquals("WH-001", found.businessUnitCode);
    assertEquals("Test Warehouse", found.location);
  }

  @Test
  @DisplayName("Should return null when warehouse not found")
  void testFindByBusinessUnitCodeNotFound() {
    var found = warehouseRepository.findByBusinessUnitCode("WH-INVALID");
    assertNull(found);
  }

  @Test
  @DisplayName("Should return null when finding warehouse with null code")
  void testFindByBusinessUnitCodeWithNullReturnsNull() {
    var found = warehouseRepository.findByBusinessUnitCode(null);
    assertNull(found);
  }

  @Test
  @DisplayName("Should return null when finding warehouse with blank code")
  void testFindByBusinessUnitCodeWithBlankReturnsNull() {
    var found = warehouseRepository.findByBusinessUnitCode("");
    assertNull(found);
  }

  // ==================== INTEGRATION TESTS ====================

  @Test
  @Transactional
  @DisplayName("Should complete full CRUD lifecycle")
  void testFullCrudLifecycle() {
    var warehouse = createTestWarehouse("WH-LIFECYCLE", "Original");

    // Create
    warehouseRepository.create(warehouse);
    var created = warehouseRepository.findByBusinessUnitCode("WH-LIFECYCLE");
    assertNotNull(created);

    // Update
    var updated = createTestWarehouse("WH-LIFECYCLE", "Updated");
    updated.capacity = 300;
    warehouseRepository.update(updated);
    var afterUpdate = warehouseRepository.findByBusinessUnitCode("WH-LIFECYCLE");
    assertEquals("Updated", afterUpdate.location);
    assertEquals(300, afterUpdate.capacity);

    // Remove (soft-delete)
    warehouseRepository.remove(updated);
    var afterRemove = warehouseRepository.findByBusinessUnitCode("WH-LIFECYCLE");
    assertNotNull(afterRemove);
    assertNotNull(afterRemove.archivedAt);
  }

  @Test
  @Transactional
  @DisplayName("Should handle multiple warehouses independently")
  void testMultipleWarehousesIndependence() {
    warehouseRepository.create(createTestWarehouse("WH-001", "NYC"));
    warehouseRepository.create(createTestWarehouse("WH-002", "LA"));
    warehouseRepository.create(createTestWarehouse("WH-003", "CHI"));

    var wh1 = warehouseRepository.findByBusinessUnitCode("WH-001");
    var wh2 = warehouseRepository.findByBusinessUnitCode("WH-002");
    var wh3 = warehouseRepository.findByBusinessUnitCode("WH-003");

    assertNotNull(wh1);
    assertNotNull(wh2);
    assertNotNull(wh3);
    assertEquals("NYC", wh1.location);
    assertEquals("LA", wh2.location);
    assertEquals("CHI", wh3.location);

    var all = warehouseRepository.getAll();
    assertEquals(3, all.size());
  }

  // ==================== HELPER METHODS ====================

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
      createTestWarehouse(String code, String location) {
    var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.location = location;
    warehouse.capacity = 100;
    warehouse.stock = 0;
    return warehouse;
  }
}
