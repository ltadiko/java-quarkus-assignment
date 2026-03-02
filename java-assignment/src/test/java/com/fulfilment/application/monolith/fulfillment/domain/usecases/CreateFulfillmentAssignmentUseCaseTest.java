package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import com.fulfilment.application.monolith.fulfillment.adapters.database.FulfillmentAssignmentRepository;
import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CreateFulfillmentAssignmentUseCase.
 *
 * Validates the business rules:
 * - Each Product can be fulfilled by max 2 different Warehouses per Store
 * - Each Store can be fulfilled by max 3 different Warehouses
 * - Each Warehouse can store max 5 types of Products
 */
@QuarkusTest
public class CreateFulfillmentAssignmentUseCaseTest {

    @Inject
    CreateFulfillmentAssignmentUseCase useCase;

    @Inject
    FulfillmentAssignmentRepository repository;

    @Inject
    WarehouseStore warehouseStore;

    @Inject
    ProductRepository productRepository;

    private Long testStoreId;
    private Long testProductId1;
    private Long testProductId2;
    private Long testProductId3;
    private String testWarehouseCode1;
    private String testWarehouseCode2;
    private String testWarehouseCode3;
    private String testWarehouseCode4;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clear existing assignments
        repository.deleteAll();

        // Set up test data - using existing data from import.sql
        testStoreId = 1L; // TONSTAD store
        testProductId1 = 1L; // TONSTAD product
        testProductId2 = 2L; // KALLAX product
        testProductId3 = 3L; // BEST product

