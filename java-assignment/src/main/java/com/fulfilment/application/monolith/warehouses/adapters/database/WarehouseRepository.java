package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * Repository implementation for warehouse persistence.
 * Provides CRUD operations for warehouse entities, implementing the WarehouseStore
 * port interface. This is a thin persistence layer — all business validations
 * are handled by the use case classes.
 *
 * @see WarehouseStore
 * @see DbWarehouse
 */
@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  private static final Logger LOGGER = Logger.getLogger(WarehouseRepository.class);

  /**
   * Retrieves all warehouses (including archived).
   *
   * @return list of all warehouses in domain model format
   */
  @Override
  public List<Warehouse> getAll() {
    LOGGER.debug("Fetching all active warehouses from database");
    return find("archivedAt is null").list().stream().map(DbWarehouse::toWarehouse).toList();
  }

  /**
   * Creates and persists a new warehouse.
   *
   * @param warehouse the warehouse to create, must not be null
   */
  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = LocalDateTime.now();

    persist(dbWarehouse);
    LOGGER.infov("Persisted warehouse with business unit code: {0}", warehouse.businessUnitCode);
  }

  /**
   * Updates an existing warehouse in the database.
   *
   * @param warehouse the warehouse with updated data, must not be null
   */
  @Override
  public void update(Warehouse warehouse) {
    DbWarehouse existing = find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode).firstResult();

    if (existing == null) {
      LOGGER.warnv("Attempted to update non-existent or archived warehouse: {0}", warehouse.businessUnitCode);
      return;
    }

    // Update fields
    existing.location = warehouse.location;
    existing.capacity = warehouse.capacity;
    existing.stock = warehouse.stock;
    existing.archivedAt = warehouse.archivedAt;

    LOGGER.infov("Updated warehouse with business unit code: {0}", warehouse.businessUnitCode);
  }

  /**
   * Removes (soft-deletes) a warehouse by setting the archivedAt timestamp.
   *
   * @param warehouse the warehouse to remove, must not be null
   */
  @Override
  public void remove(Warehouse warehouse) {
    DbWarehouse existing =
        find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode).firstResult();
    if (existing == null) {
      LOGGER.warnv("Attempted to remove non-existent or already archived warehouse: {0}", warehouse.businessUnitCode);
      return;
    }

    existing.archivedAt = LocalDateTime.now();
    LOGGER.infov("Soft-deleted warehouse with business unit code: {0}", warehouse.businessUnitCode);
  }

  /**
   * Deletes an archived warehouse permanently from the database.
   *
   * @param businessUnitCode the code of the warehouse to delete
   */
  @Override
  public void deleteArchived(String businessUnitCode) {
    DbWarehouse existing = find("businessUnitCode = ?1 and archivedAt is not null", businessUnitCode).firstResult();
    if (existing == null) {
      LOGGER.warnv("No archived warehouse found to delete: {0}", businessUnitCode);
      return;
    }

    deleteById(existing.id);
    LOGGER.infov("Permanently deleted archived warehouse: {0}", businessUnitCode);
  }

  /**
   * Finds a warehouse by its business unit code.
   *
   * @param buCode the warehouse business unit code
   * @return the warehouse if found, or null if not found
   */
  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    if (buCode == null || buCode.isBlank()) {
      LOGGER.debug("findByBusinessUnitCode called with null or blank code");
      return null;
    }

    // First try to find an active (non-archived) warehouse
    DbWarehouse dbWarehouse = find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();

    // Fall back to archived if no active exists
    if (dbWarehouse == null) {
      dbWarehouse = find("businessUnitCode", buCode).firstResult();
    }

    if (dbWarehouse == null) {
      LOGGER.debugv("Warehouse with business unit code {0} not found", buCode);
      return null;
    }


    return dbWarehouse.toWarehouse();
  }

  /**
   * Finds a warehouse by its database ID.
   *
   * @param id the database ID
   * @return the warehouse if found, or null if not found
   */
  @Override
  public Warehouse findByDatabaseId(Long id) {
    if (id == null) {
      LOGGER.debug("findByDatabaseId called with null id");
      return null;
    }

    DbWarehouse dbWarehouse = findById(id);
    if (dbWarehouse == null) {
      LOGGER.debugv("Warehouse with id {0} not found", id);
      return null;
    }

    return dbWarehouse.toWarehouse();
  }
}
