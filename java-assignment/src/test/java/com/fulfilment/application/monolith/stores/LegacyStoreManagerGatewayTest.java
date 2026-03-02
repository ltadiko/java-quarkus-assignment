package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LegacyStoreManagerGateway.
 * Tests the gateway that communicates with the legacy system.
 */
@QuarkusTest
public class LegacyStoreManagerGatewayTest {

    @Inject
    LegacyStoreManagerGateway gateway;

    @Test
    @DisplayName("Gateway should be injectable")
    void testGatewayInjectable() {
        assertNotNull(gateway);
    }

    @Test
    @DisplayName("Should create store on legacy system without error")
    void testCreateStoreOnLegacySystem() {
        Store store = new Store("Test Legacy Create");
        store.quantityProductsInStock = 100;

        // Should not throw any exception
        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
    }

    @Test
    @DisplayName("Should update store on legacy system without error")
    void testUpdateStoreOnLegacySystem() {
        Store store = new Store("Test Legacy Update");
        store.quantityProductsInStock = 200;

        // Should not throw any exception
        assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
    }

    @Test
    @DisplayName("Should handle store with empty name")
    void testCreateStoreEmptyName() {
        Store store = new Store("");
        store.quantityProductsInStock = 50;

        // Should not throw any exception
        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
    }

    @Test
    @DisplayName("Should handle store with zero stock")
    void testCreateStoreZeroStock() {
        Store store = new Store("Zero Stock");
        store.quantityProductsInStock = 0;

        // Should not throw any exception
        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
    }

    @Test
    @DisplayName("Should handle store with special characters in name")
    void testCreateStoreSpecialChars() {
        Store store = new Store("Store #1 - Test (Main)");
        store.quantityProductsInStock = 75;

        // Should not throw any exception
        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
    }

    @Test
    @DisplayName("Should handle store with large stock value")
    void testCreateStoreLargeStock() {
        Store store = new Store("Large Stock Store");
        store.quantityProductsInStock = Integer.MAX_VALUE;

        // Should not throw any exception
        assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
    }

    @Test
    @DisplayName("Should handle multiple create calls")
    void testMultipleCreates() {
        Store store1 = new Store("Multi Store 1");
        store1.quantityProductsInStock = 10;

        Store store2 = new Store("Multi Store 2");
        store2.quantityProductsInStock = 20;

        assertDoesNotThrow(() -> {
            gateway.createStoreOnLegacySystem(store1);
            gateway.createStoreOnLegacySystem(store2);
        });
    }

    @Test
    @DisplayName("Should handle multiple update calls")
    void testMultipleUpdates() {
        Store store = new Store("Update Multiple");
        store.quantityProductsInStock = 100;

        assertDoesNotThrow(() -> {
            gateway.updateStoreOnLegacySystem(store);
            store.quantityProductsInStock = 200;
            gateway.updateStoreOnLegacySystem(store);
            store.quantityProductsInStock = 300;
            gateway.updateStoreOnLegacySystem(store);
        });
    }
}

