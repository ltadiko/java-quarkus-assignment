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
 * Tests for CreateWarehouseUseCase business logic.
 *
 * Verifies that warehouse creation correctly validates:
 * 1. Business unit code uniqueness
 * 2. Location existence
 * 3. Maximum warehouses per location
 * 4. Location capacity constraints
 * 5. Warehouse capacity vs stock
 *
 * @see CreateWarehouseUseCase
 */
@QuarkusTest
@DisplayName("Create Warehouse Use Case Tests")
public class CreateWarehouseUseCaseTest {

  @Inject CreateWarehouseUseCase createWarehouseUseCase;
  @Inject WarehouseRepository warehouseRepository;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean up test data
    warehouseRepository.deleteAll();
  }

  // ==================== HAPPY PATH TEST ====================

  /**
   * Test: Create warehouse successfully with all validations passing.
   * Expected: Warehouse created and persisted.
   */
  @Test
  @Transactional
  @DisplayName("Should create warehouse successfully")
  void testCreateWarehouseSuccess() {
    // Arrange
    Warehouse warehouse = createTestWarehouse("WH-ZWOLLE-001", "ZWOLLE-001", 30, 15);

    // Act
    createWarehouseUseCase.create(warehouse);

    // Assert - Verify persistence
    Warehouse created = warehouseRepository.findByBusinessUnitCode("WH-ZWOLLE-001");
    assertNotNull(created, "Warehouse should be created in database");
    assertEquals("WH-ZWOLLE-001", created.businessUnitCode);
    assertEquals("ZWOLLE-001", created.location);
    assertEquals(30, created.capacity);
    assertEquals(15, created.stock);
  }

  // ==================== BUSINESS UNIT CODE TESTS ====================

  /**
   * Test: Create warehouse with duplicate business unit code.
   * Expected: IllegalStateException thrown.
   */
  @Test
  @Transactional
  @DisplayName("Should throw exception when code already exists")
  void testCreateWithDuplicateCodeThrows() {
    // Arrange
    Warehouse first = createTestWarehouse("WH-ZWOLLE-001", "ZWOLLE-001", 30, 15);
    Warehouse second = createTestWarehouse("WH-ZWOLLE-001", "ZWOLLE-002", 30, 15);

    createWarehouseUseCase.create(first);

    // Act & Assert
    assertThrows(
        IllegalStateException.class,
        () -> createWarehouseUseCase.create(second),
        "Creating duplicate warehouse code should throw IllegalStateException");
  }

  /**
   * Test: Create warehouse with null code.
   * Expected: IllegalArgumentException thrown.
   */
  @Test
  @Transactional
  @DisplayName("Should throw exception when code is null")
  void testCreateWithNullCodeThrows() {
    // Arrange
    Warehouse warehouse = createTestWarehouse(null, "ZWOLLE-001", 30, 15);

    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> createWarehouseUseCase.create(warehouse),
        "Creating warehouse with null code should throw IllegalArgumentException");
  }

  /**
   * Test: Create warehouse with blank code.
   * Expected: IllegalArgumentException thrown.
   */
  @Test
  @Transactional
  @DisplayName("Should throw exception when code is blank")
  void testCreateWithBlankCodeThrows() {
    // Arrange
    Warehouse warehouse = createTestWarehouse("   ", "ZWOLLE-001", 30, 15);

    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> createWarehouseUseCase.create(warehouse),
        "Creating warehouse with blank code should throw IllegalArgumentException");
  }

  // ==================== LOCATION VALIDATION TESTS ====================

  /**
   * Test: Create warehouse with non-existent location.
   * Expected: EntityNotFoundException thrown.
   */
  @Test
  @Transactional
  @DisplayName("Should throw exception when location doesn't exist")
  void testCreateWithInvalidLocationThrows() {
    // Arrange
    Warehouse warehouse = createTestWarehouse("WH-INVALID-001", "INVALID-LOCATION", 100, 50);

    // Act & Assert
    assertThrows(
        EntityNotFoundException.class,
        () -> createWarehouseUseCase.create(warehouse),
        "Creating warehouse with invalid location should throw EntityNotFoundException");
  }

  /**
   * Test: Create warehouse with null location.
   * Expected: IllegalArgumentException thrown.
   */
  @Test
  @Transactional
  @DisplayName("Should throw exception when location is null")
  void testCreateWithNullLocationThrows() {
    // Arrange
    Warehouse warehouse = createTestWarehouse("WH-TEST-001", null, 100, 50);

    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> createWarehouseUseCase.create(warehouse),
        "Creating warehouse with null location should throw IllegalArgumentException");
  }

  // ==================== WAREHOUSE FEASIBILITY TESTS ====================

  /**
   * Test: Create warehouse when maximum warehouses per location is reached.
   * Expected: IllegalStateException thrown.
   */
  @Test
  @Transactional
  @DisplayName("Should throw exception when max warehouses reached")
  void testCreateWhenMaxWarehousesReachedThrows() {
    // Arrange - ZWOLLE-001 has max 1 warehouse
    Warehouse wh1 = createTestWarehouse("WH-ZWOLLE-001", "ZWOLLE-001", 30, 15);
    Warehouse wh2 = createTestWarehouse("WH-ZWOLLE-002", "ZWOLLE-001", 30, 15);

    createWarehouseUseCase.create(wh1);

    // Act & Assert
    assertThrows(
        IllegalStateException.class,
        () -> createWarehouseUseCase.create(wh2),
        "Creating warehouse beyond location limit should throw IllegalStateException");
  }

  // ==================== LOCATION CAPACITY TESTS ====================

  /**
   * Test: Create warehouse that exceeds location's total capacity.
   * Expected: IllegalStateException thrown.
   */
  @Test
  @Transactional
  @DisplayName("Should throw exception when exceeds location capacity")
  void testCreateWhenExceedsLocationCapacityThrows() {
    // Arrange - AMSTERDAM-001 has max capacity 100
    // Create warehouse that uses 80 capacity
    Warehouse wh1 = createTestWarehouse("WH-AMSTERDAM-001", "AMSTERDAM-001", 80, 40);
    // Try to create warehouse that would exceed (80 + 50 > 100)
    Warehouse wh2 = createTestWarehouse("WH-AMSTERDAM-002", "AMSTERDAM-001", 50, 25);

    createWarehouseUseCase.create(wh1);

    // Act & Assert
    assertThrows(
        IllegalStateException.class,
        () -> createWarehouseUseCase.create(wh2),
        "Creating warehouse exceeding location capacity should throw IllegalStateException");
  }

  // ==================== WAREHOUSE CAPACITY TESTS ====================

  /**
   * Test: Create warehouse with capacity less than stock.
   * Expected: IllegalStateException thrown.
   */
  @Test
  @Transactional
  @DisplayName("Should throw exception when capacity insufficient for stock")
  void testCreateWhenCapacityInsufficientThrows() {
    // Arrange
    Warehouse warehouse = createTestWarehouse("WH-TILBURG-001", "TILBURG-001", 50, 100); // capacity < stock

    // Act & Assert
    assertThrows(
        IllegalStateException.class,
        () -> createWarehouseUseCase.create(warehouse),
        "Creating warehouse with insufficient capacity should throw IllegalStateException");
  }

  /**
   * Test: Create warehouse with zero or negative capacity.
   * Expected: IllegalArgumentException thrown.
   */
  @Test
  @Transactional
  @DisplayName("Should throw exception when capacity is negative")
  void testCreateWithNegativeCapacityThrows() {
    // Arrange
    Warehouse warehouse = createTestWarehouse("WH-HELMOND-001", "HELMOND-001", -50, 0);

    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> createWarehouseUseCase.create(warehouse),
        "Creating warehouse with negative capacity should throw IllegalArgumentException");
  }

  /**
   * Test: Create warehouse with null capacity.
   * Expected: IllegalArgumentException thrown.
   */
  @Test
  @Transactional
  @DisplayName("Should throw exception when capacity is null")
  void testCreateWithNullCapacityThrows() {
    // Arrange
    Warehouse warehouse = createTestWarehouse("WH-EINDHOVEN-001", "EINDHOVEN-001", null, 0);

    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> createWarehouseUseCase.create(warehouse),
        "Creating warehouse with null capacity should throw IllegalArgumentException");
  }

  // ==================== INTEGRATION TESTS ====================

  /**
   * Test: Complete warehouse creation lifecycle with multiple warehouses.
   * Expected: All warehouses created successfully, respecting all constraints.
   */
  @Test
  @Transactional
  @DisplayName("Should create multiple warehouses respecting all constraints")
  void testCreateMultipleWarehousesFullCycle() {
    // Arrange - Use AMSTERDAM-001 which allows up to 5 warehouses
    Warehouse wh1 = createTestWarehouse("WH-AMSTERDAM-001", "AMSTERDAM-001", 20, 10);
    Warehouse wh2 = createTestWarehouse("WH-AMSTERDAM-002", "AMSTERDAM-001", 20, 10);
    Warehouse wh3 = createTestWarehouse("WH-AMSTERDAM-003", "AMSTERDAM-001", 20, 10);

    // Act
    createWarehouseUseCase.create(wh1);
    createWarehouseUseCase.create(wh2);
    createWarehouseUseCase.create(wh3);

    // Assert
    Warehouse created1 = warehouseRepository.findByBusinessUnitCode("WH-AMSTERDAM-001");
    Warehouse created2 = warehouseRepository.findByBusinessUnitCode("WH-AMSTERDAM-002");
    Warehouse created3 = warehouseRepository.findByBusinessUnitCode("WH-AMSTERDAM-003");

    assertNotNull(created1);
    assertNotNull(created2);
    assertNotNull(created3);
    assertEquals(20, created1.capacity);
    assertEquals(20, created2.capacity);
    assertEquals(20, created3.capacity);
  }

  /**
   * Test: Create warehouse with maximum allowed capacity and stock.
   * Expected: Warehouse created successfully.
   */
  @Test
  @Transactional
  @DisplayName("Should create warehouse with capacity equals stock")
  void testCreateWarehouseWithCapacityEqualsStock() {
    // Arrange
    Warehouse warehouse = createTestWarehouse("WH-VETSBY-001", "VETSBY-001", 80, 80);

    // Act
    createWarehouseUseCase.create(warehouse);

    // Assert
    Warehouse created = warehouseRepository.findByBusinessUnitCode("WH-VETSBY-001");
    assertNotNull(created);
    assertEquals(80, created.capacity);
    assertEquals(80, created.stock);
  }

  // ==================== HELPER METHODS ====================

  /**
   * Helper to create a test warehouse with specified parameters.
   *
   * @param code business unit code
   * @param location location identifier
   * @param capacity warehouse capacity
   * @param stock initial stock
   * @return warehouse object with test data
   */
  private Warehouse createTestWarehouse(
      String code, String location, Integer capacity, Integer stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = code;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }
}

