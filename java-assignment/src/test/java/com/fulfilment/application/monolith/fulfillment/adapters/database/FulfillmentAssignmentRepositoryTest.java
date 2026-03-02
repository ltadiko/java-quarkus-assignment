package com.fulfilment.application.monolith.fulfillment.adapters.database;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FulfillmentAssignmentRepository.
 * Tests CRUD operations and query methods for fulfillment assignments.
 */
@QuarkusTest
public class FulfillmentAssignmentRepositoryTest {

    @Inject
    FulfillmentAssignmentRepository repository;

    @BeforeEach
    @Transactional
    void setUp() {
        repository.deleteAll();
    }

    // ==================== CREATE TESTS ====================

    @Test
    @Transactional
    @DisplayName("Should create fulfillment assignment successfully")
    void testCreateSuccess() {
        // Arrange
        FulfillmentAssignment assignment = new FulfillmentAssignment("WH-001", 1L, 1L);

        // Act
        FulfillmentAssignment created = repository.create(assignment);

        // Assert
        assertNotNull(created.id, "Created assignment should have an ID");
        assertEquals("WH-001", created.warehouseCode);
        assertEquals(1L, created.productId);
        assertEquals(1L, created.storeId);
        assertNotNull(created.createdAt);
    }

    @Test
    @Transactional
    @DisplayName("Should create multiple assignments")
    void testCreateMultiple() {
        // Arrange & Act
        repository.create(new FulfillmentAssignment("WH-001", 1L, 1L));
        repository.create(new FulfillmentAssignment("WH-001", 2L, 1L));
        repository.create(new FulfillmentAssignment("WH-002", 1L, 1L));

        // Assert
        List<FulfillmentAssignment> all = repository.getAll();
        assertEquals(3, all.size());
    }

    // ==================== READ TESTS ====================

    @Test
    @Transactional
    @DisplayName("Should find assignment by ID")
    void testFindAssignmentById() {
        // Arrange
        FulfillmentAssignment created = repository.create(new FulfillmentAssignment("WH-001", 1L, 1L));

        // Act
        FulfillmentAssignment found = repository.findAssignmentById(created.id);

        // Assert
        assertNotNull(found);
        assertEquals(created.id, found.id);
        assertEquals("WH-001", found.warehouseCode);
    }

    @Test
    @Transactional
    @DisplayName("Should return null for non-existent ID")
    void testFindByIdNotFound() {
        // Act
        FulfillmentAssignment found = repository.findAssignmentById(99999L);

        // Assert
        assertNull(found);
    }

    @Test
    @Transactional
    @DisplayName("Should get all assignments")
    void testGetAll() {
        // Arrange
        repository.create(new FulfillmentAssignment("WH-001", 1L, 1L));
        repository.create(new FulfillmentAssignment("WH-002", 2L, 2L));

        // Act
        List<FulfillmentAssignment> all = repository.getAll();

        // Assert
        assertEquals(2, all.size());
    }

    // ==================== QUERY TESTS ====================

    @Test
    @Transactional
    @DisplayName("Should find assignments by store ID")
    void testFindByStoreId() {
        // Arrange
        repository.create(new FulfillmentAssignment("WH-001", 1L, 1L));
        repository.create(new FulfillmentAssignment("WH-002", 2L, 1L));
        repository.create(new FulfillmentAssignment("WH-003", 3L, 2L));

        // Act
        List<FulfillmentAssignment> storeOne = repository.findByStoreId(1L);
        List<FulfillmentAssignment> storeTwo = repository.findByStoreId(2L);

        // Assert
        assertEquals(2, storeOne.size());
        assertEquals(1, storeTwo.size());
    }

    @Test
    @Transactional
    @DisplayName("Should find assignments by warehouse code")
    void testFindByWarehouseCode() {
        // Arrange
        repository.create(new FulfillmentAssignment("WH-001", 1L, 1L));
        repository.create(new FulfillmentAssignment("WH-001", 2L, 2L));
        repository.create(new FulfillmentAssignment("WH-002", 3L, 1L));

        // Act
        List<FulfillmentAssignment> whOne = repository.findByWarehouseCode("WH-001");
        List<FulfillmentAssignment> whTwo = repository.findByWarehouseCode("WH-002");

        // Assert
        assertEquals(2, whOne.size());
        assertEquals(1, whTwo.size());
    }

