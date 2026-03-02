package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * Comprehensive REST API tests for StoreResource.
 */
@QuarkusTest
public class StoreResourceTest {

    // ==================== GET ENDPOINTS ====================

    @Test
    @DisplayName("GET /store - should return all stores")
    void testListAllStores() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/store")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("GET /store/{id} - should return store by ID")
    void testGetStoreById() {
        // Create a store first
        Integer id = createStoreViaApi("Test Store Get", 100);

        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/store/" + id)
        .then()
            .statusCode(200)
            .body("name", equalTo("Test Store Get"));
    }

    @Test
    @DisplayName("GET /store/{id} - should return 404 for non-existent store")
    void testGetStoreByIdNotFound() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/store/99999")
        .then()
            .statusCode(404);
    }

    // ==================== POST ENDPOINTS ====================

    @Test
    @DisplayName("POST /store - should create store successfully")
    void testCreateStore() {
        String requestBody = """
            {
                "name": "New Test Store",
                "quantityProductsInStock": 200
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/store")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(201)))
            .body("name", equalTo("New Test Store"))
            .body("quantityProductsInStock", equalTo(200));
    }

    @Test
    @DisplayName("POST /store - should create store with zero stock")
    void testCreateStoreZeroStock() {
        String requestBody = """
            {
                "name": "Zero Stock Store",
                "quantityProductsInStock": 0
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/store")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(201)))
            .body("quantityProductsInStock", equalTo(0));
    }

    // ==================== PUT ENDPOINTS ====================

    @Test
    @DisplayName("PUT /store/{id} - should update store successfully")
    void testUpdateStore() {
        // Create a store first
        Integer id = createStoreViaApi("Store To Update", 50);

        String updateBody = """
            {
                "name": "Updated Store Name",
                "quantityProductsInStock": 150
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(updateBody)
        .when()
            .put("/store/" + id)
        .then()
            .statusCode(200)
            .body("name", equalTo("Updated Store Name"));
    }

    @Test
    @DisplayName("PUT /store/{id} - should return 404 for non-existent store")
    void testUpdateStoreNotFound() {
        String updateBody = """
            {
                "name": "Updated Store",
                "quantityProductsInStock": 100
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(updateBody)
        .when()
            .put("/store/99999")
        .then()
            .statusCode(404);
    }

    // ==================== PATCH ENDPOINTS ====================

    @Test
    @DisplayName("PATCH /store/{id} - should partially update store")
    void testPatchStore() {
        // Create a store first
        Integer id = createStoreViaApi("Store To Patch", 75);

        String patchBody = """
            {
                "name": "Store To Patch",
                "quantityProductsInStock": 300
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(patchBody)
        .when()
            .patch("/store/" + id)
        .then()
            .statusCode(200)
            .body("quantityProductsInStock", equalTo(300));
    }

    // ==================== DELETE ENDPOINTS ====================

    @Test
    @DisplayName("DELETE /store/{id} - should delete store successfully")
    void testDeleteStore() {
        // Create a store first
        Integer id = createStoreViaApi("Store To Delete", 25);

        // Delete it
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/store/" + id)
        .then()
            .statusCode(204);

        // Verify it's gone
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/store/" + id)
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("DELETE /store/{id} - should return 404 for non-existent store")
    void testDeleteStoreNotFound() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/store/99999")
        .then()
            .statusCode(404);
    }

    // ==================== HELPER METHODS ====================

    private Integer createStoreViaApi(String name, int quantity) {
        String requestBody = String.format("""
            {
                "name": "%s",
                "quantityProductsInStock": %d
            }
            """, name, quantity);

        Response response = given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/store")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(201)))
            .extract()
            .response();

        return response.path("id");
    }
}

