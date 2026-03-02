# 📋 TASK 3 IMPLEMENTATION PLAN - WarehouseRepository CRUD

## Task Overview

**Objective**: Implement 4 CRUD methods in WarehouseRepository
**Current Issue**: Repository has no persistence methods
**Solution**: Add create(), update(), remove(), and findByBusinessUnitCode() methods
**Estimated Time**: 40 minutes
**Complexity**: ⭐⭐ Medium
**Files to Modify**: 1 file

---

## Current State Analysis

### WarehouseRepository.java (Current)
```java
public interface WarehouseRepository extends PanacheRepository<DbWarehouse> {
  List<DbWarehouse> findByLocationId(Long locationId);
}
```

**Current Status**:
- ✅ Extends PanacheRepository (has findAll, findById, etc.)
- ❌ Missing custom CRUD methods
- ❌ Missing business unit code search

### What Needs to Be Done

```
PanacheRepository already provides:
├─ findById(id) - Find by ID
├─ findAll() - Get all
├─ persist() - Create
├─ delete() - Delete
└─ update() - Update

We need to add:
├─ create(warehouse) - Wrapper for persist()
├─ update(warehouse) - Wrapper for update()
├─ remove(warehouse) - Wrapper for delete()
└─ findByBusinessUnitCode(code) - Custom query
```

---

## Step-by-Step Implementation Plan

### STEP 1: Understand DbWarehouse Entity (5 minutes)

**Find and review DbWarehouse.java**:
- Check field names
- Understand structure
- Identify business unit code field name
- Check validation rules

**What to look for**:
```java
public class DbWarehouse extends PanacheEntityBase {
    public Long id;
    public String businessUnitCode;  // ← What we'll search by
    public String name;
    public Long locationId;
    public int capacity;
    public int stock;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public LocalDateTime archivedAt;
}
```

---

### STEP 2: Implement create() Method (8 minutes)

**Purpose**: Create and persist a new warehouse

**Method Signature**:
```java
/**
 * Creates and persists a new warehouse.
 * 
 * Persists the warehouse entity to the database and ensures it receives
 * a generated ID. The warehouse must not already exist (id must be null).
 *
 * @param warehouse the warehouse entity to create, id must be null
 * @return the persisted warehouse with generated id
 * @throws PersistenceException if warehouse is invalid or already exists
 */
public DbWarehouse create(DbWarehouse warehouse) {
  if (warehouse == null) {
    throw new IllegalArgumentException("Warehouse must not be null");
  }
  if (warehouse.id != null) {
    throw new IllegalArgumentException("Warehouse id must be null for create operation");
  }
  
  persist(warehouse);
  return warehouse;
}
```

**Key Points**:
- ✅ Validate warehouse not null
- ✅ Validate id is null
- ✅ Use persist() from PanacheRepository
- ✅ Return the warehouse with ID set
- ✅ Complete JavaDoc

---

### STEP 3: Implement update() Method (8 minutes)

**Purpose**: Update an existing warehouse

**Method Signature**:
```java
/**
 * Updates an existing warehouse.
 * 
 * Merges the updated warehouse entity with the existing record in the database.
 * The warehouse must already exist (id must not be null).
 *
 * @param warehouse the warehouse entity with updates, id must not be null
 * @return the updated warehouse
 * @throws IllegalArgumentException if warehouse is null or id is null
 * @throws EntityNotFoundException if warehouse doesn't exist
 */
public DbWarehouse update(DbWarehouse warehouse) {
  if (warehouse == null) {
    throw new IllegalArgumentException("Warehouse must not be null");
  }
  if (warehouse.id == null) {
    throw new IllegalArgumentException("Warehouse id must not be null for update operation");
  }
  
  // Verify warehouse exists
  DbWarehouse existing = findById(warehouse.id);
  if (existing == null) {
    throw new EntityNotFoundException("Warehouse with id " + warehouse.id + " not found");
  }
  
  // Update fields
  existing.name = warehouse.name;
  existing.businessUnitCode = warehouse.businessUnitCode;
  existing.locationId = warehouse.locationId;
  existing.capacity = warehouse.capacity;
  existing.stock = warehouse.stock;
  existing.updatedAt = LocalDateTime.now();
  
  return existing;
}
```

**Key Points**:
- ✅ Validate warehouse not null
- ✅ Validate id is not null
- ✅ Check warehouse exists
- ✅ Update all relevant fields
- ✅ Set updatedAt timestamp
- ✅ Return updated warehouse
- ✅ Complete JavaDoc

