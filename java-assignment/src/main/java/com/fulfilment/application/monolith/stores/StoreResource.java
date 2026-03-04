package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * REST resource for managing stores.
 *
 * This resource handles CRUD operations for stores and fires events for lifecycle
 * changes. Events are observed by StoreEventObserver to ensure legacy system
 * synchronization happens after database commits.
 *
 * @see StoreEvents
 * @see StoreEventObserver
 */
@Path("store")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class StoreResource {

  @Inject Event<StoreEvents.StoreCreatedEvent> storeCreatedEvent;
  @Inject Event<StoreEvents.StoreUpdatedEvent> storeUpdatedEvent;
  @Inject Event<StoreEvents.StoreDeletedEvent> storeDeletedEvent;

  private static final Logger LOGGER = Logger.getLogger(StoreResource.class.getName());

  @GET
  public List<Store> get() {
    LOGGER.info("Received request to list all stores");
    List<Store> stores = Store.listAll(Sort.by("name"));
    LOGGER.infov("Returning {0} store(s)", stores.size());
    return stores;
  }

  @GET
  @Path("{id}")
  public Store getSingle(Long id) {
    LOGGER.infov("Received request to get store with id: {0}", id);
    Store entity = Store.findById(id);
    if (entity == null) {
      LOGGER.warnv("Store with id {0} not found", id);
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    LOGGER.infov("Found store with id: {0}, name: {1}", id, entity.name);
    return entity;
  }

  /**
   * Creates a new store.
   *
   * Fires a StoreCreatedEvent after persistence to trigger legacy system
   * synchronization through the event observer.
   *
   * @param store the store to create, id must be null
   * @return response with created store and 201 status
   * @throws WebApplicationException if id is already set (422)
   */
  @POST
  @Transactional
  public Response create(Store store) {
    LOGGER.infov("Received request to create store with name: {0}", store.name);
    if (store.id != null) {
      LOGGER.warnv("Bad request: id was invalidly set on create request");
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }

    store.persist();
    storeCreatedEvent.fire(new StoreEvents.StoreCreatedEvent(store));
    LOGGER.infov("Successfully created store with id: {0}, name: {1}", store.id, store.name);

    return Response.ok(store).status(201).build();
  }

  /**
   * Updates an existing store with all fields.
   *
   * Fires a StoreUpdatedEvent after update to trigger legacy system
   * synchronization through the event observer.
   *
   * @param id the store id
   * @param updatedStore the updated store data, name must not be null
   * @return the updated store
   * @throws WebApplicationException if store name is null (422) or store not found (404)
   */
  @PUT
  @Path("{id}")
  @Transactional
  public Store update(Long id, Store updatedStore) {
    LOGGER.infov("Received request to update store with id: {0}", id);
    if (updatedStore.name == null) {
      LOGGER.warnv("Bad request: store name was not set on update request for id: {0}", id);
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }

    Store entity = Store.findById(id);

    if (entity == null) {
      LOGGER.warnv("Store with id {0} not found for update", id);
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }

    entity.name = updatedStore.name;
    entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
    storeUpdatedEvent.fire(new StoreEvents.StoreUpdatedEvent(entity));
    LOGGER.infov("Successfully updated store with id: {0}, name: {1}", id, entity.name);

    return entity;
  }

  /**
   * Partially updates an existing store (patch operation).
   *
   * Fires a StoreUpdatedEvent after update to trigger legacy system
   * synchronization through the event observer.
   *
   * @param id the store id
   * @param updatedStore the partial store data, name must not be null
   * @return the updated store
   * @throws WebApplicationException if store name is null (422) or store not found (404)
   */
  @PATCH
  @Path("{id}")
  @Transactional
  public Store patch(Long id, Store updatedStore) {
    LOGGER.infov("Received request to patch store with id: {0}", id);
    if (updatedStore.name == null) {
      LOGGER.warnv("Bad request: store name was not set on patch request for id: {0}", id);
      throw new WebApplicationException("Store Name was not set on request.", 422);
    }

    Store entity = Store.findById(id);

    if (entity == null) {
      LOGGER.warnv("Store with id {0} not found for patch", id);
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }

    if (entity.name != null) {
      entity.name = updatedStore.name;
    }

    if (entity.quantityProductsInStock != 0) {
      entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
    }

    storeUpdatedEvent.fire(new StoreEvents.StoreUpdatedEvent(entity));
    LOGGER.infov("Successfully patched store with id: {0}, name: {1}", id, entity.name);

    return entity;
  }

  /**
   * Deletes an existing store.
   *
   * Fires a StoreDeletedEvent after deletion to trigger legacy system
   * synchronization through the event observer.
   *
   * @param id the store id to delete
   * @return 204 No Content response
   * @throws WebApplicationException if store not found (404)
   */
  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    LOGGER.infov("Received request to delete store with id: {0}", id);
    Store entity = Store.findById(id);
    if (entity == null) {
      LOGGER.warnv("Store with id {0} not found for deletion", id);
      throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
    }
    entity.delete();
    storeDeletedEvent.fire(new StoreEvents.StoreDeletedEvent(id));
    LOGGER.infov("Successfully deleted store with id: {0}", id);
    return Response.status(204).build();
  }

  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Inject ObjectMapper objectMapper;

    @Override
    public Response toResponse(Exception exception) {
      LOGGER.error("Failed to handle request", exception);

      int code = 500;
      if (exception instanceof WebApplicationException) {
        code = ((WebApplicationException) exception).getResponse().getStatus();
      }

      ObjectNode exceptionJson = objectMapper.createObjectNode();
      exceptionJson.put("exceptionType", exception.getClass().getName());
      exceptionJson.put("code", code);

      if (exception.getMessage() != null) {
        exceptionJson.put("error", exception.getMessage());
      }

      return Response.status(code).entity(exceptionJson).build();
    }
  }
}
