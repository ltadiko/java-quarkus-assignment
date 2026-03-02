package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import org.jboss.logging.Logger;

/**
 * Use case for replacing warehouses with business logic validation.
 *
 * Implements the ReplaceWarehouseOperation port to handle warehouse replacement
 * while maintaining business unit code continuity and audit trail through archival.
 *
 * Validations performed:
 * 1. Old warehouse must exist with given code
 * 2. Old warehouse must not be archived
 * 3. New location must exist in the system
 * 4. New warehouse capacity must accommodate old warehouse stock
 * 5. New warehouse stock must match old warehouse stock
 * 6. New warehouse data must be valid
 *
 * @see ReplaceWarehouseOperation
 * @see WarehouseStore
 * @see LocationGateway
 */
@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ReplaceWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final LocationGateway locationGateway;

  /**
   * Constructs a ReplaceWarehouseUseCase with required dependencies.
   *
   * @param warehouseStore the warehouse repository for persistence operations
   * @param locationGateway the location gateway for location validation
   */
  @Inject
  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationGateway locationGateway) {
    this.warehouseStore = warehouseStore;
    this.locationGateway = locationGateway;
  }

  /**
   * Replaces an existing warehouse with a new one while reusing the business unit code.
   *
   * The operation:
   * 1. Validates old warehouse exists and is active
   * 2. Validates new warehouse and location
   * 3. Archives the old warehouse (soft delete)
   * 4. Creates new warehouse with same business unit code
   * 5. Transfers stock from old to new warehouse
   *
   * @param oldBusinessUnitCode the code of warehouse being replaced
   * @param newWarehouse the new warehouse data
   * @throws IllegalArgumentException if inputs invalid
   * @throws EntityNotFoundException if old warehouse or location not found
   * @throws IllegalStateException if business rules violated
   */
  @Override
  public void replace(String oldBusinessUnitCode, Warehouse newWarehouse) {
    // Validation 1: Verify old warehouse exists
    Warehouse oldWarehouse = validateWarehouseExists(oldBusinessUnitCode);

    // Validation 2: Verify old warehouse not archived
    validateWarehouseNotArchived(oldWarehouse);

    // Validation 3: Verify new location exists
    validateLocationExists(newWarehouse.location);

    // Validation 4: Verify capacity accommodation
    validateCapacityAccommodation(oldWarehouse, newWarehouse);

    // Validation 5: Verify/ensure stock matching
    validateStockMatching(oldWarehouse, newWarehouse);

    // Validation 6: Validate new warehouse validity
    validateNewWarehouseValid(newWarehouse);

    // All validations passed - proceed with replacement
    // Archive the old warehouse (soft delete - keeps it in database for audit trail)
    archiveOldWarehouse(oldWarehouse);

    // Create the new warehouse with the same code
    // The repository.create() check will ignore the archived warehouse
    createNewWarehouse(oldBusinessUnitCode, newWarehouse, oldWarehouse.stock);

    LOGGER.infov(
        "Successfully replaced warehouse {0} with new warehouse in location {1}",
        oldBusinessUnitCode, newWarehouse.location);
  }

  /**
   * Validates that a warehouse with the given code exists.
   *
   * @param businessUnitCode the code to validate
   * @return the found Warehouse
   * @throws IllegalArgumentException if code is null or blank
   * @throws EntityNotFoundException if warehouse not found
   */
  private Warehouse validateWarehouseExists(String businessUnitCode) {
    if (businessUnitCode == null || businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Warehouse business unit code is required");
    }

    Warehouse warehouse = warehouseStore.findByBusinessUnitCode(businessUnitCode);
    if (warehouse == null) {
      throw new EntityNotFoundException(
          "Warehouse with business unit code " + businessUnitCode + " not found");
    }

    return warehouse;
  }

  /**
   * Validates that a warehouse is not already archived.
   *
   * @param warehouse the warehouse to validate
   * @throws IllegalStateException if warehouse is archived
   */
  private void validateWarehouseNotArchived(Warehouse warehouse) {
    if (warehouse.archivedAt != null) {
      throw new IllegalStateException(
          "Cannot replace warehouse "
              + warehouse.businessUnitCode
              + " as it is already archived");
    }
  }

  /**
   * Validates that the location exists in the system.
   *
   * @param locationCode the location identifier to validate
   * @return the Location object if valid
   * @throws IllegalArgumentException if location code is null or blank
   * @throws EntityNotFoundException if location doesn't exist
   */
  private Location validateLocationExists(String locationCode) {
    if (locationCode == null || locationCode.isBlank()) {
      throw new IllegalArgumentException("Warehouse location code is required");
    }

    Location location = locationGateway.resolveByIdentifier(locationCode);
    if (location == null) {
      throw new EntityNotFoundException(
          "Location with code " + locationCode + " does not exist");
    }

    return location;
  }

  /**
   * Validates that the new warehouse capacity can accommodate the old warehouse stock.
   *
   * @param oldWarehouse the warehouse being replaced
   * @param newWarehouse the replacement warehouse
   * @throws IllegalArgumentException if new capacity is null
   * @throws IllegalStateException if capacity insufficient
   */
  private void validateCapacityAccommodation(Warehouse oldWarehouse, Warehouse newWarehouse) {
    if (newWarehouse.capacity == null) {
      throw new IllegalArgumentException("New warehouse capacity is required");
    }

    if (newWarehouse.capacity < oldWarehouse.stock) {
      throw new IllegalStateException(
          "New warehouse capacity "
              + newWarehouse.capacity
              + " cannot accommodate existing stock "
              + oldWarehouse.stock);
    }
  }

  /**
   * Validates or ensures stock matching between old and new warehouse.
   *
   * If new warehouse stock is not provided, it is auto-set to old warehouse stock.
   * If provided, it must match the old warehouse stock.
   *
   * @param oldWarehouse the warehouse being replaced
   * @param newWarehouse the replacement warehouse
   * @throws IllegalStateException if stock mismatch
   */
  private void validateStockMatching(Warehouse oldWarehouse, Warehouse newWarehouse) {
    if (newWarehouse.stock == null) {
      // Auto-set stock from old warehouse if not provided
      newWarehouse.stock = oldWarehouse.stock;
      LOGGER.debugv(
          "Stock auto-set to {0} from old warehouse", oldWarehouse.stock);
      return;
    }

    if (!newWarehouse.stock.equals(oldWarehouse.stock)) {
      throw new IllegalStateException(
          "New warehouse stock "
              + newWarehouse.stock
              + " must match old warehouse stock "
              + oldWarehouse.stock);
    }
  }

  /**
   * Validates that the new warehouse data is valid.
   *
   * @param newWarehouse the warehouse to validate
   * @throws IllegalArgumentException if capacity invalid
   */
  private void validateNewWarehouseValid(Warehouse newWarehouse) {
    if (newWarehouse.capacity == null || newWarehouse.capacity < 1) {
      throw new IllegalArgumentException("New warehouse capacity must be a positive number");
    }

    if (newWarehouse.location == null || newWarehouse.location.isBlank()) {
      throw new IllegalArgumentException("New warehouse location is required");
    }
  }

  /**
   * Archives the old warehouse by setting its archival timestamp.
   *
   * @param warehouse the warehouse to archive
   */
  private void archiveOldWarehouse(Warehouse warehouse) {
    warehouse.archivedAt = LocalDateTime.now();
    warehouseStore.update(warehouse);
    LOGGER.infov("Archived old warehouse {0}", warehouse.businessUnitCode);
  }


  /**
   * Creates the new warehouse with the old warehouse's business unit code.
   *
   * @param businessUnitCode the code to reuse
   * @param newWarehouse the new warehouse data
   * @param stock the stock to transfer
   */
  private void createNewWarehouse(String businessUnitCode, Warehouse newWarehouse, Integer stock) {
    newWarehouse.businessUnitCode = businessUnitCode;
    newWarehouse.stock = stock;
    warehouseStore.create(newWarehouse);
    LOGGER.infov("Created new warehouse with code {0}", businessUnitCode);
  }
}
