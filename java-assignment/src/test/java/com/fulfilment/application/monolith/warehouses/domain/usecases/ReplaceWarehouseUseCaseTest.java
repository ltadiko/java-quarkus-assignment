package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ReplaceWarehouseUseCase business logic.
 *
 * Verifies that warehouse replacement correctly:
 * 1. Validates old warehouse exists and is active
 * 2. Validates new warehouse and location
 * 3. Archives the old warehouse
 * 4. Creates new warehouse with same business unit code
 * 5. Transfers stock correctly
 *
 * @see ReplaceWarehouseUseCase
 */
@QuarkusTest
@DisplayName("Replace Warehouse Use Case Tests")
public class ReplaceWarehouseUseCaseTest {

  @Inject ReplaceWarehouseUseCase replaceWarehouseUseCase;
  @Inject WarehouseRepository warehouseRepository;
  @Inject CreateWarehouseUseCase createWarehouseUseCase;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean up test data
    warehouseRepository.deleteAll();
  }

  // ==================== HAPPY PATH TEST ====================

  /**
   * Test: Replace warehouse successfully with all validations passing.
   * Expected: Old warehouse archived, new warehouse created with same code.
   */
  @Test
  @Transactional
  @DisplayName("Should replace warehouse successfully")
  void testReplaceWarehouseSuccess() {
    // Arrange - Create old warehouse using AMSTERDAM-001 (capacity 100)
    Warehouse oldWarehouse = createTestWarehouse("WH-TEST-SUCCESS", "AMSTERDAM-001", 100, 50);
    createWarehouseUseCase.create(oldWarehouse);

    // Create replacement warehouse
    Warehouse newWarehouse = createTestWarehouse("WH-TEST-SUCCESS", "AMSTERDAM-001", 150, 50);

    // Act
    replaceWarehouseUseCase.replace("WH-TEST-SUCCESS", newWarehouse);

    // Assert - Verify warehouse replacement
    Warehouse replacedWarehouse = warehouseRepository.findByBusinessUnitCode("WH-TEST-SUCCESS");
    assertNotNull(replacedWarehouse, "Warehouse should exist after replacement");
    assertEquals(150, replacedWarehouse.capacity, "Capacity should be updated");
    assertEquals(50, replacedWarehouse.stock, "Stock should match old warehouse");
    assertNull(replacedWarehouse.archivedAt, "New warehouse should not be archived");
  }

  /**
   * Test: Replace warehouse successfully and verify old warehouse is archived.
   * Expected: Old warehouse marked as archived (soft delete).
   */
  @Test
  @Transactional
  @DisplayName("Should archive old warehouse during replacement")
  void testOldWarehouseArchived() {
    // Arrange - Use AMSTERDAM-001 (capacity 100) instead of ZWOLLE-001 (capacity 40)
    Warehouse oldWarehouse = createTestWarehouse("WH-TEST-ARCHIVED", "AMSTERDAM-001", 100, 40);
    createWarehouseUseCase.create(oldWarehouse);

    Warehouse newWarehouse = createTestWarehouse("WH-TEST-ARCHIVED", "AMSTERDAM-001", 120, 40);

    // Act
    replaceWarehouseUseCase.replace("WH-TEST-ARCHIVED", newWarehouse);

    // Assert - getAll() filters out archived, so use findByBusinessUnitCode
    // After replace: old warehouse is archived, new one is active with same code
    Warehouse currentWarehouse = warehouseRepository.findByBusinessUnitCode("WH-TEST-ARCHIVED");
    assertNotNull(currentWarehouse, "New warehouse should exist after replacement");
    assertNull(currentWarehouse.archivedAt, "New active warehouse should not be archived");
    assertEquals(120, currentWarehouse.capacity, "Should have new warehouse capacity");

    // Verify old warehouse was archived by checking that getAll() doesn't contain archived entries
    // but the DB has 2 records (archived + active) for this code
    long totalWithCode = warehouseRepository.find("businessUnitCode", "WH-TEST-ARCHIVED").count();
    assertEquals(2, totalWithCode, "Should have 2 records: 1 archived + 1 active");

    long archivedCount = warehouseRepository.find("businessUnitCode = ?1 and archivedAt is not null", "WH-TEST-ARCHIVED").count();
    assertTrue(archivedCount > 0, "Old warehouse should be archived");
  }

  // ==================== ERROR TESTS ====================

  /**
   * Test: Replace warehouse when old code doesn't exist.
   * Expected: EntityNotFoundException thrown.
   */
  @Test
  @Transactional
  @DisplayName("Should throw exception when old warehouse not found")
  void testOldWarehouseNotFound() {
    // Arrange
    Warehouse newWarehouse = createTestWarehouse("WH-INVALID", "AMSTERDAM-001", 50, 50);

    // Act & Assert
    assertThrows(
        EntityNotFoundException.class,
        () -> replaceWarehouseUseCase.replace("WH-INVALID", newWarehouse),
        "Should throw EntityNotFoundException for non-existent warehouse");
  }

  /**
   * Test: Replace warehouse when old warehouse already archived.
   * Expected: IllegalStateException thrown.
   */
  @Test
  @Transactional
  @DisplayName("Should throw exception when old warehouse already archived")
  void testOldWarehouseAlreadyArchived() {
    // Arrange - Create and archive warehouse using AMSTERDAM-001 (capacity 100)
    Warehouse warehouse = createTestWarehouse("WH-TEST-ARCHIVED-CHECK", "AMSTERDAM-001", 80, 50);
    createWarehouseUseCase.create(warehouse);

    warehouse = warehouseRepository.findByBusinessUnitCode("WH-TEST-ARCHIVED-CHECK");
    warehouse.archivedAt = java.time.LocalDateTime.now();
    warehouseRepository.update(warehouse);

    Warehouse newWarehouse = createTestWarehouse("WH-TEST-ARCHIVED-CHECK", "AMSTERDAM-001", 120, 50);

    // Act & Assert
    assertThrows(
        IllegalStateException.class,
        () -> replaceWarehouseUseCase.replace("WH-TEST-ARCHIVED-CHECK", newWarehouse),
        "Should throw IllegalStateException for archived warehouse");
  }

  /**
   * Test: Replace warehouse with invalid new location.
   * Expected: EntityNotFoundException thrown.
   */
  @Test
  @Transactional
  @DisplayName("Should throw exception when new location doesn't exist")
  void testNewLocationNotFound() {
    // Arrange - Create old warehouse
    Warehouse oldWarehouse = createTestWarehouse("WH-TEST-LOCATION", "AMSTERDAM-001", 80, 50);
    createWarehouseUseCase.create(oldWarehouse);

    Warehouse newWarehouse =
        createTestWarehouse("WH-TEST-LOCATION", "INVALID-LOCATION", 80, 50);

    // Act & Assert
    assertThrows(
        EntityNotFoundException.class,
        () -> replaceWarehouseUseCase.replace("WH-TEST-LOCATION", newWarehouse),
        "Should throw EntityNotFoundException for invalid location");
  }

  /**
   * Test: Replace warehouse with insufficient new capacity.
   * Expected: IllegalStateException thrown.
   */
  @Test
  @Transactional
  @DisplayName("Should throw exception when new capacity insufficient")
  void testInsufficientCapacity() {
    // Arrange - Create old warehouse with stock
    Warehouse oldWarehouse = createTestWarehouse("WH-ZWOLLE-001", "ZWOLLE-001", 30, 25);
    createWarehouseUseCase.create(oldWarehouse);

    // New capacity is less than old stock
    Warehouse newWarehouse = createTestWarehouse("WH-ZWOLLE-001", "ZWOLLE-001", 20, 25);

    // Act & Assert
    assertThrows(
        IllegalStateException.class,
        () -> replaceWarehouseUseCase.replace("WH-ZWOLLE-001", newWarehouse),
        "Should throw IllegalStateException for insufficient capacity");
  }

  /**
   * Test: Replace warehouse with mismatched stock.
   * Expected: IllegalStateException thrown.
   */
  @Test
  @Transactional
  @DisplayName("Should throw exception when stock doesn't match")
  void testStockMismatch() {
    // Arrange - Use AMSTERDAM-001 for larger warehouse
    Warehouse oldWarehouse = createTestWarehouse("WH-TEST-STOCK", "AMSTERDAM-001", 80, 50);
    createWarehouseUseCase.create(oldWarehouse);

    // New warehouse has different stock
    Warehouse newWarehouse = createTestWarehouse("WH-TEST-STOCK", "AMSTERDAM-001", 80, 30);

    // Act & Assert
    assertThrows(
        IllegalStateException.class,
        () -> replaceWarehouseUseCase.replace("WH-TEST-STOCK", newWarehouse),
        "Should throw IllegalStateException for stock mismatch");
  }

  // ==================== EDGE CASE TESTS ====================

  /**
   * Test: Replace warehouse when new stock not provided.
   * Expected: Stock auto-set from old warehouse.
   */
  @Test
  @Transactional
  @DisplayName("Should auto-set stock from old warehouse if not provided")
  void testStockAutoSet() {
    // Arrange - Use AMSTERDAM-001 (capacity 100)
    Warehouse oldWarehouse = createTestWarehouse("WH-TEST-AUTOSET", "AMSTERDAM-001", 80, 60);
    createWarehouseUseCase.create(oldWarehouse);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.capacity = 150;
    newWarehouse.stock = null; // Not provided

    // Act
    replaceWarehouseUseCase.replace("WH-TEST-AUTOSET", newWarehouse);

    // Assert
    Warehouse replaced = warehouseRepository.findByBusinessUnitCode("WH-TEST-AUTOSET");
    assertEquals(60, replaced.stock, "Stock should be auto-set from old warehouse");
  }

  /**
   * Test: Replace warehouse with much larger capacity.
   * Expected: Replacement succeeds.
   */
  @Test
  @Transactional
  @DisplayName("Should replace with larger capacity warehouse")
  void testReplaceWithLargerCapacity() {
    // Arrange - Use AMSTERDAM-001 (capacity 100) and upgrade to VETSBY-001 (capacity 90)
    Warehouse oldWarehouse = createTestWarehouse("WH-TEST-LARGE", "AMSTERDAM-001", 50, 40);
    createWarehouseUseCase.create(oldWarehouse);

    // Use VETSBY-001 which has higher capacity
    Warehouse newWarehouse = createTestWarehouse("WH-TEST-LARGE", "VETSBY-001", 80, 40);

    // Act
    replaceWarehouseUseCase.replace("WH-TEST-LARGE", newWarehouse);

    // Assert
    Warehouse replaced = warehouseRepository.findByBusinessUnitCode("WH-TEST-LARGE");
    assertEquals(80, replaced.capacity, "New warehouse should have larger capacity");
    assertEquals("VETSBY-001", replaced.location, "Location should be updated");
  }

  // ==================== HELPER METHODS ====================

  /**
   * Helper to create a test warehouse.
   *
   * @param code business unit code
   * @param location warehouse location
   * @param capacity warehouse capacity
   * @param stock warehouse stock
   * @return warehouse object with test data
   */
  private Warehouse createTestWarehouse(String code, String location, Integer capacity, Integer stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }
}
