package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import com.fulfilment.application.monolith.fulfillment.adapters.database.FulfillmentAssignmentRepository;
import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * Use case for creating fulfillment assignments.
 *
 * Handles the business logic for associating warehouses with products for specific stores,
 * enforcing the following constraints:
 * - Each Product can be fulfilled by max 2 different Warehouses per Store
 * - Each Store can be fulfilled by max 3 different Warehouses
 * - Each Warehouse can store max 5 types of Products
 */
@ApplicationScoped
public class CreateFulfillmentAssignmentUseCase {

    private static final Logger LOGGER = Logger.getLogger(CreateFulfillmentAssignmentUseCase.class);

    // Business rule constants
    private static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;
    private static final int MAX_WAREHOUSES_PER_STORE = 3;
    private static final int MAX_PRODUCTS_PER_WAREHOUSE = 5;

    @Inject
    FulfillmentAssignmentRepository assignmentRepository;

    @Inject
    WarehouseStore warehouseStore;

    @Inject
    ProductRepository productRepository;

    /**
     * Creates a new fulfillment assignment.
     *
     * @param warehouseCode the business unit code of the warehouse
     * @param productId the ID of the product
     * @param storeId the ID of the store
     * @return the created FulfillmentAssignment
     * @throws EntityNotFoundException if warehouse, product, or store doesn't exist
     * @throws IllegalStateException if any business rule would be violated
     */
    @Transactional
    public FulfillmentAssignment create(String warehouseCode, Long productId, Long storeId) {
        LOGGER.infov("Creating fulfillment assignment: warehouse={0}, product={1}, store={2}",
            warehouseCode, productId, storeId);

        // Validate entities exist
        validateWarehouseExists(warehouseCode);
        validateProductExists(productId);
        validateStoreExists(storeId);

        // Check if assignment already exists
        if (assignmentRepository.exists(warehouseCode, productId, storeId)) {
            throw new IllegalStateException(
                "Fulfillment assignment already exists for warehouse=" + warehouseCode +
                ", product=" + productId + ", store=" + storeId);
        }

        // Validate business rules
        validateWarehousesPerProductPerStore(warehouseCode, productId, storeId);
        validateWarehousesPerStore(warehouseCode, storeId);
        validateProductsPerWarehouse(warehouseCode, productId);

        // Create the assignment
        FulfillmentAssignment assignment = new FulfillmentAssignment(warehouseCode, productId, storeId);
        FulfillmentAssignment created = assignmentRepository.create(assignment);

        LOGGER.infov("Successfully created fulfillment assignment with ID: {0}", created.id);
        return created;
    }

    /**
     * Validates that the warehouse exists.
     */
    private void validateWarehouseExists(String warehouseCode) {
        Warehouse warehouse = warehouseStore.findByBusinessUnitCode(warehouseCode);
        if (warehouse == null) {
            throw new EntityNotFoundException("Warehouse with code " + warehouseCode + " not found");
        }
        if (warehouse.archivedAt != null) {
            throw new IllegalStateException("Warehouse with code " + warehouseCode + " is archived");
        }
    }

    /**
     * Validates that the product exists.
     */
    private void validateProductExists(Long productId) {
        Product product = productRepository.findById(productId);
        if (product == null) {
            throw new EntityNotFoundException("Product with ID " + productId + " not found");
        }
    }

    /**
     * Validates that the store exists.
     */
    private void validateStoreExists(Long storeId) {
        Store store = Store.findById(storeId);
        if (store == null) {
            throw new EntityNotFoundException("Store with ID " + storeId + " not found");
        }
    }

    /**
     * Validates: Each Product can be fulfilled by max 2 different Warehouses per Store
     */
    private void validateWarehousesPerProductPerStore(String warehouseCode, Long productId, Long storeId) {
        long currentCount = assignmentRepository.countWarehousesForProductInStore(productId, storeId);

        // Check if this warehouse is already counted
        List<FulfillmentAssignment> existingAssignments = assignmentRepository.findByStoreId(storeId);
        boolean warehouseAlreadyAssignedToProduct = existingAssignments.stream()
            .anyMatch(a -> a.warehouseCode.equals(warehouseCode) && a.productId.equals(productId));

        if (!warehouseAlreadyAssignedToProduct && currentCount >= MAX_WAREHOUSES_PER_PRODUCT_PER_STORE) {
            throw new IllegalStateException(
                "Product " + productId + " already has " + MAX_WAREHOUSES_PER_PRODUCT_PER_STORE +
                " warehouses assigned for store " + storeId +
                ". Maximum limit reached.");
        }
    }

    /**
     * Validates: Each Store can be fulfilled by max 3 different Warehouses
     */
    private void validateWarehousesPerStore(String warehouseCode, Long storeId) {
        long currentCount = assignmentRepository.countWarehousesForStore(storeId);

        // Check if this warehouse is already fulfilling this store
        List<FulfillmentAssignment> existingAssignments = assignmentRepository.findByStoreId(storeId);
        boolean warehouseAlreadyAssignedToStore = existingAssignments.stream()
            .anyMatch(a -> a.warehouseCode.equals(warehouseCode));

        if (!warehouseAlreadyAssignedToStore && currentCount >= MAX_WAREHOUSES_PER_STORE) {
            throw new IllegalStateException(
                "Store " + storeId + " already has " + MAX_WAREHOUSES_PER_STORE +
                " warehouses assigned. Maximum limit reached.");
        }
    }

    /**
     * Validates: Each Warehouse can store max 5 types of Products
     */
    private void validateProductsPerWarehouse(String warehouseCode, Long productId) {
        long currentCount = assignmentRepository.countProductsInWarehouse(warehouseCode);

        // Check if this product is already stored in this warehouse
        List<FulfillmentAssignment> existingAssignments = assignmentRepository.findByWarehouseCode(warehouseCode);
        boolean productAlreadyInWarehouse = existingAssignments.stream()
            .anyMatch(a -> a.productId.equals(productId));

        if (!productAlreadyInWarehouse && currentCount >= MAX_PRODUCTS_PER_WAREHOUSE) {
            throw new IllegalStateException(
                "Warehouse " + warehouseCode + " already has " + MAX_PRODUCTS_PER_WAREHOUSE +
                " product types. Maximum limit reached.");
        }
    }
}