        // Using existing warehouses from import.sql
        testWarehouseCode1 = "MWH.001"; // ZWOLLE-001
        testWarehouseCode2 = "MWH.012"; // AMSTERDAM-001
        testWarehouseCode3 = "MWH.023"; // TILBURG-001
    }

    // ==================== SUCCESS TESTS ====================

    @Test
    @Transactional
    @DisplayName("Should create fulfillment assignment successfully")
    void testCreateAssignmentSuccess() {
        // Act
        FulfillmentAssignment assignment = useCase.create(testWarehouseCode1, testProductId1, testStoreId);

        // Assert
        assertNotNull(assignment.id, "Assignment should have an ID");
        assertEquals(testWarehouseCode1, assignment.warehouseCode);
        assertEquals(testProductId1, assignment.productId);
        assertEquals(testStoreId, assignment.storeId);
        assertNotNull(assignment.createdAt);
    }

    @Test
    @Transactional
    @DisplayName("Should allow same warehouse to fulfill multiple products for same store")
    void testSameWarehouseMultipleProducts() {
        // Act - Same warehouse fulfills 2 different products
        FulfillmentAssignment assignment1 = useCase.create(testWarehouseCode1, testProductId1, testStoreId);
        FulfillmentAssignment assignment2 = useCase.create(testWarehouseCode1, testProductId2, testStoreId);

        // Assert
        assertNotNull(assignment1.id);
        assertNotNull(assignment2.id);
        assertNotEquals(assignment1.id, assignment2.id);
    }

    @Test
    @Transactional
    @DisplayName("Should allow 2 warehouses to fulfill same product for same store")
    void testTwoWarehousesSameProductSameStore() {
        // Act - 2 different warehouses fulfill same product
        FulfillmentAssignment assignment1 = useCase.create(testWarehouseCode1, testProductId1, testStoreId);
        FulfillmentAssignment assignment2 = useCase.create(testWarehouseCode2, testProductId1, testStoreId);

        // Assert
        assertNotNull(assignment1.id);
        assertNotNull(assignment2.id);
        assertEquals(2, repository.countWarehousesForProductInStore(testProductId1, testStoreId));
    }

    // ==================== CONSTRAINT VIOLATION TESTS ====================

    @Test
    @Transactional
    @DisplayName("Should throw when exceeding max 2 warehouses per product per store")
    void testMaxWarehousesPerProductPerStore() {
        // Arrange - Create 2 assignments for same product-store
        useCase.create(testWarehouseCode1, testProductId1, testStoreId);
        useCase.create(testWarehouseCode2, testProductId1, testStoreId);

        // Act & Assert - Third warehouse should fail
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> useCase.create(testWarehouseCode3, testProductId1, testStoreId),
            "Should throw when exceeding 2 warehouses per product per store"
        );

        assertTrue(exception.getMessage().contains("Maximum limit reached"));
    }

    @Test
    @Transactional
    @DisplayName("Should throw when exceeding max 3 warehouses per store")
    void testMaxWarehousesPerStore() {
        // Arrange - Create 3 different warehouses for the store
        useCase.create(testWarehouseCode1, testProductId1, testStoreId);
        useCase.create(testWarehouseCode2, testProductId2, testStoreId);
        useCase.create(testWarehouseCode3, testProductId3, testStoreId);

        // Need a 4th warehouse - create one
        Warehouse warehouse4 = createTestWarehouse("WH-TEST-004", "AMSTERDAM-001", 50, 10);
        warehouseStore.create(warehouse4);

        // Act & Assert - Fourth warehouse should fail
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> useCase.create("WH-TEST-004", testProductId1, testStoreId),
            "Should throw when exceeding 3 warehouses per store"
        );

        assertTrue(exception.getMessage().contains("Maximum limit reached"));
    }

    @Test
    @Transactional
    @DisplayName("Should throw when exceeding max 5 products per warehouse")
    void testMaxProductsPerWarehouse() {
        // This test would need 6 products - simplified version
        // Create assignments for 5 different products in same warehouse
        useCase.create(testWarehouseCode1, testProductId1, testStoreId);
        useCase.create(testWarehouseCode1, testProductId2, testStoreId);
        useCase.create(testWarehouseCode1, testProductId3, testStoreId);

        // Create additional products for testing
        Product product4 = createAndPersistProduct("TEST-PRODUCT-4");
        Product product5 = createAndPersistProduct("TEST-PRODUCT-5");
        Product product6 = createAndPersistProduct("TEST-PRODUCT-6");

        useCase.create(testWarehouseCode1, product4.id, testStoreId);
        useCase.create(testWarehouseCode1, product5.id, testStoreId);

        // Act & Assert - Sixth product should fail
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> useCase.create(testWarehouseCode1, product6.id, testStoreId),
            "Should throw when exceeding 5 products per warehouse"
        );

        assertTrue(exception.getMessage().contains("Maximum limit reached"));
    }

    @Test
    @Transactional
    @DisplayName("Should throw when duplicate assignment is attempted")
    void testDuplicateAssignment() {
        // Arrange
        useCase.create(testWarehouseCode1, testProductId1, testStoreId);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> useCase.create(testWarehouseCode1, testProductId1, testStoreId),
            "Should throw when creating duplicate assignment"
        );

        assertTrue(exception.getMessage().contains("already exists"));
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @Transactional
    @DisplayName("Should throw when warehouse does not exist")
    void testWarehouseNotFound() {
        assertThrows(
            EntityNotFoundException.class,
            () -> useCase.create("NONEXISTENT-WH", testProductId1, testStoreId),
            "Should throw when warehouse doesn't exist"
        );
    }

    @Test
    @Transactional
    @DisplayName("Should throw when product does not exist")
    void testProductNotFound() {
        assertThrows(
            EntityNotFoundException.class,
            () -> useCase.create(testWarehouseCode1, 99999L, testStoreId),
            "Should throw when product doesn't exist"
        );
    }

    @Test
    @Transactional
    @DisplayName("Should throw when store does not exist")
    void testStoreNotFound() {
        assertThrows(
            EntityNotFoundException.class,
            () -> useCase.create(testWarehouseCode1, testProductId1, 99999L),
            "Should throw when store doesn't exist"
        );
    }

    // ==================== HELPER METHODS ====================

    private Warehouse createTestWarehouse(String code, String location, int capacity, int stock) {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = code;
        warehouse.location = location;
        warehouse.capacity = capacity;
        warehouse.stock = stock;
        return warehouse;
    }

    private Product createAndPersistProduct(String name) {
        Product product = new Product(name);
        product.stock = 10;
        productRepository.persist(product);
        return product;
    }
}

