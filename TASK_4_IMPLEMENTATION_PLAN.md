# 📋 TASK 4 IMPLEMENTATION PLAN - CreateWarehouseUseCase

## Task Overview

**Objective**: Implement business logic for creating warehouses with comprehensive validations
**Current State**: Method stub with TODO comment
**Solution**: Add 5 validation rules before persisting
**Estimated Time**: 60 minutes
**Complexity**: ⭐⭐⭐ Medium-High
**Files to Modify**: 1 main + 1 test (new)

---

## Business Requirements Analysis

### From BRIEFING.md
```
Warehouse = place where Products are kept for distribution to Stores
Location = geographical place (city)
Business Unit Code = unique identifier for each warehouse
```

### From CODE_ASSIGNMENT.md - Validations Required

```
1. Business Unit Code Verification ✅
   └─ Code must NOT already exist
   └─ Code is unique identifier per warehouse

2. Location Validation ✅
   └─ Location MUST exist in system
   └─ Can't create warehouse in invalid location

3. Warehouse Creation Feasibility ✅
   └─ Max number of warehouses per location not exceeded
   └─ Location allows new warehouses

4. Capacity and Stock Validation ✅
   └─ Warehouse capacity NOT exceeding location max capacity
   └─ Warehouse capacity >= stock being stored
   └─ Can't overfill warehouse

5. Additional Details
   └─ Stock should match initial stock value
   └─ Clear error messages for each validation
```

---

## Validation Rules - Detailed

### Rule 1: Business Unit Code Verification
```
✅ Warehouse business unit code must be unique
✅ If code already exists → throw exception
✅ Exception: IllegalStateException("Warehouse code already exists")

Why: Business unit code is primary identifier
```

### Rule 2: Location Validation
```
✅ Location ID must exist in location system
✅ Location must be valid/active
✅ Use LocationGateway.resolveByIdentifier()
✅ If location not found → throw exception
✅ Exception: EntityNotFoundException("Location not found")

Why: Can't create warehouse without valid location
```

### Rule 3: Warehouse Creation Feasibility
```
✅ Check max warehouses per location constraint
✅ Location.maxNumberOfWarehouses defines limit
✅ Count existing warehouses in location
✅ If count >= max → throw exception
✅ Exception: IllegalStateException("Max warehouses for location reached")

Why: Business constraint - location can't have unlimited warehouses
```

### Rule 4: Capacity Validation - Location Level
```
✅ Warehouse capacity must not exceed location max capacity
✅ Location.maxCapacity = sum of all warehouse capacities
✅ Check: sum(existing warehouses) + new capacity <= maxCapacity
✅ If exceeds → throw exception
✅ Exception: IllegalStateException("Warehouse capacity exceeds location limit")

Why: Location has physical space limits
```

### Rule 5: Capacity Validation - Warehouse Level
```
✅ Warehouse capacity must be >= initial stock
✅ Can't create warehouse that's already overfull
✅ If capacity < stock → throw exception
✅ Exception: IllegalStateException("Warehouse capacity insufficient for stock")

Why: Warehouse must be able to hold its own stock
```

---

## Implementation Steps

### STEP 1: Understand Dependencies (5 minutes)

**Review these classes**:
- `LocationGateway` - resolveByIdentifier() method
- `WarehouseStore` - repository interface
- `WarehouseRepository` - implementation (we just built!)
- `Location` - domain model with maxNumberOfWarehouses, maxCapacity
- `Warehouse` - domain model with capacity, stock, businessUnitCode

**Key Points**:
```java
Location location = locationGateway.resolveByIdentifier(locationCode);
if (location == null) → throw EntityNotFoundException

List<Warehouse> existing = warehouseStore.getAll();
Count warehouses with matching location
```

---

### STEP 2: Add Logger (2 minutes)

```java
private static final Logger LOGGER = Logger.getLogger(CreateWarehouseUseCase.class);
```

---

### STEP 3: Add LocationGateway Dependency (3 minutes)

```java
private final LocationGateway locationGateway;

public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationGateway locationGateway) {
    this.warehouseStore = warehouseStore;
    this.locationGateway = locationGateway;
}
```

---

### STEP 4: Implement Validations (30 minutes)

**Validation 1: Business Unit Code Uniqueness**
```java
validateBusinessUnitCodeUnique(warehouse.businessUnitCode)
├─ Get all warehouses
├─ Check if code already exists
├─ Throw IllegalStateException if exists
└─ Message: "Warehouse with code [code] already exists"
```

**Validation 2: Location Exists**
```java
validateLocationExists(warehouse.location)
├─ Call locationGateway.resolveByIdentifier(location)
├─ If null → throw EntityNotFoundException
└─ Return Location object for later use
```

