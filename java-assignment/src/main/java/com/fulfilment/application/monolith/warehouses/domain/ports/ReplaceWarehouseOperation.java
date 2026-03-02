package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;

/**
 * Port interface for the warehouse replacement operation.
 *
 * This interface defines the contract for replacing an existing warehouse with a new one
 * while reusing the business unit code for continuity tracking. The old warehouse is archived
 * and the new one takes its place with the same identifier.
 *
 * @see ReplaceWarehouseOperation
 */
public interface ReplaceWarehouseOperation {

  /**
   * Replaces an existing warehouse with a new one while reusing the business unit code.
   *
   * This operation ensures business continuity by:
   * 1. Validating the old warehouse exists and is active
   * 2. Validating the new warehouse data
   * 3. Archiving the old warehouse (soft delete)
   * 4. Creating the new warehouse with the same business unit code
   * 5. Transferring stock from old to new warehouse
   *
   * Validations performed:
   * - Old warehouse with given code must exist
   * - Old warehouse must not be archived
   * - New location must be valid
   * - New warehouse capacity must accommodate old warehouse stock
   * - Stock must match or be auto-set from old warehouse
   *
   * @param oldBusinessUnitCode the code of the warehouse being replaced (must exist)
   * @param newWarehouse the new warehouse data (stock will be set to old warehouse stock)
   *
   * @throws IllegalArgumentException if required fields are null or blank
   * @throws jakarta.persistence.EntityNotFoundException if old warehouse or location not found
   * @throws IllegalStateException if business rules are violated:
   *         - Old warehouse already archived
   *         - New capacity insufficient for old stock
   *         - Stock mismatch (if validation mode)
   *
   * @see Warehouse
   */
  void replace(String oldBusinessUnitCode, Warehouse newWarehouse);
}