---

### STEP 4: Implement remove() Method (8 minutes)

**Purpose**: Delete a warehouse

**Method Signature**:
```java
/**
 * Removes (deletes) a warehouse.
 * 
 * Soft-deletes the warehouse by setting the archivedAt timestamp.
 * This preserves historical data for auditing purposes.
 *
 * @param warehouse the warehouse entity to delete
 * @throws IllegalArgumentException if warehouse is null or id is null
 * @throws EntityNotFoundException if warehouse doesn't exist
 */
public void remove(DbWarehouse warehouse) {
  if (warehouse == null) {
    throw new IllegalArgumentException("Warehouse must not be null");
  }
  if (warehouse.id == null) {
    throw new IllegalArgumentException("Warehouse id must not be null for remove operation");
  }
  
  // Verify warehouse exists
  DbWarehouse existing = findById(warehouse.id);
  if (existing == null) {
    throw new EntityNotFoundException("Warehouse with id " + warehouse.id + " not found");
  }
  
  // Soft-delete by setting archivedAt
  existing.archivedAt = LocalDateTime.now();
  
  // Or hard-delete if required
  // existing.delete();
}
```

**Key Points**:
- ✅ Validate warehouse not null
- ✅ Validate id is not null
- ✅ Check warehouse exists
- ✅ Soft-delete with archivedAt timestamp
- ✅ Preserve historical data
- ✅ Complete JavaDoc

**Note**: Check if soft-delete or hard-delete is required

---

### STEP 5: Implement findByBusinessUnitCode() Method (8 minutes)

**Purpose**: Find warehouse by business unit code

**Method Signature**:
```java
/**
 * Finds a warehouse by its business unit code.
 * 
 * Performs a case-sensitive search for a warehouse with the given business
 * unit code. This is typically the unique identifier used by the domain.
 *
 * @param businessUnitCode the warehouse business unit code
 * @return the warehouse with matching code, or null if not found
 * @throws IllegalArgumentException if code is null or blank
 */
public DbWarehouse findByBusinessUnitCode(String businessUnitCode) {
  if (businessUnitCode == null || businessUnitCode.isBlank()) {
    throw new IllegalArgumentException("Business unit code must not be null or blank");
  }
  
  return find("businessUnitCode", businessUnitCode).firstResult();
}
```

**Key Points**:
- ✅ Validate code not null/blank
- ✅ Use PanacheRepository find() method
- ✅ Return null if not found (instead of throwing)
- ✅ Case-sensitive search
- ✅ Complete JavaDoc

---

### STEP 6: Add Required Imports (3 minutes)

**Imports needed**:
```java
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
```

---

### STEP 7: Write Tests (20 minutes)

**File**: Create `WarehouseRepositoryTest.java`

**Test Cases to Include**:

