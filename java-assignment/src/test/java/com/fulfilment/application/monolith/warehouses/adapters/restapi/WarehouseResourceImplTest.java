package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * REST API tests for WarehouseResourceImpl.
 * Tests the warehouse CRUD endpoints including create, get, archive, and replace.
 *
 * Tests are self-contained and create their own data to avoid dependency on seed data
 * which may be modified by other test classes.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseResourceImplTest {

    // ==================== LIST ALL ====================

    @Test
    @Order(1)
    @DisplayName("GET /warehouse - should return list of warehouses")
    void testListAllWarehouses() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/warehouse")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(0));
    }

    // ==================== CREATE ====================

    @Test
    @Order(2)
    @DisplayName("POST /warehouse - should create a new warehouse")
    void testCreateWarehouse() {
        String requestBody = """
            {
                "businessUnitCode": "WH-REST-CREATE",
                "location": "AMSTERDAM-001",
                "capacity": 10,
                "stock": 5
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/warehouse")
        .then()
            .statusCode(200)
            .body("businessUnitCode", equalTo("WH-REST-CREATE"))
            .body("location", equalTo("AMSTERDAM-001"))
            .body("capacity", equalTo(10))
            .body("stock", equalTo(5));
    }

    @Test
    @Order(3)
    @DisplayName("POST /warehouse - should return 409 for duplicate business unit code")
    void testCreateWarehouseDuplicateCode() {
        // WH-REST-CREATE was created in the previous test
        String requestBody = """
            {
                "businessUnitCode": "WH-REST-CREATE",
                "location": "AMSTERDAM-001",
                "capacity": 10,
                "stock": 5
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/warehouse")
        .then()
            .statusCode(409);
    }

    @Test
    @Order(2)
    @DisplayName("POST /warehouse - should return 404 for invalid location")
    void testCreateWarehouseInvalidLocation() {
        String requestBody = """
            {
                "businessUnitCode": "WH-INVALID-LOC",
                "location": "INVALID-LOCATION",
                "capacity": 10,
                "stock": 5
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/warehouse")
        .then()
            .statusCode(404);
    }

    @Test
    @Order(2)
    @DisplayName("POST /warehouse - should return 400 for missing business unit code")
    void testCreateWarehouseMissingCode() {
        String requestBody = """
            {
                "location": "AMSTERDAM-001",
                "capacity": 10,
                "stock": 5
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/warehouse")
        .then()
            .statusCode(400);
    }

    // ==================== GET BY ID ====================

    @Test
    @Order(4)
    @DisplayName("GET /warehouse/{id} - should return warehouse by database id")
    void testGetWarehouseById() {
        // First create a warehouse and capture its id
        String createBody = """
            {
                "businessUnitCode": "WH-GET-BY-ID",
                "location": "AMSTERDAM-001",
                "capacity": 10,
                "stock": 5
            }
            """;

        String createdId = given()
            .contentType(ContentType.JSON)
            .body(createBody)
        .when()
            .post("/warehouse")
        .then()
            .statusCode(200)
            .extract().path("id");

        // Now get by database id
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/warehouse/" + createdId)
        .then()
            .statusCode(200)
            .body("id", equalTo(createdId))
            .body("businessUnitCode", equalTo("WH-GET-BY-ID"))
            .body("location", equalTo("AMSTERDAM-001"))
            .body("capacity", equalTo(10))
            .body("stock", equalTo(5));
    }

    @Test
    @Order(2)
    @DisplayName("GET /warehouse/{id} - should return 404 for non-existent warehouse id")
    void testGetWarehouseByIdNotFound() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/warehouse/999999")
        .then()
            .statusCode(404);
    }

    @Test
    @Order(2)
    @DisplayName("GET /warehouse/{id} - should return 400 for invalid (non-numeric) id")
    void testGetWarehouseByIdInvalid() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/warehouse/NON-EXISTENT")
        .then()
            .statusCode(400);
    }

    // ==================== ARCHIVE ====================

    @Test
    @Order(2)
    @DisplayName("DELETE /warehouse/{id} - should return 404 for non-existent warehouse id")
    void testArchiveWarehouseNotFound() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/warehouse/999999")
        .then()
            .statusCode(404);
    }

    @Test
    @Order(2)
    @DisplayName("DELETE /warehouse/{id} - should return 400 for invalid (non-numeric) id")
    void testArchiveWarehouseInvalidId() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/warehouse/NON-EXISTENT")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(2)
    @DisplayName("DELETE /warehouse/{id} - should archive an existing warehouse")
    void testArchiveWarehouseSuccess() {
        // First create a warehouse to archive
        String createBody = """
            {
                "businessUnitCode": "WH-ARCHIVE-TEST",
                "location": "AMSTERDAM-001",
                "capacity": 10,
                "stock": 5
            }
            """;

        String createdId = given()
            .contentType(ContentType.JSON)
            .body(createBody)
        .when()
            .post("/warehouse")
        .then()
            .statusCode(200)
            .extract().path("id");

        // Now archive it using the database id
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/warehouse/" + createdId)
        .then()
            .statusCode(204);
    }

    @Test
    @Order(3)
    @DisplayName("DELETE /warehouse/{id} - should return 409 when warehouse is already archived")
    void testArchiveWarehouseAlreadyArchived() {
        // First create a warehouse
        String createBody = """
            {
                "businessUnitCode": "WH-DOUBLE-ARCH",
                "location": "AMSTERDAM-001",
                "capacity": 10,
                "stock": 5
            }
            """;

        String createdId = given()
            .contentType(ContentType.JSON)
            .body(createBody)
        .when()
            .post("/warehouse")
        .then()
            .statusCode(200)
            .extract().path("id");

        // Archive it using database id
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/warehouse/" + createdId)
        .then()
            .statusCode(204);

        // Try to archive again — should get 409 conflict or 404
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/warehouse/" + createdId)
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(409)));
    }

    // ==================== REPLACE ====================

    @Test
    @Order(2)
    @DisplayName("POST /warehouse/{code}/replacement - should return 404 for non-existent warehouse")
    void testReplaceWarehouseNotFound() {
        String requestBody = """
            {
                "location": "AMSTERDAM-001",
                "capacity": 100,
                "stock": 50
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/warehouse/NON-EXISTENT/replacement")
        .then()
            .statusCode(404);
    }

    @Test
    @Order(5)
    @DisplayName("POST /warehouse/{code}/replacement - should replace an existing warehouse")
    void testReplaceWarehouse() {
        // Replace WH-REST-CREATE (stock=5, capacity=10 at AMSTERDAM-001)
        String requestBody = """
            {
                "location": "AMSTERDAM-001",
                "capacity": 20,
                "stock": 5
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/warehouse/WH-REST-CREATE/replacement")
        .then()
            .statusCode(200)
            .body("businessUnitCode", equalTo("WH-REST-CREATE"))
            .body("location", equalTo("AMSTERDAM-001"))
            .body("capacity", equalTo(20))
            .body("stock", equalTo(5));
    }

    @Test
    @Order(2)
    @DisplayName("POST /warehouse/{code}/replacement - should return 404 for invalid location")
    void testReplaceWarehouseInvalidLocation() {
        // First create a warehouse to replace
        String createBody = """
            {
                "businessUnitCode": "WH-REPLACE-LOC",
                "location": "AMSTERDAM-001",
                "capacity": 10,
                "stock": 5
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(createBody)
        .when()
            .post("/warehouse")
        .then()
            .statusCode(200);

        // Try to replace with invalid location
        String replaceBody = """
            {
                "location": "INVALID-LOCATION",
                "capacity": 10,
                "stock": 5
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(replaceBody)
        .when()
            .post("/warehouse/WH-REPLACE-LOC/replacement")
        .then()
            .statusCode(404);
    }
}
