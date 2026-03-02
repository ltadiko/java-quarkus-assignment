package com.fulfilment.application.monolith.stores;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Store entity model.
 */
public class StoreModelTest {

    @Test
    @DisplayName("Should create store with default constructor")
    void testDefaultConstructor() {
        Store store = new Store();

        assertNull(store.name);
        assertEquals(0, store.quantityProductsInStock);
    }

    @Test
    @DisplayName("Should create store with name constructor")
    void testNameConstructor() {
        Store store = new Store("Test Store");

        assertEquals("Test Store", store.name);
        assertEquals(0, store.quantityProductsInStock);
    }

    @Test
    @DisplayName("Should set all fields")
    void testSetAllFields() {
        Store store = new Store();
        store.name = "Complete Store";
        store.quantityProductsInStock = 500;

        assertEquals("Complete Store", store.name);
        assertEquals(500, store.quantityProductsInStock);
    }

    @Test
    @DisplayName("Should handle zero stock")
    void testZeroStock() {
        Store store = new Store("Zero Stock Store");
        store.quantityProductsInStock = 0;

        assertEquals(0, store.quantityProductsInStock);
    }

    @Test
    @DisplayName("Should handle large stock values")
    void testLargeStockValues() {
        Store store = new Store("Large Stock Store");
        store.quantityProductsInStock = Integer.MAX_VALUE;

        assertEquals(Integer.MAX_VALUE, store.quantityProductsInStock);
    }

    @Test
    @DisplayName("Should handle empty name")
    void testEmptyName() {
        Store store = new Store("");

        assertEquals("", store.name);
    }

    @Test
    @DisplayName("Should handle special characters in name")
    void testSpecialCharactersInName() {
        Store store = new Store("Store #1 - Main (Downtown)");

        assertEquals("Store #1 - Main (Downtown)", store.name);
    }

    @Test
    @DisplayName("Should handle long name (max 40 chars)")
    void testMaxLengthName() {
        String maxName = "A".repeat(40);
        Store store = new Store(maxName);

        assertEquals(40, store.name.length());
    }

    @Test
    @DisplayName("Name and stock should be modifiable")
    void testFieldsModifiable() {
        Store store = new Store("Original Name");
        store.quantityProductsInStock = 100;

        // Modify fields
        store.name = "Modified Name";
        store.quantityProductsInStock = 200;

        assertEquals("Modified Name", store.name);
        assertEquals(200, store.quantityProductsInStock);
    }
}