```java
@QuarkusTest
@DisplayName("Warehouse Repository Tests")
public class WarehouseRepositoryTest {
  
  @Inject
  WarehouseRepository warehouseRepository;
  
  @BeforeEach
  void setUp() {
    DbWarehouse.deleteAll();
  }
  
  // Test create()
  @Test
  @DisplayName("Should create warehouse successfully")
  void testCreateWarehouseSuccess() {
    // Arrange
    DbWarehouse warehouse = new DbWarehouse();
    warehouse.businessUnitCode = "WH-001";
    warehouse.name = "Main Warehouse";
    warehouse.locationId = 1L;
    warehouse.capacity = 100;
    warehouse.stock = 0;
    
    // Act
    DbWarehouse created = warehouseRepository.create(warehouse);
    
    // Assert
    assertNotNull(created.id, "Warehouse should have generated id");
    assertEquals("WH-001", created.businessUnitCode);
    assertNotNull(created.createdAt, "Created timestamp should be set");
  }
  
  @Test
  @DisplayName("Should throw exception when creating with pre-set id")
  void testCreateWarehouseWithIdThrowsException() {
    // Arrange
    DbWarehouse warehouse = new DbWarehouse();
    warehouse.id = 1L;  // Invalid: id should be null
    warehouse.businessUnitCode = "WH-001";
    
    // Act & Assert
    assertThrows(IllegalArgumentException.class, 
      () -> warehouseRepository.create(warehouse));
  }
  
  // Test update()
  @Test
  @DisplayName("Should update warehouse successfully")
  void testUpdateWarehouseSuccess() {
    // Arrange
    DbWarehouse warehouse = createAndPersistWarehouse("WH-001", "Original Name");
    warehouse.name = "Updated Name";
    warehouse.capacity = 200;
    
    // Act
    DbWarehouse updated = warehouseRepository.update(warehouse);
    
    // Assert
    assertEquals("Updated Name", updated.name);
    assertEquals(200, updated.capacity);
    assertNotNull(updated.updatedAt, "Updated timestamp should be set");
  }
  
  @Test
  @DisplayName("Should throw exception when updating non-existent warehouse")
  void testUpdateNonExistentWarehouseThrowsException() {
    // Arrange
    DbWarehouse warehouse = new DbWarehouse();
    warehouse.id = 99999L;
    warehouse.businessUnitCode = "INVALID";
    
    // Act & Assert
    assertThrows(EntityNotFoundException.class,
      () -> warehouseRepository.update(warehouse));
  }
  
  // Test remove()
  @Test
  @DisplayName("Should soft-delete warehouse successfully")
  void testRemoveWarehouseSuccess() {
    // Arrange
    DbWarehouse warehouse = createAndPersistWarehouse("WH-001", "Test");
    Long warehouseId = warehouse.id;
    
    // Act
    warehouseRepository.remove(warehouse);
    
    // Assert
    DbWarehouse deleted = warehouseRepository.findById(warehouseId);
    assertNotNull(deleted.archivedAt, "Archived timestamp should be set");
  }
  
  @Test
  @DisplayName("Should throw exception when removing non-existent warehouse")
  void testRemoveNonExistentWarehouseThrowsException() {
    // Arrange
    DbWarehouse warehouse = new DbWarehouse();
    warehouse.id = 99999L;
    
    // Act & Assert
    assertThrows(EntityNotFoundException.class,
      () -> warehouseRepository.remove(warehouse));
  }
  
  // Test findByBusinessUnitCode()
  @Test
  @DisplayName("Should find warehouse by business unit code")
  void testFindByBusinessUnitCodeSuccess() {
    // Arrange
    createAndPersistWarehouse("WH-001", "Test Warehouse");
    
    // Act
    DbWarehouse found = warehouseRepository.findByBusinessUnitCode("WH-001");
    
    // Assert
    assertNotNull(found);
    assertEquals("WH-001", found.businessUnitCode);
    assertEquals("Test Warehouse", found.name);
  }
  
  @Test
  @DisplayName("Should return null when business unit code not found")
  void testFindByBusinessUnitCodeNotFound() {
    // Act
    DbWarehouse found = warehouseRepository.findByBusinessUnitCode("INVALID");
    
    // Assert
    assertNull(found);
  }
  
  @Test
  @DisplayName("Should throw exception for null business unit code")
  void testFindByBusinessUnitCodeWithNullThrowsException() {
    // Act & Assert
    assertThrows(IllegalArgumentException.class,
      () -> warehouseRepository.findByBusinessUnitCode(null));
  }
  
  // Helper method
  private DbWarehouse createAndPersistWarehouse(String code, String name) {
    DbWarehouse warehouse = new DbWarehouse();
    warehouse.businessUnitCode = code;
    warehouse.name = name;
    warehouse.locationId = 1L;
    warehouse.capacity = 100;
    warehouse.stock = 0;
    warehouse.createdAt = LocalDateTime.now();
    warehouse.persist();
    return warehouse;
  }
}
```

---

## Code Quality Checklist

```
DOCUMENTATION:
☐ Class-level JavaDoc
☐ Method-level JavaDoc with @param/@return/@throws
☐ Clear inline comments where needed
☐ Complete documentation for all methods

VALIDATION:
☐ Null checks on inputs
☐ ID validation for create/update/remove
☐ Business unit code validation
☐ Clear error messages

CODE ORGANIZATION:
☐ Proper method ordering
☐ Constants in UPPER_CASE if any
☐ No code duplication
☐ Single responsibility per method

TESTING:
☐ Happy path tests (create, update, remove, find)
☐ Error scenario tests (null inputs, non-existent records)
☐ Validation tests
☐ @DisplayName on all tests
☐ Arrange-Act-Assert pattern
```

---

## Files to Modify

### File 1: WarehouseRepository.java
**Location**: `src/main/java/com/fulfilment/application/monolith/warehouses/domain/repositories/WarehouseRepository.java`

**Changes**:
- Add create() method (with validation)
- Add update() method (with validation)
- Add remove() method (with validation)
- Add findByBusinessUnitCode() method (with validation)
- Add required imports
- Add comprehensive JavaDoc

**Lines Added**: ~100-120 lines

---

