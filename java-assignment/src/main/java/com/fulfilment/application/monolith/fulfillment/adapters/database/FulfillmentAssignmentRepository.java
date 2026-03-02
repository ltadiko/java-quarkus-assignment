package com.fulfilment.application.monolith.fulfillment.adapters.database;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * Repository for fulfillment assignment persistence operations.
 *
 * Provides CRUD operations and query methods for fulfillment assignments,
 * supporting the business rules around warehouse-product-store relationships.
 */
@ApplicationScoped
public class FulfillmentAssignmentRepository implements PanacheRepository<DbFulfillmentAssignment> {

    private static final Logger LOGGER = Logger.getLogger(FulfillmentAssignmentRepository.class);

    /**
     * Retrieves all fulfillment assignments.
     */
    public List<FulfillmentAssignment> getAll() {
        return listAll().stream()
            .map(DbFulfillmentAssignment::toFulfillmentAssignment)
            .toList();
    }

    /**
     * Creates a new fulfillment assignment.
     */
    public FulfillmentAssignment create(FulfillmentAssignment assignment) {
        DbFulfillmentAssignment db = DbFulfillmentAssignment.fromFulfillmentAssignment(assignment);
        persist(db);
        LOGGER.infov("Created fulfillment assignment: warehouse={0}, product={1}, store={2}",
            assignment.warehouseCode, assignment.productId, assignment.storeId);
        return db.toFulfillmentAssignment();
    }

    /**
     * Finds an assignment by ID.
     */
    public FulfillmentAssignment findAssignmentById(Long id) {
        DbFulfillmentAssignment db = find("id", id).firstResult();
        return db != null ? db.toFulfillmentAssignment() : null;
    }

    /**
     * Deletes an assignment by ID.
     */
    public void removeById(Long id) {
        DbFulfillmentAssignment db = find("id", id).firstResult();
        if (db == null) {
            throw new EntityNotFoundException("Fulfillment assignment with ID " + id + " not found");
        }
        delete(db);
        LOGGER.infov("Deleted fulfillment assignment with ID: {0}", id);
    }

    /**
     * Counts the number of warehouses fulfilling a specific product for a store.
     * Used to enforce: Each Product can be fulfilled by max 2 different Warehouses per Store
     */
    public long countWarehousesForProductInStore(Long productId, Long storeId) {
        return find("productId = ?1 and storeId = ?2", productId, storeId)
            .stream()
            .map(db -> db.warehouseCode)
            .distinct()
            .count();
    }

    /**
     * Counts the number of warehouses fulfilling a specific store.
     * Used to enforce: Each Store can be fulfilled by max 3 different Warehouses
     */
    public long countWarehousesForStore(Long storeId) {
        return find("storeId", storeId)
            .stream()
            .map(db -> db.warehouseCode)
            .distinct()
            .count();
    }

    /**
     * Counts the number of product types stored in a warehouse.
     * Used to enforce: Each Warehouse can store max 5 types of Products
     */
    public long countProductsInWarehouse(String warehouseCode) {
        return find("warehouseCode", warehouseCode)
            .stream()
            .map(db -> db.productId)
            .distinct()
            .count();
    }

    /**
     * Checks if an assignment already exists.
     */
    public boolean exists(String warehouseCode, Long productId, Long storeId) {
        return find("warehouseCode = ?1 and productId = ?2 and storeId = ?3",
            warehouseCode, productId, storeId).firstResult() != null;
    }

    /**
     * Finds all assignments for a specific store.
     */
    public List<FulfillmentAssignment> findByStoreId(Long storeId) {
        return find("storeId", storeId).stream()
            .map(DbFulfillmentAssignment::toFulfillmentAssignment)
            .toList();
    }

    /**
     * Finds all assignments for a specific warehouse.
     */
    public List<FulfillmentAssignment> findByWarehouseCode(String warehouseCode) {
        return find("warehouseCode", warehouseCode).stream()
            .map(DbFulfillmentAssignment::toFulfillmentAssignment)
            .toList();
    }

    /**
     * Finds all assignments for a specific product.
     */
    public List<FulfillmentAssignment> findByProductId(Long productId) {
        return find("productId", productId).stream()
            .map(DbFulfillmentAssignment::toFulfillmentAssignment)
            .toList();
    }

    /**
     * Checks if adding a new warehouse for a product in a store would exceed the limit.
     * Returns true if the warehouse is already assigned or if there's room for another warehouse.
     */
    public boolean canAddWarehouseForProductInStore(String warehouseCode, Long productId, Long storeId) {
        // Check if this exact combination already exists
        if (exists(warehouseCode, productId, storeId)) {
            return false; // Already exists, can't add duplicate
        }

        // Check if this warehouse is already fulfilling this product for this store
        boolean warehouseAlreadyAssigned = find("warehouseCode = ?1 and productId = ?2 and storeId = ?3",
            warehouseCode, productId, storeId).firstResult() != null;

        if (warehouseAlreadyAssigned) {
            return true; // Same warehouse, no limit issue
        }

        // Count distinct warehouses for this product-store combo
        long currentCount = countWarehousesForProductInStore(productId, storeId);
        return currentCount < 2; // Max 2 warehouses per product per store
    }
}

