package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

/**
 * Use case for archiving (soft-deleting) warehouses.
 *
 * Archives a warehouse by setting its archivedAt timestamp, preserving
 * historical data while removing it from active operations.
 */
@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ArchiveWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  /**
   * Archives a warehouse by its business unit code.
   *
   * @param businessUnitCode the code of the warehouse to archive
   * @throws IllegalArgumentException if businessUnitCode is null or blank
   * @throws EntityNotFoundException if warehouse not found
   * @throws IllegalStateException if warehouse is already archived
   */
  @Transactional
  public void archiveByCode(String businessUnitCode) {
    if (businessUnitCode == null || businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Business unit code is required");
    }

    Warehouse warehouse = warehouseStore.findByBusinessUnitCode(businessUnitCode);
    if (warehouse == null) {
      throw new EntityNotFoundException(
          "Warehouse with business unit code " + businessUnitCode + " not found");
    }

    archive(warehouse);
  }

  /**
   * Archives a warehouse by its database ID.
   *
   * @param id the database ID of the warehouse to archive
   * @throws IllegalArgumentException if id is null
   * @throws EntityNotFoundException if warehouse not found
   * @throws IllegalStateException if warehouse is already archived
   */
  @Override
  @Transactional
  public void archiveById(Long id) {
    if (id == null) {
      throw new IllegalArgumentException("Warehouse ID is required");
    }

    Warehouse warehouse = warehouseStore.findByDatabaseId(id);
    if (warehouse == null) {
      throw new EntityNotFoundException("Warehouse with id " + id + " not found");
    }

    archive(warehouse);
  }

  /**
   * Archives the given warehouse.
   *
   * @param warehouse the warehouse to archive
   * @throws IllegalArgumentException if warehouse is null
   * @throws IllegalStateException if warehouse is already archived
   */
  @Override
  @Transactional
  public void archive(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse must not be null");
    }

    if (warehouse.archivedAt != null) {
      throw new IllegalStateException(
          "Warehouse " + warehouse.businessUnitCode + " is already archived");
    }

    warehouse.archivedAt = LocalDateTime.now();
    warehouseStore.update(warehouse);

    LOGGER.infov("Archived warehouse with business unit code: {0}", warehouse.businessUnitCode);
  }
}
