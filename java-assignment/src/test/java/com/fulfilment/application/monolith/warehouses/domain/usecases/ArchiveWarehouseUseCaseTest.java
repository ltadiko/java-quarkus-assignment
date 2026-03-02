package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ArchiveWarehouseUseCase.
 * Tests warehouse archiving (soft-delete) functionality.
 */
@QuarkusTest
public class ArchiveWarehouseUseCaseTest {

    @Inject
    ArchiveWarehouseUseCase archiveWarehouseUseCase;

    @Inject
    CreateWarehouseUseCase createWarehouseUseCase;

    @Inject
    WarehouseRepository warehouseRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        warehouseRepository.deleteAll();
    }

    // ==================== SUCCESS TESTS ====================

    @Test
    @Transactional
    @DisplayName("Should archive warehouse successfully")
    void testArchiveSuccess() {
        // Arrange
        Warehouse warehouse = createTestWarehouse("WH-ARCHIVE-001", "AMSTERDAM-001", 50, 20);
        createWarehouseUseCase.create(warehouse);

        Warehouse createdWarehouse = warehouseRepository.findByBusinessUnitCode("WH-ARCHIVE-001");
        assertNull(createdWarehouse.archivedAt, "Warehouse should not be archived initially");

        // Act
        archiveWarehouseUseCase.archive(createdWarehouse);

        // Assert - need to refetch to see the change
        var allWarehouses = warehouseRepository.getAll();
        Warehouse archivedWarehouse = allWarehouses.stream()
            .filter(w -> w.businessUnitCode.equals("WH-ARCHIVE-001"))
            .findFirst()
            .orElse(null);

        assertNotNull(archivedWarehouse, "Warehouse should still exist");
        assertNotNull(archivedWarehouse.archivedAt, "Warehouse should have archivedAt set");
    }

    @Test
    @Transactional
    @DisplayName("Should archive warehouse by code successfully")
    void testArchiveByCodeSuccess() {
        // Arrange
        Warehouse warehouse = createTestWarehouse("WH-ARCHIVE-002", "AMSTERDAM-001", 50, 20);
        createWarehouseUseCase.create(warehouse);

        // Act
        archiveWarehouseUseCase.archiveByCode("WH-ARCHIVE-002");

        // Assert
        var allWarehouses = warehouseRepository.getAll();
        Warehouse archivedWarehouse = allWarehouses.stream()
            .filter(w -> w.businessUnitCode.equals("WH-ARCHIVE-002"))
            .findFirst()
            .orElse(null);

        assertNotNull(archivedWarehouse, "Warehouse should still exist");
        assertNotNull(archivedWarehouse.archivedAt, "Warehouse should have archivedAt set");
    }

    @Test
    @Transactional
    @DisplayName("Archived warehouse should preserve all data")
    void testArchivePreservesData() {
        // Arrange
        Warehouse warehouse = createTestWarehouse("WH-PRESERVE", "TILBURG-001", 30, 15);
        createWarehouseUseCase.create(warehouse);

        Warehouse createdWarehouse = warehouseRepository.findByBusinessUnitCode("WH-PRESERVE");

        // Act
        archiveWarehouseUseCase.archive(createdWarehouse);

        // Assert
        var allWarehouses = warehouseRepository.getAll();
        Warehouse archivedWarehouse = allWarehouses.stream()
            .filter(w -> w.businessUnitCode.equals("WH-PRESERVE"))
            .findFirst()
            .orElse(null);

        assertNotNull(archivedWarehouse);
        assertEquals("WH-PRESERVE", archivedWarehouse.businessUnitCode);
        assertEquals("TILBURG-001", archivedWarehouse.location);
        assertEquals(30, archivedWarehouse.capacity);
        assertEquals(15, archivedWarehouse.stock);
        assertNotNull(archivedWarehouse.createdAt);
        assertNotNull(archivedWarehouse.archivedAt);
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @Transactional
    @DisplayName("Should throw when archiving null warehouse")
    void testArchiveNullWarehouse() {
        assertThrows(
            IllegalArgumentException.class,
            () -> archiveWarehouseUseCase.archive(null),
            "Should throw when warehouse is null"
        );
    }

    @Test
    @Transactional
    @DisplayName("Should throw when archiving already archived warehouse")
    void testArchiveAlreadyArchived() {
        // Arrange
        Warehouse warehouse = createTestWarehouse("WH-DOUBLE-ARCHIVE", "AMSTERDAM-001", 50, 20);
        createWarehouseUseCase.create(warehouse);

        Warehouse createdWarehouse = warehouseRepository.findByBusinessUnitCode("WH-DOUBLE-ARCHIVE");
        archiveWarehouseUseCase.archive(createdWarehouse);

        // Get the archived warehouse
        var allWarehouses = warehouseRepository.getAll();
        Warehouse archivedWarehouse = allWarehouses.stream()
            .filter(w -> w.businessUnitCode.equals("WH-DOUBLE-ARCHIVE"))
            .findFirst()
            .orElse(null);

        // Act & Assert - try to archive again
        assertThrows(
            IllegalStateException.class,
            () -> archiveWarehouseUseCase.archive(archivedWarehouse),
            "Should throw when warehouse is already archived"
        );
    }

    @Test
    @Transactional
    @DisplayName("Should throw when archiving by null code")
    void testArchiveByNullCode() {
        assertThrows(
            IllegalArgumentException.class,
            () -> archiveWarehouseUseCase.archiveByCode(null),
            "Should throw when code is null"
        );
    }

    @Test
    @Transactional
    @DisplayName("Should throw when archiving by blank code")
    void testArchiveByBlankCode() {
        assertThrows(
            IllegalArgumentException.class,
            () -> archiveWarehouseUseCase.archiveByCode("   "),
            "Should throw when code is blank"
        );
    }

    @Test
    @Transactional
    @DisplayName("Should throw when warehouse not found")
    void testArchiveByCodeNotFound() {
        assertThrows(
            EntityNotFoundException.class,
            () -> archiveWarehouseUseCase.archiveByCode("NONEXISTENT-CODE"),
            "Should throw when warehouse not found"
        );
    }

    // ==================== HELPER METHODS ====================

    private Warehouse createTestWarehouse(String code, String location, int capacity, int stock) {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = code;
        warehouse.location = location;
        warehouse.capacity = capacity;
        warehouse.stock = stock;
        return warehouse;
    }
}
