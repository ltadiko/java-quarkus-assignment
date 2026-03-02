package com.fulfilment.application.monolith.warehouses.domain.models;

import java.util.Objects;

/**
 * Domain model representing a geographical location where warehouses can be established.
 *
 * A location defines the maximum capacity and number of warehouses that can operate in that
 * geographical area. Each location is uniquely identified by an identification code.
 *
 * This is a domain entity and should not be modified after creation.
 */
public class Location {

  /** Unique identifier for the location (e.g., "ZWOLLE-001", "AMSTERDAM-001") */
  public final String identification;

  /** Maximum number of warehouses that can be established at this location */
  public final int maxNumberOfWarehouses;

  /** Maximum total capacity across all warehouses at this location */
  public final int maxCapacity;

  /**
   * Constructs a new Location with the specified identification, warehouse limit, and capacity.
   *
   * @param identification the unique location identifier, must not be null or blank
   * @param maxNumberOfWarehouses the maximum number of warehouses allowed at this location, must be positive
   * @param maxCapacity the maximum total capacity of all warehouses at this location, must be positive
   *
   * @throws IllegalArgumentException if identification is null/blank, or limits are not positive
   */
  public Location(String identification, int maxNumberOfWarehouses, int maxCapacity) {
    validateIdentification(identification);
    validateMaxNumberOfWarehouses(maxNumberOfWarehouses);
    validateMaxCapacity(maxCapacity);

    this.identification = identification;
    this.maxNumberOfWarehouses = maxNumberOfWarehouses;
    this.maxCapacity = maxCapacity;
  }

  /**
   * Validates that the identification is valid.
   *
   * @param identification the identification to validate
   * @throws IllegalArgumentException if identification is null or blank
   */
  private void validateIdentification(String identification) {
    if (identification == null || identification.isBlank()) {
      throw new IllegalArgumentException("Location identification must not be null or blank");
    }
  }

  /**
   * Validates that the maximum number of warehouses is positive.
   *
   * @param maxNumberOfWarehouses the value to validate
   * @throws IllegalArgumentException if value is not positive
   */
  private void validateMaxNumberOfWarehouses(int maxNumberOfWarehouses) {
    if (maxNumberOfWarehouses <= 0) {
      throw new IllegalArgumentException(
          "Max number of warehouses must be positive, got: " + maxNumberOfWarehouses);
    }
  }

  /**
   * Validates that the maximum capacity is positive.
   *
   * @param maxCapacity the value to validate
   * @throws IllegalArgumentException if value is not positive
   */
  private void validateMaxCapacity(int maxCapacity) {
    if (maxCapacity <= 0) {
      throw new IllegalArgumentException(
          "Max capacity must be positive, got: " + maxCapacity);
    }
  }

  /**
   * Checks if this location has capacity for an additional warehouse.
   *
   * @param currentWarehouseCount the current number of warehouses at this location
   * @return true if additional warehouse can be created, false otherwise
   */
  public boolean canAddWarehouse(int currentWarehouseCount) {
    return currentWarehouseCount < maxNumberOfWarehouses;
  }

  /**
   * Checks if the given capacity is within this location's limits.
   *
   * @param capacity the capacity to validate
   * @return true if capacity is within limits, false otherwise
   */
  public boolean isCapacityValid(int capacity) {
    return capacity > 0 && capacity <= maxCapacity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Location location = (Location) o;
    return Objects.equals(identification, location.identification);
  }

  @Override
  public int hashCode() {
    return Objects.hash(identification);
  }

  @Override
  public String toString() {
    return "Location{" +
        "identification='" + identification + '\'' +
        ", maxNumberOfWarehouses=" + maxNumberOfWarehouses +
        ", maxCapacity=" + maxCapacity +
        '}';
  }
}
