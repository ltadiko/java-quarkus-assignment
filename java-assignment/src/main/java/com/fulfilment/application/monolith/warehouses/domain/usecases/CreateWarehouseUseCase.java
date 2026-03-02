package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * Use case for creating warehouses with business logic validation.
 *
 * Implements the CreateWarehouseOperation port to handle warehouse creation
 * with comprehensive validation of business rules including location validation,
 * capacity constraints, and uniqueness constraints.
 *
 * Validations performed:
 * 1. Business unit code must be unique
 * 2. Location must exist in the system
 * 3. Maximum warehouses per location must not be exceeded
 * 4. Total location capacity must not be exceeded
 * 5. Warehouse capacity must accommodate the stock
 *
 * @see CreateWarehouseOperation
 * @see WarehouseStore
 * @see LocationGateway
 */
@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(CreateWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final LocationGateway locationGateway;

  /**
   * Constructs a CreateWarehouseUseCase with required dependencies.
   *
   * @param warehouseStore the warehouse repository for persistence operations
   * @param locationGateway the location gateway for location validation
   */
  @Inject
  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationGateway locationGateway) {
    this.warehouseStore = warehouseStore;
    this.locationGateway = locationGateway;
  }

  /**
   * Creates a new warehouse after performing comprehensive validations.
   *
   * Validates:
   * 1. Business unit code is unique (not already in use)
   * 2. Location exists in the system
   * 3. Maximum warehouses per location not exceeded
   * 4. Total location capacity not exceeded
   * 5. Warehouse capacity is sufficient for stock
   *
   * @param warehouse the warehouse to create, must have valid data
   * @throws IllegalArgumentException if warehouse data is invalid (null, blank fields)
   * @throws IllegalStateException if business rules are violated
   * @throws EntityNotFoundException if location doesn't exist
   */
  @Override
  public void create(Warehouse warehouse) {
    // Validation 1: Verify business unit code is unique
    validateBusinessUnitCodeUnique(warehouse.businessUnitCode);

    // Validation 2: Verify location exists
    Location location = validateLocationExists(warehouse.location);

    // Get existing warehouses for further validations
    List<Warehouse> existingWarehouses = warehouseStore.getAll();

    // Validation 3: Verify warehouse creation is feasible
    validateWarehouseCreationFeasible(location, existingWarehouses);

    // Validation 4: Verify location capacity not exceeded
    validateLocationCapacity(location, warehouse, existingWarehouses);

    // Validation 5: Verify warehouse capacity is sufficient
    validateWarehouseCapacity(warehouse);

    // All validations passed - create the warehouse
    warehouseStore.create(warehouse);

    LOGGER.infov("Successfully created warehouse with code: {0}", warehouse.businessUnitCode);
  }

  /**
   * Validates that the business unit code is unique (not already in use).
   *
   * @param businessUnitCode the code to validate
   * @throws IllegalArgumentException if code is null or blank
   * @throws IllegalStateException if code already exists
   */
  private void validateBusinessUnitCodeUnique(String businessUnitCode) {
    if (businessUnitCode == null || businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Warehouse business unit code is required");
    }

    Warehouse existing = warehouseStore.findByBusinessUnitCode(businessUnitCode);
    if (existing != null) {
      throw new IllegalStateException(
          "Warehouse with business unit code " + businessUnitCode + " already exists");
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
   * Validates that the maximum number of warehouses per location has not been exceeded.
   *
   * @param location the location where warehouse will be created
   * @param existingWarehouses list of all existing warehouses
   * @throws IllegalStateException if max warehouses for location reached
   */
  private void validateWarehouseCreationFeasible(
      Location location, List<Warehouse> existingWarehouses) {
    long warehousesInLocation =
        existingWarehouses.stream()
            .filter(w -> w.location != null && w.location.equals(location.identification))
            .count();

    if (warehousesInLocation >= location.maxNumberOfWarehouses) {
      throw new IllegalStateException(
          "Maximum number of warehouses ("
              + location.maxNumberOfWarehouses
              + ") for location "
              + location.identification
              + " has been reached");
    }
  }

  /**
   * Validates that the location's maximum total capacity is not exceeded.
   *
   * Sums the capacity of all existing warehouses in the location and ensures
   * that adding this new warehouse would not exceed the location's maximum capacity.
   *
   * @param location the location where warehouse will be created
   * @param warehouse the warehouse being created
   * @param existingWarehouses list of all existing warehouses
   * @throws IllegalStateException if total capacity would be exceeded
   */
  private void validateLocationCapacity(
      Location location, Warehouse warehouse, List<Warehouse> existingWarehouses) {
    int usedCapacity =
        existingWarehouses.stream()
            .filter(w -> w.location != null && w.location.equals(location.identification))
            .mapToInt(w -> w.capacity != null ? w.capacity : 0)
            .sum();

    int totalCapacityNeeded = usedCapacity + (warehouse.capacity != null ? warehouse.capacity : 0);

    if (totalCapacityNeeded > location.maxCapacity) {
      throw new IllegalStateException(
          "Warehouse capacity "
              + warehouse.capacity
              + " would exceed location maximum capacity "
              + location.maxCapacity);
    }
  }

  /**
   * Validates that the warehouse capacity is sufficient for the stock.
   *
   * Ensures the warehouse capacity is positive and can accommodate the
   * initial stock being stored in it.
   *
   * @param warehouse the warehouse being created
   * @throws IllegalArgumentException if capacity is invalid
   * @throws IllegalStateException if capacity is insufficient for stock
   */
  private void validateWarehouseCapacity(Warehouse warehouse) {
    if (warehouse.capacity == null || warehouse.capacity < 1) {
      throw new IllegalArgumentException("Warehouse capacity must be a positive number");
    }

    if (warehouse.stock != null && warehouse.stock > warehouse.capacity) {
      throw new IllegalStateException(
          "Warehouse capacity "
              + warehouse.capacity
              + " is insufficient for stock "
              + warehouse.stock);
    }
  }
}




