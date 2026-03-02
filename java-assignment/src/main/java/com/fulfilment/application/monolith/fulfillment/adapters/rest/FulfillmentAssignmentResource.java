package com.fulfilment.application.monolith.fulfillment.adapters.rest;

import com.fulfilment.application.monolith.fulfillment.adapters.database.FulfillmentAssignmentRepository;
import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;
import com.fulfilment.application.monolith.fulfillment.domain.usecases.CreateFulfillmentAssignmentUseCase;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

/**
 * REST resource for managing fulfillment assignments.
 *
 * Provides endpoints to create, retrieve, and delete fulfillment assignments
 * that link warehouses to products for specific stores.
 *
 * Business Rules:
 * - Each Product can be fulfilled by max 2 different Warehouses per Store
 * - Each Store can be fulfilled by max 3 different Warehouses
 * - Each Warehouse can store max 5 types of Products
 */
@Path("/fulfillment-assignments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Fulfillment Assignments", description = "Manage warehouse-product-store fulfillment relationships")
public class FulfillmentAssignmentResource {

    private static final Logger LOGGER = Logger.getLogger(FulfillmentAssignmentResource.class);

    @Inject
    CreateFulfillmentAssignmentUseCase createUseCase;

    @Inject
    FulfillmentAssignmentRepository repository;

    /**
     * Creates a new fulfillment assignment.
     *
     * @param request the assignment request containing warehouseCode, productId, storeId
     * @return the created assignment
     */
    @POST
    @Operation(summary = "Create fulfillment assignment",
               description = "Links a warehouse to a product for a specific store")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Assignment created successfully"),
        @APIResponse(responseCode = "400", description = "Invalid request"),
        @APIResponse(responseCode = "404", description = "Warehouse, product, or store not found"),
        @APIResponse(responseCode = "409", description = "Business rule violation (e.g., max assignments reached)")
    })
    public Response create(FulfillmentAssignmentRequest request) {
        LOGGER.infov("Received request to create fulfillment assignment: {0}", request);

        try {
            validateRequest(request);

            FulfillmentAssignment assignment = createUseCase.create(
                request.warehouseCode,
                request.productId,
                request.storeId
            );

            return Response.status(Response.Status.CREATED)
                .entity(FulfillmentAssignmentResponse.from(assignment))
                .build();

        } catch (EntityNotFoundException e) {
            LOGGER.errorv("Entity not found: {0}", e.getMessage());
            throw new WebApplicationException(e.getMessage(), Response.Status.NOT_FOUND);
        } catch (IllegalStateException e) {
            LOGGER.errorv("Business rule violation: {0}", e.getMessage());
            throw new WebApplicationException(e.getMessage(), Response.Status.CONFLICT);
        } catch (IllegalArgumentException e) {
            LOGGER.errorv("Invalid request: {0}", e.getMessage());
            throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        }
    }

    /**
     * Retrieves all fulfillment assignments.
     */
    @GET
    @Operation(summary = "List all fulfillment assignments")
    @APIResponse(responseCode = "200", description = "List of all assignments")
    public List<FulfillmentAssignmentResponse> getAll() {
        return repository.getAll().stream()
            .map(FulfillmentAssignmentResponse::from)
            .toList();
    }

    /**
     * Retrieves a fulfillment assignment by ID.
     */
    @GET
    @Path("/{id}")
    @Operation(summary = "Get fulfillment assignment by ID")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Assignment found"),
        @APIResponse(responseCode = "404", description = "Assignment not found")
    })
    public FulfillmentAssignmentResponse getById(
            @Parameter(description = "Assignment ID") @PathParam("id") Long id) {
        FulfillmentAssignment assignment = repository.findAssignmentById(id);
        if (assignment == null) {
            throw new WebApplicationException(
                "Fulfillment assignment with ID " + id + " not found",
                Response.Status.NOT_FOUND
            );
        }
        return FulfillmentAssignmentResponse.from(assignment);
    }

    /**
     * Retrieves assignments by store ID.
     */
    @GET
    @Path("/by-store/{storeId}")
    public List<FulfillmentAssignmentResponse> getByStore(@PathParam("storeId") Long storeId) {
        return repository.findByStoreId(storeId).stream()
            .map(FulfillmentAssignmentResponse::from)
            .toList();
    }

    /**
     * Retrieves assignments by warehouse code.
     */
    @GET
    @Path("/by-warehouse/{warehouseCode}")
    public List<FulfillmentAssignmentResponse> getByWarehouse(@PathParam("warehouseCode") String warehouseCode) {
        return repository.findByWarehouseCode(warehouseCode).stream()
            .map(FulfillmentAssignmentResponse::from)
            .toList();
    }

    /**
     * Retrieves assignments by product ID.
     */
    @GET
    @Path("/by-product/{productId}")
    public List<FulfillmentAssignmentResponse> getByProduct(@PathParam("productId") Long productId) {
        return repository.findByProductId(productId).stream()
            .map(FulfillmentAssignmentResponse::from)
            .toList();
    }

    /**
     * Deletes a fulfillment assignment by ID.
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        try {
            repository.removeById(id);
            return Response.noContent().build();
        } catch (EntityNotFoundException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.NOT_FOUND);
        }
    }

    private void validateRequest(FulfillmentAssignmentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (request.warehouseCode == null || request.warehouseCode.isBlank()) {
            throw new IllegalArgumentException("warehouseCode is required");
        }
        if (request.productId == null) {
            throw new IllegalArgumentException("productId is required");
        }
        if (request.storeId == null) {
            throw new IllegalArgumentException("storeId is required");
        }
    }

    /**
     * Request DTO for creating fulfillment assignments.
     */
    public static class FulfillmentAssignmentRequest {
        public String warehouseCode;
        public Long productId;
        public Long storeId;

        @Override
        public String toString() {
            return "FulfillmentAssignmentRequest{" +
                "warehouseCode='" + warehouseCode + '\'' +
                ", productId=" + productId +
                ", storeId=" + storeId +
                '}';
        }
    }

    /**
     * Response DTO for fulfillment assignments.
     */
    public static class FulfillmentAssignmentResponse {
        public Long id;
        public String warehouseCode;
        public Long productId;
        public Long storeId;
        public String createdAt;

        public static FulfillmentAssignmentResponse from(FulfillmentAssignment assignment) {
            FulfillmentAssignmentResponse response = new FulfillmentAssignmentResponse();
            response.id = assignment.id;
            response.warehouseCode = assignment.warehouseCode;
            response.productId = assignment.productId;
            response.storeId = assignment.storeId;
            response.createdAt = assignment.createdAt != null ? assignment.createdAt.toString() : null;
            return response;
        }
    }
}

