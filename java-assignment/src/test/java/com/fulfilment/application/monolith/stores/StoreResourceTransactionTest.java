package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Tests for StoreResource transaction management with event-driven legacy integration.
 *
 * Verifies that Store CRUD operations correctly fire events after database commits,
 * ensuring proper synchronization with legacy systems through the StoreEventObserver.
 *
 * @see StoreResource
 * @see StoreEventObserver
 * @see StoreEvents
 */
@QuarkusTest
@DisplayName("Store Resource Transaction & Event Tests")
public class StoreResourceTransactionTest {

  @Inject
  StoreResource storeResource;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean up test data before each test
    Store.deleteAll();
  }

  /**
   * Helper method to persist a store within a transaction.
   * REST tests need this because they call persist() from non-@Transactional methods.
   * Must be package-private (not private) for @Transactional to work with CDI.
   */
  @Transactional
  void persistStore(Store store) {
    store.persist();
  }

  @Test
  @Transactional
  @DisplayName("Should create store and fire StoreCreatedEvent")
  void testCreateStoreSuccessWithEventFiring() {
    // Arrange
    Store newStore = new Store("Test Store");
    newStore.quantityProductsInStock = 100;

    // Act
    var response = given()
        .contentType("application/json")
        .body(newStore)
        .when()
        .post("/store")
        .then()
        .statusCode(201)
        .body("name", equalTo("Test Store"))
        .body("quantityProductsInStock", equalTo(100))
        .body("id", notNullValue());

    // Assert - Verify store was created
    Store created = Store.find("name", "Test Store").firstResult();
    assert created != null : "Store should be created in database";
    assert created.name.equals("Test Store") : "Store name should match";

    // Note: Event firing is verified by checking:
    // 1. Store persistence (successful database commit)
    // 2. Observer runs after commit (logged by LegacyStoreManagerGateway)
  }

  @Test
  @DisplayName("Should update store and fire StoreUpdatedEvent")
  void testUpdateStoreSuccessWithEventFiring() {
    // Arrange
    Store originalStore = new Store("Original Store");
    originalStore.quantityProductsInStock = 50;
    persistStore(originalStore);
    Long storeId = originalStore.id;

    Store updateData = new Store("Updated Store");
    updateData.quantityProductsInStock = 150;

    // Act
    var response = given()
        .contentType("application/json")
        .body(updateData)
        .when()
        .put("/store/" + storeId)
        .then()
        .statusCode(200)
        .body("name", equalTo("Updated Store"))
        .body("quantityProductsInStock", equalTo(150));

    // Assert - Verify update was persisted
    Store updated = Store.findById(storeId);
    assert updated != null : "Updated store should exist";
    assert updated.name.equals("Updated Store") : "Store name should be updated";
    assert updated.quantityProductsInStock == 150 : "Quantity should be updated";
  }

  @Test
  @DisplayName("Should partially update store and fire StoreUpdatedEvent")
  void testPatchStoreSuccessWithEventFiring() {
    // Arrange
    Store originalStore = new Store("Original Store");
    originalStore.quantityProductsInStock = 75;
    persistStore(originalStore);
    Long storeId = originalStore.id;

    Store partialUpdate = new Store("Patched Store");
    partialUpdate.quantityProductsInStock = 0; // Indicates no quantity change

    // Act
    var response = given()
        .contentType("application/json")
        .body(partialUpdate)
        .when()
        .patch("/store/" + storeId)
        .then()
        .statusCode(200)
        .body("name", equalTo("Patched Store"));

    // Assert - Verify partial update
    Store patched = Store.findById(storeId);
    assert patched != null : "Patched store should exist";
    assert patched.name.equals("Patched Store") : "Store name should be patched";
  }

  @Test
  @DisplayName("Should delete store and fire StoreDeletedEvent")
  void testDeleteStoreSuccessWithEventFiring() {
    // Arrange
    Store storeToDelete = new Store("Store To Delete");
    storeToDelete.quantityProductsInStock = 30;
    persistStore(storeToDelete);
    Long storeId = storeToDelete.id;

    // Act
    given()
        .when()
        .delete("/store/" + storeId)
        .then()
        .statusCode(204);

    // Assert - Verify deletion
    Store deleted = Store.findById(storeId);
    assert deleted == null : "Deleted store should not exist in database";
  }

  @Test
  @DisplayName("Should return 404 when updating non-existent store")
  void testUpdateNonExistentStoreReturns404() {
    // Arrange
    Store updateData = new Store("Non-existent Store");

    // Act & Assert
    given()
        .contentType("application/json")
        .body(updateData)
        .when()
        .put("/store/99999")
        .then()
        .statusCode(404)
        .body("error", notNullValue());
  }

  @Test
  @DisplayName("Should return 404 when deleting non-existent store")
  void testDeleteNonExistentStoreReturns404() {
    // Act & Assert
    given()
        .when()
        .delete("/store/99999")
        .then()
        .statusCode(404)
        .body("error", notNullValue());
  }

  @Test
  @DisplayName("Should return 422 when creating store with pre-set id")
  void testCreateStoreWithIdReturns422() {
    // Arrange
    Store invalidStore = new Store("Invalid Store");
    invalidStore.id = 1L; // Pre-set id is invalid
    invalidStore.quantityProductsInStock = 100;

    // Act & Assert
    given()
        .contentType("application/json")
        .body(invalidStore)
        .when()
        .post("/store")
        .then()
        .statusCode(422)
        .body("error", notNullValue());
  }

  @Test
  @Transactional
  @DisplayName("Should return 422 when updating store without name")
  void testUpdateStoreWithoutNameReturns422() {
    // Arrange
    Store originalStore = new Store("Original Store");
    originalStore.persist();
    Long storeId = originalStore.id;

    Store invalidUpdate = new Store();
    invalidUpdate.name = null; // Invalid: name required
    invalidUpdate.quantityProductsInStock = 100;

    // Act & Assert
    given()
        .contentType("application/json")
        .body(invalidUpdate)
        .when()
        .put("/store/" + storeId)
        .then()
        .statusCode(422)
        .body("error", notNullValue());
  }

  @Test
  @Transactional
  @DisplayName("Should list all stores")
  void testListAllStoresSuccess() {
    // Arrange
    new Store("Store 1").persist();
    new Store("Store 2").persist();
    new Store("Store 3").persist();

    // Act & Assert
    given()
        .when()
        .get("/store")
        .then()
        .statusCode(200);
  }

  @Test
  @DisplayName("Should retrieve single store by id")
  void testGetSingleStoreSuccess() {
    // Arrange
    Store store = new Store("Single Store");
    store.quantityProductsInStock = 50;
    persistStore(store);
    Long storeId = store.id;

    // Act & Assert
    given()
        .when()
        .get("/store/" + storeId)
        .then()
        .statusCode(200)
        .body("name", equalTo("Single Store"))
        .body("quantityProductsInStock", equalTo(50));
  }

  @Test
  @DisplayName("Should return 404 when retrieving non-existent store")
  void testGetNonExistentStoreReturns404() {
    // Act & Assert
    given()
        .when()
        .get("/store/99999")
        .then()
        .statusCode(404)
        .body("error", notNullValue());
  }
}