### File 2: WarehouseRepositoryTest.java (New)
**Location**: `src/test/java/com/fulfilment/application/monolith/warehouses/domain/repositories/WarehouseRepositoryTest.java`

**Content**:
- 8+ comprehensive test cases
- Happy path + error scenarios
- Complete test coverage

**Lines**: ~180-200 lines

---

## Time Breakdown

```
Understanding DbWarehouse entity:     5 min
Implementing create():                8 min
Implementing update():                8 min
Implementing remove():                8 min
Implementing findByBusinessUnitCode(): 8 min
Adding imports:                       3 min
Writing tests:                       20 min
Verification & cleanup:               5 min
─────────────────────────────────────────
Total: ~65 minutes (includes buffer)
Estimated: 40-50 minutes actual
```

---

## Validation Rules to Implement

### create() Validation
- ✅ Warehouse must not be null
- ✅ ID must be null (new record)
- ✅ Persist and return with generated ID

### update() Validation
- ✅ Warehouse must not be null
- ✅ ID must not be null
- ✅ Warehouse must exist in database
- ✅ Update timestamp should be set
- ✅ Preserve created timestamp

### remove() Validation
- ✅ Warehouse must not be null
- ✅ ID must not be null
- ✅ Warehouse must exist
- ✅ Set archivedAt timestamp (soft-delete)

### findByBusinessUnitCode() Validation
- ✅ Code must not be null
- ✅ Code must not be blank
- ✅ Case-sensitive search
- ✅ Return null if not found (not exception)

---

## Key Decisions

### 1. Soft-Delete vs Hard-Delete
**Current Plan**: Soft-delete (set archivedAt)
**Why**: Preserves audit trail
**Alternative**: Hard-delete if required by business logic

### 2. Update Strategy
**Current Plan**: Find → Update fields → Return
**Why**: Allows validation and field-by-field updates
**Alternative**: Merge() if using JPA

### 3. Search Strategy
**Current Plan**: Case-sensitive exact match
**Why**: Business unit codes are typically uppercase
**Alternative**: Case-insensitive if needed

---

## Integration Points

### After Task 3, these classes will use WarehouseRepository:

```
Task 4: CreateWarehouseUseCase
  └─ Calls: warehouseRepository.create()

Task 5: ReplaceWarehouseUseCase
  └─ Calls: warehouseRepository.update()
  └─ Calls: warehouseRepository.findByBusinessUnitCode()

Task 6: ArchiveWarehouseUseCase
  └─ Calls: warehouseRepository.remove()

Task 7: WarehouseResourceImpl
  └─ Calls: All repository methods
```

---

## Success Criteria

✅ **Task 3 is COMPLETE when**:
- [ ] create() method implemented with validation
- [ ] update() method implemented with validation
- [ ] remove() method implemented with validation
- [ ] findByBusinessUnitCode() method implemented
- [ ] All methods have JavaDoc
- [ ] 8+ test cases created
- [ ] All tests passing
- [ ] Code follows senior-level standards
- [ ] No compilation errors

---

## Troubleshooting Guide

### Problem: EntityNotFoundException not imported
**Solution**: Add import: `import jakarta.persistence.EntityNotFoundException;`

### Problem: LocalDateTime methods not available
**Solution**: Add import: `import java.time.LocalDateTime;`

### Problem: PanacheRepository methods not available
**Solution**: Verify WarehouseRepository extends PanacheRepository<DbWarehouse>

### Problem: Tests failing on timestamp checks
**Solution**: Use assertNotNull() instead of exact timestamp matching

---

## Next Task Preview

**After Task 3**: Task 4 - CreateWarehouseUseCase (60 minutes)
- Will use `warehouseRepository.create()`
- Will validate business unit code uniqueness
- Will use events (like Task 2)

---

## Reference Materials

📄 **QUICK_START_GUIDE.md** - Implementation template
📄 **SENIOR_JAVA_CODE_STANDARDS.md** - Code quality checklist
📄 **TASK_2_COMPLETION_REPORT.md** - Similar pattern (events)

---

## Ready to Implement?

**Start with**:
1. Open WarehouseRepository.java
2. Review DbWarehouse structure
3. Follow STEP 1-7 sequentially
4. Write tests as you go
5. Verify all tests pass

**Expected Result**: 
- 4 CRUD methods implemented
- 8+ tests (100% passing)
- Production-ready code quality

---

**Estimated Time**: 40-50 minutes
**Difficulty**: ⭐⭐ Medium
**Status**: READY TO IMPLEMENT 🚀