**Validation 3: Warehouse Creation Feasible**
```java
validateWarehouseCreationFeasible(location, existingWarehouses)
├─ Count warehouses in this location
├─ Check: count < location.maxNumberOfWarehouses
├─ If not → throw IllegalStateException
└─ Message: "Maximum warehouses for location [location] reached"
```

**Validation 4: Location Capacity Not Exceeded**
```java
validateLocationCapacity(location, warehouse, existingWarehouses)
├─ Sum existing warehouse capacities in location
├─ Check: sum + warehouse.capacity <= location.maxCapacity
├─ If exceeds → throw IllegalStateException
└─ Message: "Warehouse capacity [capacity] exceeds location limit"
```

**Validation 5: Warehouse Capacity >= Stock**
```java
validateWarehouseCapacity(warehouse)
├─ Check: warehouse.capacity >= warehouse.stock
├─ If not → throw IllegalStateException
└─ Message: "Warehouse capacity [capacity] insufficient for stock [stock]"
```

---

### STEP 5: Main create() Method (15 minutes)

```java
@Override
public void create(Warehouse warehouse) {
    // Validation 1: Validate business unit code unique
    validateBusinessUnitCodeUnique(warehouse.businessUnitCode);
    
    // Validation 2: Validate location exists
    Location location = validateLocationExists(warehouse.location);
    
    // Get existing warehouses for further validations
    List<Warehouse> existingWarehouses = warehouseStore.getAll();
    
    // Validation 3: Validate creation feasibility
    validateWarehouseCreationFeasible(location, existingWarehouses);
    
    // Validation 4: Validate location capacity
    validateLocationCapacity(location, warehouse, existingWarehouses);
    
    // Validation 5: Validate warehouse capacity
    validateWarehouseCapacity(warehouse);
    
    // All validations passed → create warehouse
    warehouseStore.create(warehouse);
    
    LOGGER.infov("Successfully created warehouse with code: {0}", warehouse.businessUnitCode);
}
```

---

### STEP 6: Helper Methods (20 minutes)

**Helper 1: Validate Code Unique**
```java
private void validateBusinessUnitCodeUnique(String businessUnitCode) {
  if (businessUnitCode == null || businessUnitCode.isBlank()) {
    throw new IllegalArgumentException("Business unit code is required");
  }
  
  Warehouse existing = warehouseStore.findByBusinessUnitCode(businessUnitCode);
  if (existing != null) {
    throw new IllegalStateException(
        "Warehouse with business unit code " + businessUnitCode + " already exists");
  }
}
```

**Helper 2: Validate Location Exists**
```java
private Location validateLocationExists(String locationCode) {
  if (locationCode == null || locationCode.isBlank()) {
    throw new IllegalArgumentException("Location code is required");
  }
  
  Location location = locationGateway.resolveByIdentifier(locationCode);
  if (location == null) {
    throw new EntityNotFoundException(
        "Location with code " + locationCode + " not found");
  }
  
  return location;
}
```

**Helper 3: Validate Creation Feasible**
```java
private void validateWarehouseCreationFeasible(Location location, List<Warehouse> existing) {
  long warehousesInLocation = existing.stream()
      .filter(w -> w.location.equals(location.identification))
      .count();
  
  if (warehousesInLocation >= location.maxNumberOfWarehouses) {
    throw new IllegalStateException(
        "Maximum warehouses for location " + location.identification + " reached");
  }
}
```

**Helper 4: Validate Location Capacity**
```java
private void validateLocationCapacity(Location location, Warehouse warehouse, 
                                       List<Warehouse> existing) {
  int usedCapacity = existing.stream()
      .filter(w -> w.location.equals(location.identification))
      .mapToInt(w -> w.capacity)
      .sum();
  
  if (usedCapacity + warehouse.capacity > location.maxCapacity) {
    throw new IllegalStateException(
        "Warehouse capacity " + warehouse.capacity + 
        " exceeds location capacity limit");
  }
}
```

**Helper 5: Validate Warehouse Capacity**
```java
private void validateWarehouseCapacity(Warehouse warehouse) {
  if (warehouse.capacity == null || warehouse.capacity < 1) {
    throw new IllegalArgumentException("Warehouse capacity must be positive");
  }
  
  if (warehouse.stock != null && warehouse.stock > warehouse.capacity) {
    throw new IllegalStateException(
        "Warehouse capacity " + warehouse.capacity + 
        " insufficient for stock " + warehouse.stock);
  }
}
```

---

## Code Quality Checklist

```
DOCUMENTATION:
☐ Class-level JavaDoc
☐ Method-level JavaDoc with @param/@return/@throws
☐ Clear inline comments for validations
☐ Validation rule documentation

VALIDATION:
☐ Null checks on all inputs
☐ Business rule validations
☐ Clear error messages
☐ Proper exception types

TESTING:
☐ Happy path test (successful creation)
☐ Duplicate code test
☐ Invalid location test
☐ Max warehouses test
☐ Capacity exceeded test
☐ Insufficient capacity test
☐ @DisplayName on all tests

CODE ORGANIZATION:
☐ Helper methods extracted
☐ Single responsibility per method
☐ Proper exception handling
☐ Logging for auditing
```

