package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StoreEventObserver.
 * Tests that store events are properly observed and processed.
 */
@QuarkusTest
public class StoreEventObserverTest {

    @Inject
    Event<StoreEvents.StoreCreatedEvent> storeCreatedEvent;

    @Inject
    Event<StoreEvents.StoreUpdatedEvent> storeUpdatedEvent;

    @Inject
    Event<StoreEvents.StoreDeletedEvent> storeDeletedEvent;

    @Inject
    StoreEventObserver observer;

    // ==================== EVENT CREATION TESTS ====================

    @Test
    @DisplayName("Should create StoreCreatedEvent with correct data")
    void testStoreCreatedEvent() {
        // Arrange
        Store store = new Store("Test Store");
        store.id = 1L;
        store.quantityProductsInStock = 100;

        // Act
        StoreEvents.StoreCreatedEvent event = new StoreEvents.StoreCreatedEvent(store);

        // Assert
        assertNotNull(event);
        assertEquals("Test Store", event.store.name);
        assertEquals(100, event.store.quantityProductsInStock);
    }

    @Test
    @DisplayName("Should create StoreUpdatedEvent with correct data")
    void testStoreUpdatedEvent() {
        // Arrange
        Store store = new Store("Updated Store");
        store.id = 2L;
        store.quantityProductsInStock = 200;

        // Act
        StoreEvents.StoreUpdatedEvent event = new StoreEvents.StoreUpdatedEvent(store);

        // Assert
        assertNotNull(event);
        assertEquals("Updated Store", event.store.name);
        assertEquals(200, event.store.quantityProductsInStock);
    }

    @Test
    @DisplayName("Should create StoreDeletedEvent with correct ID")
    void testStoreDeletedEvent() {
        // Act
        StoreEvents.StoreDeletedEvent event = new StoreEvents.StoreDeletedEvent(123L);

        // Assert
        assertNotNull(event);
        assertEquals(123L, event.storeId);
    }

    // ==================== EVENT FIRING TESTS ====================

    @Test
    @DisplayName("Should fire StoreCreatedEvent without errors")
    void testFireStoreCreatedEvent() {
        // Arrange
        Store store = new Store("Fire Test Store");
        store.id = 10L;
        store.quantityProductsInStock = 50;

        // Act & Assert - should not throw
        assertDoesNotThrow(() -> storeCreatedEvent.fire(new StoreEvents.StoreCreatedEvent(store)));
    }

    @Test
    @DisplayName("Should fire StoreUpdatedEvent without errors")
    void testFireStoreUpdatedEvent() {
        // Arrange
        Store store = new Store("Fire Update Store");
        store.id = 20L;
        store.quantityProductsInStock = 75;

        // Act & Assert - should not throw
        assertDoesNotThrow(() -> storeUpdatedEvent.fire(new StoreEvents.StoreUpdatedEvent(store)));
    }

    @Test
    @DisplayName("Should fire StoreDeletedEvent without errors")
    void testFireStoreDeletedEvent() {
        // Act & Assert - should not throw
        assertDoesNotThrow(() -> storeDeletedEvent.fire(new StoreEvents.StoreDeletedEvent(30L)));
    }

    // ==================== OBSERVER EXISTENCE TESTS ====================

    @Test
    @DisplayName("StoreEventObserver should be injectable")
    void testObserverIsInjectable() {
        assertNotNull(observer, "StoreEventObserver should be injectable");
    }
}
