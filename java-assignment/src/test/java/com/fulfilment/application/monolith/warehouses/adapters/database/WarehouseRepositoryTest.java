package com.fulfilment.application.monolith.warehouses.adapters.database;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WarehouseRepository CRUD operations.
 *
 * Verifies that the repository correctly implements create, update, remove,
 * and find operations with proper validation and error handling.
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

  /**
   * Test: Create warehouse with valid data successfully.
   * Expected: Warehouse persisted with generated ID and createdAt timestamp.
   */
  @Test
  @Transactional
  @DisplayName("Should create warehouse successfully")
  void testCreateWarehouseSuccess() {
    // Arrange
    var warehouse = createTestWarehouse("WH-001", "Main Warehouse");

    // Act
    warehouseRepository.create(warehouse);

    // Assert - Verify persistence
    var created = warehouseRepository.findByBusinessUnitCode("WH-001");
    assertNotNull(created, "Created warehouse should exist in database");
    assertEquals("WH-001", created.businessUnitCode, "Business unit code should match");
    assertEquals("Main Warehouse", created.location, "Location should match");
    assertNotNull(created.createdAt, "Created timestamp should be set");
  }

  /**
   * Test: Create warehouse with null throws exception.
   * Expected: IllegalArgumentException thrown.
   */
  @Test
  @DisplayName("Should throw exception when creating null warehouse")
  void testCreateNullWarehouseThrowsException() {
    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> warehouseRepository.create(null),
        "Creating null warehouse should throw IllegalArgumentException");
  }

  /**
   * Test: Create warehouse with blank business unit code throws exception.
   * Expected: IllegalArgumentException thrown.
   */
  @Test
  @DisplayName("Should throw exception when creating warehouse with blank code")
  void testCreateWarehouseWithBlankCodeThrowsException() {
    // Arrange
    var warehouse = createTestWarehouse("", "Test", "Test");

    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> warehouseRepository.create(warehouse),
        "Creating warehouse with blank code should throw IllegalArgumentException");
  }

  /**
   * Test: Create warehouse with duplicate code throws exception.
   * Expected: IllegalStateException thrown.
   */
  @Test
  @Transactional
  @DisplayName("Should throw exception when creating warehouse with duplicate code")
  void testCreateWarehouseWithDuplicateCodeThrowsException() {
    // Arrange
    var warehouse1 = createTestWarehouse("WH-001", "First", "NYC");
    var warehouse2 = createTestWarehouse("WH-001", "Second", "LA");

    warehouseRepository.create(warehouse1);

    // Act & Assert
    assertThrows(
        IllegalStateException.class,
        () -> warehouseRepository.create(warehouse2),
        "Creating duplicate warehouse code should throw IllegalStateException");
  }

  // ==================== UPDATE TESTS ====================

  /**
   * Test: Update warehouse with valid data successfully.
   * Expected: Warehouse fields updated in database.
   */
  @Test
  @Transactional
  @DisplayName("Should update warehouse successfully")
  void testUpdateWarehouseSuccess() {
    // Arrange
    var original = createTestWarehouse("WH-001", "Original");
    warehouseRepository.create(original);

    var updated = createTestWarehouse("WH-001", "Updated Location");
    updated.capacity = 500;

    // Act
    warehouseRepository.update(updated);

    // Assert - Verify update
    var result = warehouseRepository.findByBusinessUnitCode("WH-001");
    assertNotNull(result, "Updated warehouse should exist");
    assertEquals("Updated Location", result.location, "Location should be updated");
    assertEquals(500, result.capacity, "Capacity should be updated");
  }

  /**
   * Test: Update null warehouse throws exception.
   * Expected: IllegalArgumentException thrown.
   */
  @Test
  @DisplayName("Should throw exception when updating null warehouse")
  void testUpdateNullWarehouseThrowsException() {
    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> warehouseRepository.update(null),
        "Updating null warehouse should throw IllegalArgumentException");
  }

  /**
   * Test: Update warehouse with blank code throws exception.
   * Expected: IllegalArgumentException thrown.
   */
  @Test
  @DisplayName("Should throw exception when updating warehouse with blank code")
  void testUpdateWarehouseWithBlankCodeThrowsException() {
    // Arrange
    var warehouse = createTestWarehouse("", "Test", "Test");

    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> warehouseRepository.update(warehouse),
        "Updating warehouse with blank code should throw IllegalArgumentException");
  }

  /**
   * Test: Update non-existent warehouse throws exception.
   * Expected: EntityNotFoundException thrown.
   */
  @Test
  @DisplayName("Should throw exception when updating non-existent warehouse")
  void testUpdateNonExistentWarehouseThrowsException() {
    // Arrange
    var warehouse = createTestWarehouse("WH-INVALID", "Test", "Test");

    // Act & Assert
    assertThrows(
        EntityNotFoundException.class,
        () -> warehouseRepository.update(warehouse),
        "Updating non-existent warehouse should throw EntityNotFoundException");
  }

  // ==================== REMOVE TESTS ====================

  /**
   * Test: Remove warehouse with valid data successfully.
   * Expected: Warehouse marked as archived (archivedAt set).
   */
  @Test
  @Transactional
  @DisplayName("Should soft-delete warehouse successfully")
  void testRemoveWarehouseSuccess() {
    // Arrange
    var warehouse = createTestWarehouse("WH-001", "Test", "NYC");
    warehouseRepository.create(warehouse);

    var toRemove = createTestWarehouse("WH-001", "Test", "NYC");

    // Act
    warehouseRepository.remove(toRemove);

    // Assert - Verify soft-delete
    var removed = warehouseRepository.findByBusinessUnitCode("WH-001");
    assertNotNull(removed, "Removed warehouse should still exist (soft-delete)");
    assertNotNull(removed.archivedAt, "Archived timestamp should be set");
  }

  /**
   * Test: Remove null warehouse throws exception.
   * Expected: IllegalArgumentException thrown.
   */
  @Test
  @DisplayName("Should throw exception when removing null warehouse")
  void testRemoveNullWarehouseThrowsException() {
    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> warehouseRepository.remove(null),
        "Removing null warehouse should throw IllegalArgumentException");
  }

  /**
   * Test: Remove warehouse with blank code throws exception.
   * Expected: IllegalArgumentException thrown.
   */
  @Test
  @DisplayName("Should throw exception when removing warehouse with blank code")
  void testRemoveWarehouseWithBlankCodeThrowsException() {
    // Arrange
    var warehouse = createTestWarehouse("", "Test", "Test");

    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> warehouseRepository.remove(warehouse),
        "Removing warehouse with blank code should throw IllegalArgumentException");
  }

  /**
   * Test: Remove non-existent warehouse throws exception.
   * Expected: EntityNotFoundException thrown.
   */
  @Test
  @DisplayName("Should throw exception when removing non-existent warehouse")
  void testRemoveNonExistentWarehouseThrowsException() {
    // Arrange
    var warehouse = createTestWarehouse("WH-INVALID", "Test", "Test");

    // Act & Assert
    assertThrows(
        EntityNotFoundException.class,
        () -> warehouseRepository.remove(warehouse),
        "Removing non-existent warehouse should throw EntityNotFoundException");
  }

  // ==================== FIND BY CODE TESTS ====================

  /**
   * Test: Find warehouse by business unit code successfully.
   * Expected: Correct warehouse returned.
   */
  @Test
  @Transactional
  @DisplayName("Should find warehouse by business unit code")
  void testFindByBusinessUnitCodeSuccess() {
    // Arrange
    var warehouse = createTestWarehouse("WH-001", "Test Warehouse");
    warehouseRepository.create(warehouse);

    // Act
    var found = warehouseRepository.findByBusinessUnitCode("WH-001");

    // Assert
    assertNotNull(found, "Warehouse should be found");
    assertEquals("WH-001", found.businessUnitCode, "Business unit code should match");
    assertEquals("Test Warehouse", found.location, "Location should match");
  }

  /**
   * Test: Find warehouse with non-existent code returns null.
   * Expected: null returned (no exception).
   */
  @Test
  @DisplayName("Should return null when warehouse not found")
  void testFindByBusinessUnitCodeNotFound() {
    // Act
    var found = warehouseRepository.findByBusinessUnitCode("WH-INVALID");

    // Assert
    assertNull(found, "Should return null when warehouse not found");
  }

  /**
   * Test: Find warehouse with null code throws exception.
   * Expected: IllegalArgumentException thrown.
   */
  @Test
  @DisplayName("Should throw exception when finding warehouse with null code")
  void testFindByBusinessUnitCodeWithNullThrowsException() {
    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> warehouseRepository.findByBusinessUnitCode(null),
        "Finding warehouse with null code should throw IllegalArgumentException");
  }

  /**
   * Test: Find warehouse with blank code throws exception.
   * Expected: IllegalArgumentException thrown.
   */
  @Test
  @DisplayName("Should throw exception when finding warehouse with blank code")
  void testFindByBusinessUnitCodeWithBlankThrowsException() {
    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> warehouseRepository.findByBusinessUnitCode(""),
        "Finding warehouse with blank code should throw IllegalArgumentException");
  }

  // ==================== INTEGRATION TESTS ====================

  /**
   * Test: Complete CRUD lifecycle (create, find, update, remove).
   * Expected: All operations succeed sequentially.
   */
  @Test
  @Transactional
  @DisplayName("Should complete full CRUD lifecycle")
  void testFullCrudLifecycle() {
    // Arrange
    var warehouse = createTestWarehouse("WH-LIFECYCLE", "Original");

    // Act & Assert - Create
    warehouseRepository.create(warehouse);
    var created = warehouseRepository.findByBusinessUnitCode("WH-LIFECYCLE");
    assertNotNull(created, "Warehouse should be created");

    // Act & Assert - Update
    var updated = createTestWarehouse("WH-LIFECYCLE", "Updated");
    updated.capacity = 300;
    warehouseRepository.update(updated);
    var afterUpdate = warehouseRepository.findByBusinessUnitCode("WH-LIFECYCLE");
    assertEquals("Updated", afterUpdate.location, "Location should be updated");
    assertEquals(300, afterUpdate.capacity, "Capacity should be updated");

    // Act & Assert - Remove
    warehouseRepository.remove(updated);
    var afterRemove = warehouseRepository.findByBusinessUnitCode("WH-LIFECYCLE");
    assertNotNull(afterRemove, "Removed warehouse should still exist (soft-delete)");
    assertNotNull(afterRemove.archivedAt, "Warehouse should be marked as archived");
  }

  /**
   * Test: Multiple warehouses can be created and retrieved.
   * Expected: All warehouses can be created and found independently.
   */
  @Test
  @Transactional
  @DisplayName("Should handle multiple warehouses independently")
  void testMultipleWarehousesIndependence() {
    // Arrange & Act
    warehouseRepository.create(createTestWarehouse("WH-001", "NYC", "NYC"));
    warehouseRepository.create(createTestWarehouse("WH-002", "LA", "LA"));
    warehouseRepository.create(createTestWarehouse("WH-003", "CHI", "Chicago"));

    // Assert
    var wh1 = warehouseRepository.findByBusinessUnitCode("WH-001");
    var wh2 = warehouseRepository.findByBusinessUnitCode("WH-002");
    var wh3 = warehouseRepository.findByBusinessUnitCode("WH-003");

    assertNotNull(wh1);
    assertNotNull(wh2);
    assertNotNull(wh3);
    assertEquals("NYC", wh1.location);
    assertEquals("LA", wh2.location);
    assertEquals("CHI", wh3.location);

    // Verify getAll returns all warehouses
    var all = warehouseRepository.getAll();
    assertEquals(3, all.size(), "Should have 3 warehouses");
  }

  // ==================== HELPER METHODS ====================

  /**
   * Helper to create a test warehouse.
   *
   * @param code business unit code
   * @param location warehouse location
   * @return warehouse object with test data
   */
  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
      createTestWarehouse(String code, String location) {
    var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.location = location;
    warehouse.capacity = 100;
    warehouse.stock = 0;
    return warehouse;
  }

  /**
   * Helper to create a test warehouse (overloaded for compatibility).
   * The name parameter is ignored; location is the second parameter.
   *
   * @param code business unit code
   * @param name warehouse location (used as location field)
   * @param location unused parameter (for backward compatibility)
   * @return warehouse object with test data
   */
  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse
      createTestWarehouse(String code, String name, String location) {
    return createTestWarehouse(code, name);
  }
}

