package com.fulfilment.application.monolith.products;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProductRepository.
 * Tests Panache repository operations for products.
 */
@QuarkusTest
public class ProductRepositoryTest {

    @Inject
    ProductRepository repository;

    // ==================== CREATE TESTS ====================

    @Test
    @Transactional
    @DisplayName("Should persist product successfully")
    void testPersistProduct() {
        Product product = new Product("Test Repository Product");
        product.description = "Test description";
        product.price = new BigDecimal("29.99");
        product.stock = 100;

        repository.persist(product);

        assertNotNull(product.id, "Product should have an ID after persist");
    }

    @Test
    @Transactional
    @DisplayName("Should persist product with constructor")
    void testProductConstructor() {
        Product product = new Product("Constructor Test");

        assertNotNull(product);
        assertEquals("Constructor Test", product.name);
    }

    @Test
    @Transactional
    @DisplayName("Should persist product with default constructor")
    void testProductDefaultConstructor() {
        Product product = new Product();
        product.name = "Default Constructor Test";
        product.stock = 50;

        repository.persist(product);

        assertNotNull(product.id);
        assertEquals("Default Constructor Test", product.name);
    }

    // ==================== READ TESTS ====================

    @Test
    @Transactional
    @DisplayName("Should find product by ID")
    void testFindById() {
        Product product = new Product("Find By ID Test");
        product.stock = 25;
        repository.persist(product);

        Product found = repository.findById(product.id);

        assertNotNull(found);
        assertEquals("Find By ID Test", found.name);
        assertEquals(25, found.stock);
    }

    @Test
    @Transactional
    @DisplayName("Should return null for non-existent ID")
    void testFindByIdNotFound() {
        Product found = repository.findById(99999L);
        assertNull(found);
    }

    @Test
    @Transactional
    @DisplayName("Should list all products")
    void testListAll() {
        long initialCount = repository.count();

        Product product1 = new Product("List Test 1");
        Product product2 = new Product("List Test 2");
        repository.persist(product1);
        repository.persist(product2);

        List<Product> products = repository.listAll();

        assertTrue(products.size() >= initialCount + 2);
    }

    // ==================== UPDATE TESTS ====================

    @Test
    @Transactional
    @DisplayName("Should update product fields")
    void testUpdateProduct() {
        Product product = new Product("Original Name");
        product.price = new BigDecimal("10.00");
        repository.persist(product);

        // Update fields
        product.name = "Updated Name";
        product.price = new BigDecimal("20.00");
        repository.persist(product);

        Product updated = repository.findById(product.id);
        assertEquals("Updated Name", updated.name);
        assertEquals(new BigDecimal("20.00"), updated.price);
    }

    // ==================== DELETE TESTS ====================

    @Test
    @Transactional
    @DisplayName("Should delete product by ID")
    void testDeleteById() {
        Product product = new Product("Delete Test");
        repository.persist(product);
        Long id = product.id;

        boolean deleted = repository.deleteById(id);

        assertTrue(deleted);
        assertNull(repository.findById(id));
    }

    @Test
    @Transactional
    @DisplayName("Should return false when deleting non-existent product")
    void testDeleteByIdNotFound() {
        boolean deleted = repository.deleteById(99999L);
        assertFalse(deleted);
    }

    // ==================== COUNT TESTS ====================

    @Test
    @Transactional
    @DisplayName("Should count products")
    void testCount() {
        long initialCount = repository.count();

        Product product = new Product("Count Test");
        repository.persist(product);

        long newCount = repository.count();
        assertEquals(initialCount + 1, newCount);
    }

    // ==================== FIELD TESTS ====================

    @Test
    @Transactional
    @DisplayName("Should handle null description")
    void testNullDescription() {
        Product product = new Product("Null Desc Test");
        product.description = null;
        product.stock = 10;

        repository.persist(product);

        Product found = repository.findById(product.id);
        assertNull(found.description);
    }

    @Test
    @Transactional
    @DisplayName("Should handle null price")
    void testNullPrice() {
        Product product = new Product("Null Price Test");
        product.price = null;
        product.stock = 10;

        repository.persist(product);

        Product found = repository.findById(product.id);
        assertNull(found.price);
    }

    @Test
    @Transactional
    @DisplayName("Should handle decimal price precision")
    void testPricePrecision() {
        Product product = new Product("Precision Test");
        // Column has precision=10, scale=2, so max 8 digits before decimal + 2 after
        product.price = new BigDecimal("12345678.12");
        product.stock = 5;

        repository.persist(product);

        Product found = repository.findById(product.id);
        assertEquals(new BigDecimal("12345678.12"), found.price);
    }
}

