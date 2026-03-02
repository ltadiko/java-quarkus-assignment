package com.fulfilment.application.monolith.fulfillment.adapters.rest;

import com.fulfilment.application.monolith.fulfillment.adapters.database.FulfillmentAssignmentRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * REST API tests for FulfillmentAssignmentResource.
 * Tests HTTP endpoints for fulfillment assignment operations.
 */
@QuarkusTest
public class FulfillmentAssignmentResourceTest {

    @Inject
    FulfillmentAssignmentRepository repository;

    @BeforeEach
    void setUp() {
        // Use QuarkusTransaction to ensure the cleanup is committed
        QuarkusTransaction.requiringNew().run(() -> repository.deleteAll());
    }

    // ==================== CREATE ENDPOINT TESTS ====================

    @Test
    @DisplayName("POST /fulfillment-assignments - should create assignment successfully")
    void testCreateSuccess() {
        String requestBody = """
            {
                "warehouseCode": "MWH.001",
                "productId": 1,
                "storeId": 1
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/fulfillment-assignments")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("warehouseCode", equalTo("MWH.001"))
            .body("productId", equalTo(1))
            .body("storeId", equalTo(1))
            .body("createdAt", notNullValue());
    }

    @Test
    @DisplayName("POST /fulfillment-assignments - should return 400 for missing warehouseCode")
    void testCreateMissingWarehouseCode() {
        String requestBody = """
            {
                "productId": 1,
                "storeId": 1
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/fulfillment-assignments")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("POST /fulfillment-assignments - should return 400 for missing productId")
    void testCreateMissingProductId() {
        String requestBody = """
            {
                "warehouseCode": "MWH.001",
                "storeId": 1
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/fulfillment-assignments")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("POST /fulfillment-assignments - should return 404 for non-existent warehouse")
    void testCreateWarehouseNotFound() {
        String requestBody = """
            {
                "warehouseCode": "NONEXISTENT",
                "productId": 1,
                "storeId": 1
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/fulfillment-assignments")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("POST /fulfillment-assignments - should return 404 for non-existent product")
    void testCreateProductNotFound() {
        String requestBody = """
            {
                "warehouseCode": "MWH.001",
                "productId": 99999,
                "storeId": 1
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/fulfillment-assignments")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("POST /fulfillment-assignments - should return 404 for non-existent store")
    void testCreateStoreNotFound() {
        String requestBody = """
            {
                "warehouseCode": "MWH.001",
                "productId": 1,
                "storeId": 99999
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/fulfillment-assignments")
        .then()
            .statusCode(404);
    }

    // ==================== GET ENDPOINT TESTS ====================

    @Test
    @DisplayName("GET /fulfillment-assignments - should return all assignments")
    void testGetAll() {
        // Create via API (not @Transactional) to ensure data is committed
        createAssignmentViaApi("MWH.001", 1, 1);
        createAssignmentViaApi("MWH.012", 2, 1);

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/fulfillment-assignments")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    @DisplayName("GET /fulfillment-assignments/{id} - should return assignment by ID")
    void testGetById() {
        // Create via API and get ID
        Integer id = createAssignmentViaApi("MWH.001", 1, 1);

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/fulfillment-assignments/" + id)
        .then()
            .statusCode(200)
            .body("id", equalTo(id))
            .body("warehouseCode", equalTo("MWH.001"));
    }

    @Test
    @DisplayName("GET /fulfillment-assignments/{id} - should return 404 for non-existent ID")
    void testGetByIdNotFound() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/fulfillment-assignments/99999")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("GET /fulfillment-assignments/by-store/{storeId} - should return assignments by store")
    void testGetByStore() {
        // Create via API
        createAssignmentViaApi("MWH.001", 1, 1);
        createAssignmentViaApi("MWH.012", 2, 1);

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/fulfillment-assignments/by-store/1")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    @DisplayName("GET /fulfillment-assignments/by-warehouse/{code} - should return assignments by warehouse")
    void testGetByWarehouse() {
        // Create via API - same warehouse, different products/stores
        createAssignmentViaApi("MWH.001", 1, 1);
        createAssignmentViaApi("MWH.001", 2, 2);

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/fulfillment-assignments/by-warehouse/MWH.001")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(2));
    }

    @Test
    @DisplayName("GET /fulfillment-assignments/by-product/{productId} - should return assignments by product")
    void testGetByProduct() {
        // Create via API - same product, different warehouses/stores
        createAssignmentViaApi("MWH.001", 1, 1);
        createAssignmentViaApi("MWH.012", 1, 2);

        // Act & Assert
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/fulfillment-assignments/by-product/1")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(2));
    }

    // ==================== DELETE ENDPOINT TESTS ====================

    @Test
    @DisplayName("DELETE /fulfillment-assignments/{id} - should delete assignment")
    void testDelete() {
        // Create via API and get ID
        Integer id = createAssignmentViaApi("MWH.001", 1, 1);

        // Act - Delete
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/fulfillment-assignments/" + id)
        .then()
            .statusCode(204);

        // Verify deleted
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/fulfillment-assignments/" + id)
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("DELETE /fulfillment-assignments/{id} - should return 404 for non-existent ID")
    void testDeleteNotFound() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/fulfillment-assignments/99999")
        .then()
            .statusCode(404);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates an assignment via the REST API and returns its ID.
     * This ensures the data is committed and visible to subsequent API calls.
     */
    private Integer createAssignmentViaApi(String warehouseCode, int productId, int storeId) {
        String requestBody = String.format("""
            {
                "warehouseCode": "%s",
                "productId": %d,
                "storeId": %d
            }
            """, warehouseCode, productId, storeId);

        Response response = given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/fulfillment-assignments")
        .then()
            .statusCode(201)
            .extract()
            .response();

        return response.path("id");
    }
}

