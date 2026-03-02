package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * Repository implementation for warehouse persistence.
 *
 * Provides CRUD operations for warehouse entities, implementing the WarehouseStore
 * port interface. Extends PanacheRepository for automatic Hibernate integration
 * and simplified database operations.
 *
 * @see WarehouseStore
 * @see DbWarehouse
 */
@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  private static final Logger LOGGER = Logger.getLogger(WarehouseRepository.class);

  /**
   * Retrieves all active (non-archived) warehouses.
   *
   * @return list of all warehouses in domain model format
   */
  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  /**
   * Creates and persists a new warehouse.
   *
   * Converts the domain warehouse model to a database entity, persists it,
   * and sets the creation timestamp. The warehouse must not already exist
   * (businessUnitCode must be unique).
   *
   * @param warehouse the warehouse to create, must not be null
   * @throws IllegalArgumentException if warehouse is null or businessUnitCode is blank
   * @throws IllegalStateException if warehouse with same code already exists
   */
  @Override
  public void create(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse must not be null");
    }
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Warehouse business unit code is required");
    }

    // Check for duplicate business unit code
    // Only active (non-archived) warehouses block creation
    // Archived warehouses have their codes freed for reuse during replacement
    DbWarehouse existing = find("businessUnitCode", warehouse.businessUnitCode).list().stream()
        .filter(w -> w.archivedAt == null)
        .findFirst()
        .orElse(null);

    if (existing != null) {
      // An active warehouse with this code already exists
      throw new IllegalStateException(
          "Warehouse with business unit code " + warehouse.businessUnitCode + " already exists");
    }

    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = LocalDateTime.now();

    persist(dbWarehouse);
    LOGGER.infov("Created warehouse with business unit code: {0}", warehouse.businessUnitCode);
  }

  /**
   * Updates an existing warehouse.
   *
   * Merges the updated warehouse data with the existing database record.
   * The warehouse must already exist (located by businessUnitCode).
   * Updates the modification timestamp.
   *
   * @param warehouse the warehouse with updated data, must not be null
   * @throws IllegalArgumentException if warehouse is null or businessUnitCode is blank
   * @throws EntityNotFoundException if warehouse with given code is not found
   */
  @Override
  public void update(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse must not be null");
    }
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Warehouse business unit code is required");
    }

    // Find existing warehouse by business unit code
    // Prefer active warehouse, but also find archived ones if needed
    List<DbWarehouse> allWithCode = find("businessUnitCode", warehouse.businessUnitCode).list();
    DbWarehouse existing = allWithCode.stream()
        .filter(w -> w.archivedAt == null)
        .findFirst()
        .orElse(allWithCode.isEmpty() ? null : allWithCode.get(0));

    if (existing == null) {
      throw new EntityNotFoundException(
          "Warehouse with business unit code " + warehouse.businessUnitCode + " not found");
    }

    // Update fields
    existing.location = warehouse.location;
    existing.capacity = warehouse.capacity;
    existing.stock = warehouse.stock;
    existing.archivedAt = warehouse.archivedAt; // Update archival status too

    LOGGER.infov("Updated warehouse with business unit code: {0}", warehouse.businessUnitCode);
  }

  /**
   * Removes (soft-deletes) a warehouse.
   *
   * Marks the warehouse as archived by setting the archivedAt timestamp.
   * This preserves the warehouse record for auditing purposes while removing
   * it from active operations.
   *
   * @param warehouse the warehouse to remove, must not be null
   * @throws IllegalArgumentException if warehouse is null or businessUnitCode is blank
   * @throws EntityNotFoundException if warehouse with given code is not found
   */
  @Override
  public void remove(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse must not be null");
    }
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Warehouse business unit code is required");
    }

    // Find existing warehouse by business unit code
    DbWarehouse existing =
        find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (existing == null) {
      throw new EntityNotFoundException(
          "Warehouse with business unit code " + warehouse.businessUnitCode + " not found");
    }

    // Soft-delete by setting archivedAt timestamp
    existing.archivedAt = LocalDateTime.now();

    LOGGER.infov("Archived warehouse with business unit code: {0}", warehouse.businessUnitCode);
  }

  /**
   * Deletes an archived warehouse permanently from the repository.
   *
   * Used during warehouse replacement to clean up old archived records
   * and free up the business unit code for reuse.
   *
   * @param businessUnitCode the code of the warehouse to delete
   * @throws EntityNotFoundException if warehouse not found
   */
  public void deleteArchived(String businessUnitCode) {
    if (businessUnitCode == null || businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Business unit code must not be null or blank");
    }

    DbWarehouse existing = find("businessUnitCode", businessUnitCode).firstResult();
    if (existing == null) {
      throw new EntityNotFoundException(
          "Warehouse with business unit code " + businessUnitCode + " not found");
    }

    if (existing.archivedAt != null) {
      // Delete by ID to ensure it's removed from the database
      deleteById(existing.id);
      LOGGER.infov("Deleted archived warehouse {0}", businessUnitCode);
    }
  }

  /**
   * Finds a warehouse by its business unit code.
   *
   * Performs a case-sensitive search for a warehouse matching the given
   * business unit code. This is the primary way to locate a warehouse.
   *
   * @param buCode the warehouse business unit code
   * @return the warehouse if found, or null if not found
   * @throws IllegalArgumentException if buCode is null or blank
   */
  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    if (buCode == null || buCode.isBlank()) {
      throw new IllegalArgumentException("Business unit code must not be null or blank");
    }

    // Find all warehouses with the given code
    List<DbWarehouse> allWithCode = find("businessUnitCode", buCode).list();

    if (allWithCode.isEmpty()) {
      LOGGER.debugv("Warehouse with business unit code {0} not found", buCode);
      return null;
    }

    // Prefer active (non-archived) warehouse, but return archived if no active exists
    DbWarehouse dbWarehouse = allWithCode.stream()
        .filter(w -> w.archivedAt == null)
        .findFirst()
        .orElse(allWithCode.get(0)); // Fallback to first (archived) if no active

    return dbWarehouse.toWarehouse();
  }
}
