package com.fulfilment.application.monolith.products;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * Comprehensive tests for ProductResource REST API.
 */
@QuarkusTest
public class ProductResourceTest {


    // ==================== GET ENDPOINTS ====================

    @Test
    @DisplayName("GET /product - should return all products")
    void testListAllProducts() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/product")
        .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("GET /product/{id} - should return product by ID")
    void testGetProductById() {
        // First create a product
        Integer id = createProductViaApi("Test Product Get", "Description", 29.99, 50);

        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/product/" + id)
        .then()
            .statusCode(200)
            .body("name", equalTo("Test Product Get"));
    }

    @Test
    @DisplayName("GET /product/{id} - should return 404 for non-existent product")
    void testGetProductByIdNotFound() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/product/99999")
        .then()
            .statusCode(404);
    }

    // ==================== POST ENDPOINTS ====================

    @Test
    @DisplayName("POST /product - should create product successfully")
    void testCreateProduct() {
        String requestBody = """
            {
                "name": "New Test Product",
                "description": "A test product",
                "price": 49.99,
                "stock": 100
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/product")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(201)))
            .body("name", equalTo("New Test Product"));
    }

    @Test
    @DisplayName("POST /product - should create product with minimal fields")
    void testCreateProductMinimalFields() {
        String requestBody = """
            {
                "name": "Minimal Product"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/product")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(201)))
            .body("name", equalTo("Minimal Product"));
    }

    // ==================== PUT ENDPOINTS ====================

    @Test
    @DisplayName("PUT /product/{id} - should update product successfully")
    void testUpdateProduct() {
        // First create a product
        Integer id = createProductViaApi("Product To Update", "Original", 19.99, 30);

        String updateBody = """
            {
                "name": "Updated Product Name",
                "description": "Updated description",
                "price": 39.99,
                "stock": 60
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(updateBody)
        .when()
            .put("/product/" + id)
        .then()
            .statusCode(200)
            .body("name", equalTo("Updated Product Name"))
            .body("stock", equalTo(60));
    }

    @Test
    @DisplayName("PUT /product/{id} - should return 404 for non-existent product")
    void testUpdateProductNotFound() {
        String updateBody = """
            {
                "name": "Updated Product",
                "stock": 100
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(updateBody)
        .when()
            .put("/product/99999")
        .then()
            .statusCode(404);
    }

    // ==================== DELETE ENDPOINTS ====================

    @Test
    @DisplayName("DELETE /product/{id} - should delete product successfully")
    void testDeleteProduct() {
        // First create a product
        Integer id = createProductViaApi("Product To Delete", "Will be deleted", 9.99, 10);

        // Delete it
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/product/" + id)
        .then()
            .statusCode(204);

        // Verify it's gone
        given()
            .contentType(ContentType.JSON)
        .when()
            .get("/product/" + id)
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("DELETE /product/{id} - should return 404 for non-existent product")
    void testDeleteProductNotFound() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .delete("/product/99999")
        .then()
            .statusCode(404);
    }

    // ==================== HELPER METHODS ====================

    private Integer createProductViaApi(String name, String description, double price, int stock) {
        String requestBody = String.format("""
            {
                "name": "%s",
                "description": "%s",
                "price": %s,
                "stock": %d
            }
            """, name, description, String.valueOf(price), stock);

        Response response = given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/product")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(201)))
            .extract()
            .response();

        return response.path("id");
    }
}

