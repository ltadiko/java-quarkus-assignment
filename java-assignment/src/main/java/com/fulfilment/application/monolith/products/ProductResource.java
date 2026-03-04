package com.fulfilment.application.monolith.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
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

@Path("product")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class ProductResource {

  @Inject ProductRepository productRepository;

  private static final Logger LOGGER = Logger.getLogger(ProductResource.class.getName());

  @GET
  public List<Product> get() {
    LOGGER.info("Received request to list all products");
    var products = productRepository.listAll(Sort.by("name"));
    LOGGER.infov("Returning {0} product(s)", products.size());
    return products;
  }

  @GET
  @Path("{id}")
  public Product getSingle(Long id) {
    LOGGER.infov("Received request to get product with id: {0}", id);
    Product entity = productRepository.findById(id);
    if (entity == null) {
      LOGGER.warnv("Product with id {0} not found", id);
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }
    LOGGER.infov("Found product with id: {0}, name: {1}", id, entity.name);
    return entity;
  }

  @POST
  @Transactional
  public Response create(Product product) {
    LOGGER.infov("Received request to create product with name: {0}", product.name);
    if (product.id != null) {
      LOGGER.warnv("Bad request: id was invalidly set on create request");
      throw new WebApplicationException("Id was invalidly set on request.", 422);
    }

    productRepository.persist(product);
    LOGGER.infov("Successfully created product with id: {0}, name: {1}", product.id, product.name);
    return Response.ok(product).status(201).build();
  }

  @PUT
  @Path("{id}")
  @Transactional
  public Product update(Long id, Product product) {
    LOGGER.infov("Received request to update product with id: {0}", id);
    if (product.name == null) {
      LOGGER.warnv("Bad request: product name was not set on update request for id: {0}", id);
      throw new WebApplicationException("Product Name was not set on request.", 422);
    }

    Product entity = productRepository.findById(id);

    if (entity == null) {
      LOGGER.warnv("Product with id {0} not found for update", id);
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }

    entity.name = product.name;
    entity.description = product.description;
    entity.price = product.price;
    entity.stock = product.stock;

    productRepository.persist(entity);
    LOGGER.infov("Successfully updated product with id: {0}, name: {1}", id, entity.name);

    return entity;
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(Long id) {
    LOGGER.infov("Received request to delete product with id: {0}", id);
    Product entity = productRepository.findById(id);
    if (entity == null) {
      LOGGER.warnv("Product with id {0} not found for deletion", id);
      throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
    }
    productRepository.delete(entity);
    LOGGER.infov("Successfully deleted product with id: {0}", id);
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