    @Test
    @Transactional
    @DisplayName("Should find assignments by product ID")
    void testFindByProductId() {
        // Arrange
        repository.create(new FulfillmentAssignment("WH-001", 1L, 1L));
        repository.create(new FulfillmentAssignment("WH-002", 1L, 2L));
        repository.create(new FulfillmentAssignment("WH-003", 2L, 1L));

        // Act
        List<FulfillmentAssignment> productOne = repository.findByProductId(1L);
        List<FulfillmentAssignment> productTwo = repository.findByProductId(2L);

        // Assert
        assertEquals(2, productOne.size());
        assertEquals(1, productTwo.size());
    }

    // ==================== COUNT TESTS ====================

    @Test
    @Transactional
    @DisplayName("Should count warehouses for product in store")
    void testCountWarehousesForProductInStore() {
        // Arrange
        repository.create(new FulfillmentAssignment("WH-001", 1L, 1L));
        repository.create(new FulfillmentAssignment("WH-002", 1L, 1L));
        repository.create(new FulfillmentAssignment("WH-003", 2L, 1L));

        // Act
        long count = repository.countWarehousesForProductInStore(1L, 1L);

        // Assert
        assertEquals(2, count);
    }

    @Test
    @Transactional
    @DisplayName("Should count warehouses for store")
    void testCountWarehousesForStore() {
        // Arrange
        repository.create(new FulfillmentAssignment("WH-001", 1L, 1L));
        repository.create(new FulfillmentAssignment("WH-002", 2L, 1L));
        repository.create(new FulfillmentAssignment("WH-001", 3L, 1L)); // Same warehouse, different product

        // Act
        long count = repository.countWarehousesForStore(1L);

        // Assert
        assertEquals(2, count); // Only 2 distinct warehouses
    }

    @Test
    @Transactional
    @DisplayName("Should count products in warehouse")
    void testCountProductsInWarehouse() {
        // Arrange
        repository.create(new FulfillmentAssignment("WH-001", 1L, 1L));
        repository.create(new FulfillmentAssignment("WH-001", 2L, 1L));
        repository.create(new FulfillmentAssignment("WH-001", 3L, 2L));
        repository.create(new FulfillmentAssignment("WH-001", 1L, 2L)); // Same product, different store

        // Act
        long count = repository.countProductsInWarehouse("WH-001");

        // Assert
        assertEquals(3, count); // 3 distinct products
    }

    // ==================== EXISTS TESTS ====================

    @Test
    @Transactional
    @DisplayName("Should check if assignment exists")
    void testExists() {
        // Arrange
        repository.create(new FulfillmentAssignment("WH-001", 1L, 1L));

        // Act & Assert
        assertTrue(repository.exists("WH-001", 1L, 1L));
        assertFalse(repository.exists("WH-001", 1L, 2L));
        assertFalse(repository.exists("WH-002", 1L, 1L));
    }

    // ==================== DELETE TESTS ====================

    @Test
    @Transactional
    @DisplayName("Should delete assignment by ID")
    void testRemoveById() {
        // Arrange
        FulfillmentAssignment created = repository.create(new FulfillmentAssignment("WH-001", 1L, 1L));

        // Act
        repository.removeById(created.id);

        // Assert
        assertNull(repository.findAssignmentById(created.id));
        assertEquals(0, repository.getAll().size());
    }

    @Test
    @Transactional
    @DisplayName("Should throw when deleting non-existent assignment")
    void testRemoveByIdNotFound() {
        // Act & Assert
        assertThrows(
            jakarta.persistence.EntityNotFoundException.class,
            () -> repository.removeById(99999L),
            "Should throw when assignment not found"
        );
    }
}