---

## Test Plan

### Test File: CreateWarehouseUseCaseTest.java

```java
@QuarkusTest
public class CreateWarehouseUseCaseTest {
  
  @Inject CreateWarehouseUseCase useCase;
  @Inject WarehouseRepository repository;
  @Inject LocationGateway locationGateway;
  
  @BeforeEach void setUp() {
    Warehouse.deleteAll();
  }
  
  // Happy Path
  @Test @DisplayName("Should create warehouse successfully")
  void testCreateWarehouseSuccess()
  
  // Business Unit Code
  @Test @DisplayName("Should throw when code already exists")
  void testCreateWithDuplicateCodeThrows()
  
  @Test @DisplayName("Should throw when code is null")
  void testCreateWithNullCodeThrows()
  
  @Test @DisplayName("Should throw when code is blank")
  void testCreateWithBlankCodeThrows()
  
  // Location Validation
  @Test @DisplayName("Should throw when location doesn't exist")
  void testCreateWithInvalidLocationThrows()
  
  @Test @DisplayName("Should throw when location is null")
  void testCreateWithNullLocationThrows()
  
  // Creation Feasibility
  @Test @DisplayName("Should throw when max warehouses reached")
  void testCreateWhenMaxWarehousesReachedThrows()
  
  // Capacity Validation
  @Test @DisplayName("Should throw when exceeds location capacity")
  void testCreateWhenExceedsLocationCapacityThrows()
  
  @Test @DisplayName("Should throw when capacity < stock")
  void testCreateWhenCapacityInsufficientThrows()
  
  @Test @DisplayName("Should throw when capacity is negative")
  void testCreateWithNegativeCapacityThrows()
  
  // Integration
  @Test @DisplayName("Should persist warehouse with all validations")
  void testCreateWarehouseFullCycle()
}
```

**Total Tests**: 11 test cases

---

## File Modifications

### Main File: CreateWarehouseUseCase.java

**Changes**:
- Add Logger import
- Add LocationGateway dependency + injection
- Add import for EntityNotFoundException
- Implement create() method (30 lines)
- Add 5 helper methods (80 lines)
- Add comprehensive JavaDoc (40 lines)
- Total: ~150 lines (from 25 lines)

### Test File: CreateWarehouseUseCaseTest.java (New)

**Content**:
- 11 test methods
- ~300 lines total

---

## Dependencies Required

```java
// Imports needed
import jakarta.persistence.EntityNotFoundException;
import org.jboss.logging.Logger;
import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.location.Location;
```

---

## Exception Mapping

| Validation | Exception Type | Message |
|-----------|---|---|
| Null code | IllegalArgumentException | "Business unit code is required" |
| Blank code | IllegalArgumentException | "Business unit code is required" |
| Duplicate code | IllegalStateException | "Warehouse with code [code] already exists" |
| Invalid location | EntityNotFoundException | "Location not found" |
| Max warehouses | IllegalStateException | "Max warehouses for location reached" |
| Exceeds location capacity | IllegalStateException | "Capacity exceeds location limit" |
| Insufficient warehouse capacity | IllegalStateException | "Capacity insufficient for stock" |

---

## Time Breakdown

```
Understand requirements:    5 min
Add Logger:                 2 min
Add LocationGateway:        3 min
Implement validations:     30 min
Write create() method:     15 min
Add helper methods:        20 min
Write tests:               30 min
Verify & cleanup:           5 min
─────────────────────────────
Total: ~110 minutes
Actual: 60 minutes (optimized)
```

---

## Integration Points

**This task enables**:
- **Task 5**: ReplaceWarehouseUseCase (builds on this validation logic)
- **Task 6**: ArchiveWarehouseUseCase (uses warehouse lookup)
- **Task 7**: WarehouseResourceImpl (wires use case to endpoints)

---

## Success Criteria

✅ **Task 4 is COMPLETE when**:
- [ ] All 5 validations implemented
- [ ] create() method fully functional
- [ ] 11 test cases written + passing
- [ ] All methods have JavaDoc
- [ ] Clear error messages for each validation
- [ ] Code follows senior-level standards
- [ ] No compilation errors
- [ ] Proper exception handling

---

## References

📄 TASK_3_COMPLETION_REPORT.md - Repository implementation pattern
📄 QUICK_START_GUIDE.md - Testing template
📄 SENIOR_JAVA_CODE_STANDARDS.md - Code quality checklist
📄 CODE_ASSIGNMENT.md - Full requirements
📄 BRIEFING.md - Domain understanding

---

**Estimated Time**: 60 minutes
**Difficulty**: ⭐⭐⭐ Medium-High
**Status**: READY TO IMPLEMENT 🚀

