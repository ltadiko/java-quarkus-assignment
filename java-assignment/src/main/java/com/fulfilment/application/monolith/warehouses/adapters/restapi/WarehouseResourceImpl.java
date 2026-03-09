package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;
import org.jboss.logging.Logger;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  private static final Logger LOGGER = Logger.getLogger(WarehouseResourceImpl.class);

  @Inject WarehouseRepository warehouseRepository;
  @Inject CreateWarehouseOperation createWarehouseOperation;
  @Inject ArchiveWarehouseOperation archiveWarehouseOperation;
  @Inject ReplaceWarehouseOperation replaceWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    LOGGER.info("Received request to list all warehouse units");
    var warehouses = warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
    LOGGER.infov("Returning {0} warehouse(s)", warehouses.size());
    return warehouses;
  }

  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    LOGGER.infov("Received request to create warehouse with business unit code: {0}, location: {1}, capacity: {2}, stock: {3}",
        data.getBusinessUnitCode(), data.getLocation(), data.getCapacity(), data.getStock());
    try {
      var domainWarehouse = toDomainModel(data);
      createWarehouseOperation.create(domainWarehouse);

      // Re-fetch to get server-set fields (createdAt, etc.)
      var created = warehouseRepository.findByBusinessUnitCode(data.getBusinessUnitCode());
      LOGGER.infov("Successfully created warehouse with business unit code: {0}", data.getBusinessUnitCode());
      return toWarehouseResponse(created);
    } catch (IllegalArgumentException e) {
      LOGGER.warnv("Bad request while creating warehouse: {0}", e.getMessage());
      throw new WebApplicationException(e.getMessage(), 400);
    } catch (EntityNotFoundException e) {
      LOGGER.warnv("Resource not found while creating warehouse: {0}", e.getMessage());
      throw new WebApplicationException(e.getMessage(), 404);
    } catch (IllegalStateException e) {
      LOGGER.warnv("Conflict while creating warehouse: {0}", e.getMessage());
      throw new WebApplicationException(e.getMessage(), 409);
    }
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    LOGGER.infov("Received request to get warehouse with id: {0}", id);
    Long warehouseId = parseWarehouseId(id);
    var warehouse = warehouseRepository.findByDatabaseId(warehouseId);
    if (warehouse == null) {
      LOGGER.warnv("Warehouse with id {0} not found", id);
      throw new WebApplicationException("Warehouse with id " + id + " does not exist.", 404);
    }
    LOGGER.infov("Found warehouse with id: {0}, location: {1}", id, warehouse.location);
    return toWarehouseResponse(warehouse);
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    LOGGER.infov("Received request to archive warehouse with id: {0}", id);
    try {
      Long warehouseId = parseWarehouseId(id);
      archiveWarehouseOperation.archiveById(warehouseId);
      LOGGER.infov("Successfully archived warehouse with id: {0}", id);
    } catch (NumberFormatException e) {
      LOGGER.warnv("Invalid warehouse id format: {0}", id);
      throw new WebApplicationException("Invalid warehouse id: " + id, 400);
    } catch (IllegalArgumentException e) {
      LOGGER.warnv("Bad request while archiving warehouse {0}: {1}", id, e.getMessage());
      throw new WebApplicationException(e.getMessage(), 400);
    } catch (EntityNotFoundException e) {
      LOGGER.warnv("Cannot archive warehouse with id {0}: not found", id);
      throw new WebApplicationException(e.getMessage(), 404);
    } catch (IllegalStateException e) {
      LOGGER.warnv("Conflict while archiving warehouse {0}: {1}", id, e.getMessage());
      throw new WebApplicationException(e.getMessage(), 409);
    }
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    LOGGER.infov("Received request to replace warehouse {0} with new warehouse at location: {1}, capacity: {2}, stock: {3}",
        businessUnitCode, data.getLocation(), data.getCapacity(), data.getStock());
    try {
      var newWarehouse = toDomainModel(data);
      replaceWarehouseOperation.replace(businessUnitCode, newWarehouse);

      // Re-fetch the newly created warehouse
      var replaced = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
      LOGGER.infov("Successfully replaced warehouse with business unit code: {0}", businessUnitCode);
      return toWarehouseResponse(replaced);
    } catch (IllegalArgumentException e) {
      LOGGER.warnv("Bad request while replacing warehouse {0}: {1}", businessUnitCode, e.getMessage());
      throw new WebApplicationException(e.getMessage(), 400);
    } catch (EntityNotFoundException e) {
      LOGGER.warnv("Resource not found while replacing warehouse {0}: {1}", businessUnitCode, e.getMessage());
      throw new WebApplicationException(e.getMessage(), 404);
    } catch (IllegalStateException e) {
      LOGGER.warnv("Conflict while replacing warehouse {0}: {1}", businessUnitCode, e.getMessage());
      throw new WebApplicationException(e.getMessage(), 409);
    }
  }

  /**
   * Converts an API Warehouse bean to a domain Warehouse model.
   */
  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomainModel(
      Warehouse apiWarehouse) {
    var domain = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domain.businessUnitCode = apiWarehouse.getBusinessUnitCode();
    domain.location = apiWarehouse.getLocation();
    domain.capacity = apiWarehouse.getCapacity();
    domain.stock = apiWarehouse.getStock();
    return domain;
  }

  /**
   * Converts a domain Warehouse model to an API Warehouse bean.
   */
  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    if (warehouse.id != null) {
      response.setId(String.valueOf(warehouse.id));
    }
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);
    return response;
  }

  /**
   * Parses a warehouse ID string into a Long.
   *
   * @param id the ID string to parse
   * @return the parsed Long ID
   * @throws WebApplicationException with 400 status if id is not a valid number
   */
  private Long parseWarehouseId(String id) {
    try {
      return Long.parseLong(id);
    } catch (NumberFormatException e) {
      LOGGER.warnv("Invalid warehouse id format: {0}", id);
      throw new WebApplicationException("Invalid warehouse id: " + id, 400);
    }
  }
}
