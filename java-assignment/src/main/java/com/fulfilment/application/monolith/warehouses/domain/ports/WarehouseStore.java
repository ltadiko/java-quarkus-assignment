package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import java.util.List;

public interface WarehouseStore {

  List<Warehouse> getAll();

  void create(Warehouse warehouse);

  void update(Warehouse warehouse);

  void remove(Warehouse warehouse);

  /**
   * Deletes an archived warehouse permanently from the repository.
   * Used during warehouse replacement to free up the business unit code.
   *
   * @param businessUnitCode the code of the warehouse to delete
   */
  void deleteArchived(String businessUnitCode);

  Warehouse findByBusinessUnitCode(String buCode);

  /**
   * Finds a warehouse by its database ID.
   *
   * @param id the database ID
   * @return the warehouse if found, or null if not found
   */
  Warehouse findByDatabaseId(Long id);
}
