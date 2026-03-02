package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * REST API tests for WarehouseResourceImpl.
 * Tests the warehouse endpoints.
 *
 * Note: Many endpoints are not yet implemented and return 500.
 */
@QuarkusTest
public class WarehouseResourceImplTest {

    // ==================== GET LIST ENDPOINT (IMPLEMENTED) ====================

    @Test
    @DisplayName("GET /warehouse - should return all warehouses")
    void testListAllWarehouses() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/warehouse")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(0));
    }

    // ==================== UNIMPLEMENTED ENDPOINTS (Return 500) ====================

    @Test
    @DisplayName("GET /warehouse/{id} - returns 500 (not implemented)")
    void testGetWarehouseByIdNotImplemented() {
        // getAWarehouseUnitByID throws UnsupportedOperationException
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/warehouse/1")
        .then()
            .statusCode(500);
    }

    @Test
    @DisplayName("POST /warehouse - returns 500 (not implemented)")
    void testCreateWarehouseNotImplemented() {
        String requestBody = """
            {
                "businessUnitCode": "WH-REST-TEST-001",
                "location": "AMSTERDAM-001",
                "capacity": 50,
                "stock": 20
            }
            """;

        // createANewWarehouseUnit throws UnsupportedOperationException
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/warehouse")
        .then()
            .statusCode(500);
    }

    @Test
    @DisplayName("DELETE /warehouse/{id} - returns 500 (not implemented)")
    void testArchiveWarehouseNotImplemented() {
        // archiveAWarehouseUnitByID throws UnsupportedOperationException
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/warehouse/99999")
        .then()
            .statusCode(500);
    }

    @Test
    @DisplayName("POST /warehouse/{businessUnitCode}/replacement - returns 500 (not implemented)")
    void testReplaceWarehouseNotImplemented() {
        String requestBody = """
            {
                "businessUnitCode": "WH-NEW",
                "location": "AMSTERDAM-001",
                "capacity": 100,
                "stock": 50
            }
            """;

        // replaceTheCurrentActiveWarehouse throws UnsupportedOperationException
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/warehouse/WH-OLD/replacement")
        .then()
            .statusCode(500);
    }
}
