# 📋 TASK 5 IMPLEMENTATION PLAN - ReplaceWarehouse UseCase

## Task Overview

**Objective**: Implement warehouse replacement logic with comprehensive validations
**Current State**: Feature not yet implemented
**Solution**: Create ReplaceWarehouse use case with 6 validation rules
**Estimated Time**: 45-60 minutes
**Complexity**: ⭐⭐⭐⭐ Medium-High
**Files to Create**: 2 new (ReplaceWarehouseUseCase.java, ReplaceWarehouseOperationPort.java)
**Files to Modify**: 1 (WarehouseResource.java to add endpoint)

---

## Business Requirements Analysis

### From BRIEFING.md
```
"A Warehouse also has a special operation called 'replace', where the company is 
creating a new Warehouse in the same area of an already existent one and we will 
deprecate this one, re-using the Business Unit Code that identifies every Warehouse 
to this new one.

When a Warehouse is replaced we basically archive the current Warehouse using the 
Business Unit Code provided and create the Warehouse using this same Business Unit Code. 
This way we have a history track of the area/business unit."
```

### From CODE_ASSIGNMENT.md - Replacement Validations

#### Standard Validations (from warehouse creation)
```
1. Business Unit Code Verification ✅
   └─ Code must already exist (warehouse being replaced)
   └─ Different from Create: code MUST exist, not duplicate

2. Location Validation ✅
   └─ New warehouse location must be valid
   └─ (Typically same as old warehouse's location)

3. Capacity Accommodation ✅ (NEW for replacement)
   └─ New warehouse capacity >= old warehouse stock
   └─ Can't replace with smaller warehouse

4. Stock Matching ✅ (NEW for replacement)
   └─ New warehouse stock = old warehouse stock
   └─ Stock transfers to new warehouse
```

#### Additional Context
```
- Code Reuse: Business Unit Code transfers from old to new warehouse
- Archival: Old warehouse marked as archived (soft delete)
- History: Maintains business unit tracking across warehouse generations
- Same Location: Implied same location (or can specify new one?)
```

---

## Validation Rules - Detailed

### Rule 1: Business Unit Code Verification (REVERSED)
```
✅ Warehouse business unit code must ALREADY exist
✅ Different from Create: code must be found, not new
✅ If code NOT found → throw exception
✅ Exception: EntityNotFoundException("Warehouse not found")

Why: We're replacing an existing warehouse, not creating a new one
```

### Rule 2: Location Validation
```
✅ New location ID must exist in location system
✅ Use LocationGateway.resolveByIdentifier()
✅ If location not found → throw exception
✅ Exception: EntityNotFoundException("Location not found")

Why: Can't replace with warehouse in invalid location
```

### Rule 3: Capacity Accommodation
```
✅ New warehouse capacity >= existing warehouse stock
✅ Check: newWarehouse.capacity >= oldWarehouse.stock
✅ If insufficient → throw exception
✅ Exception: IllegalStateException("New warehouse capacity insufficient")

Why: New warehouse must be able to hold the stock from old warehouse
```

### Rule 4: Stock Matching
```
✅ New warehouse stock = old warehouse stock
✅ Stock transfers from old to new
✅ If mismatch → throw exception OR auto-correct
✅ Exception: IllegalStateException("Stock mismatch")

Why: Ensures inventory continuity during replacement
Approach: Can either validate match or auto-set stock = old.stock
```

### Rule 5: Warehouse Not Already Archived
```
✅ Warehouse being replaced must NOT be archived
✅ Can't replace an already-archived warehouse
✅ If archived → throw exception
✅ Exception: IllegalStateException("Warehouse already archived")

Why: Archived warehouses can't be modified
```

### Rule 6: New Warehouse Doesn't Already Exist with Different Code
```
✅ New warehouse details must be valid
✅ If business unit code used elsewhere → error
✅ Actually: Code will be TRANSFERRED, not duplicated
✅ Exception: Handled by repository uniqueness check

Why: Ensure no accidental duplicates during transfer
```

---

## Implementation Steps

### STEP 1: Create ReplaceWarehouseOperation Port Interface (3 minutes)

**File**: `com/fulfilment/application/monolith/warehouses/domain/ports/ReplaceWarehouseOperation.java`

```java
public interface ReplaceWarehouseOperation {
  /**
   * Replaces an existing warehouse with a new one while reusing the business unit code.
   * 
   * Validates that:
   * 1. Old warehouse code exists
   * 2. New location exists
   * 3. New capacity can accommodate old stock
   * 4. Stock levels match
   * 5. Old warehouse not already archived
   * 
   * @param oldBusinessUnitCode the code of warehouse being replaced
   * @param newWarehouse the new warehouse data (will use oldBusinessUnitCode)
   * @throws EntityNotFoundException if old warehouse or location not found
   * @throws IllegalStateException if business rules violated
   */
  void replace(String oldBusinessUnitCode, Warehouse newWarehouse);
}
```

---

### STEP 2: Create ReplaceWarehouseUseCase Implementation (40 minutes)

**File**: `com/fulfilment/application/monolith/warehouses/domain/usecases/ReplaceWarehouseUseCase.java`

```
Structure:
├─ Dependencies: WarehouseStore, LocationGateway
├─ Main Method: replace(oldCode, newWarehouse)
├─ Validation Methods (6):
│  ├─ validateWarehouseExists(code)
│  ├─ validateLocationExists(location)
│  ├─ validateCapacityAccommodation(oldWarehouse, newWarehouse)
│  ├─ validateStockMatching(oldWarehouse, newWarehouse)
│  ├─ validateWarehouseNotArchived(warehouse)
│  └─ validateNewWarehouseValid(newWarehouse)
└─ Processing Methods:
   ├─ archiveOldWarehouse(warehouse)
   └─ createNewWarehouse(oldCode, warehouse)
```

#### Main Flow
```java
public void replace(String oldBusinessUnitCode, Warehouse newWarehouse) {
    // 1. Validate old warehouse exists
    Warehouse oldWarehouse = validateWarehouseExists(oldBusinessUnitCode);
    
    // 2. Validate old warehouse not archived
    validateWarehouseNotArchived(oldWarehouse);
    
    // 3. Validate location exists
    Location location = validateLocationExists(newWarehouse.location);
    
    // 4. Validate capacity accommodation
    validateCapacityAccommodation(oldWarehouse, newWarehouse);
    
    // 5. Validate stock matching
    validateStockMatching(oldWarehouse, newWarehouse);
    
    // 6. Ensure new warehouse is valid
    validateNewWarehouseValid(newWarehouse);
    
    // All validations passed
    // Archive old warehouse
    archiveOldWarehouse(oldWarehouse);
    
    // Create new warehouse with same business unit code
    newWarehouse.businessUnitCode = oldBusinessUnitCode;
    newWarehouse.stock = oldWarehouse.stock; // Ensure stock transferred
    warehouseStore.create(newWarehouse);
    
    LOGGER.infov("Successfully replaced warehouse {0}", oldBusinessUnitCode);
}
```

#### Validation Methods

**validateWarehouseExists()**
```java
private Warehouse validateWarehouseExists(String businessUnitCode) {
  if (businessUnitCode == null || businessUnitCode.isBlank()) {
    throw new IllegalArgumentException("Business unit code is required");
  }
  
  Warehouse warehouse = warehouseStore.findByBusinessUnitCode(businessUnitCode);
  if (warehouse == null) {
    throw new EntityNotFoundException(
        "Warehouse with code " + businessUnitCode + " not found");
  }
  
  return warehouse;
}
```

**validateWarehouseNotArchived()**
```java
private void validateWarehouseNotArchived(Warehouse warehouse) {
  if (warehouse.archivedAt != null) {
    throw new IllegalStateException(
        "Cannot replace warehouse " + warehouse.businessUnitCode + 
        " as it is already archived");
  }
}
```

**validateLocationExists()**
```java
private Location validateLocationExists(String locationCode) {
  if (locationCode == null || locationCode.isBlank()) {
    throw new IllegalArgumentException("Location code is required");
  }
  
  Location location = locationGateway.resolveByIdentifier(locationCode);
  if (location == null) {
    throw new EntityNotFoundException(
        "Location with code " + locationCode + " does not exist");
  }
  
  return location;
}
```

**validateCapacityAccommodation()**
```java
private void validateCapacityAccommodation(
    Warehouse oldWarehouse, Warehouse newWarehouse) {
  if (newWarehouse.capacity == null) {
    throw new IllegalArgumentException("New warehouse capacity is required");
  }
  
  if (newWarehouse.capacity < oldWarehouse.stock) {
    throw new IllegalStateException(
        "New warehouse capacity " + newWarehouse.capacity + 
        " cannot accommodate existing stock " + oldWarehouse.stock);
  }
}
```

**validateStockMatching()**
```java
private void validateStockMatching(
    Warehouse oldWarehouse, Warehouse newWarehouse) {
  if (newWarehouse.stock == null) {
    // Auto-set stock if not provided
    newWarehouse.stock = oldWarehouse.stock;
    return;
  }
  
  if (!newWarehouse.stock.equals(oldWarehouse.stock)) {
    throw new IllegalStateException(
        "New warehouse stock " + newWarehouse.stock + 
        " must match old warehouse stock " + oldWarehouse.stock);
  }
}
```

**archiveOldWarehouse()**
```java
private void archiveOldWarehouse(Warehouse warehouse) {
  warehouse.archivedAt = LocalDateTime.now();
  warehouseStore.update(warehouse);
  LOGGER.infov("Archived old warehouse {0}", warehouse.businessUnitCode);
}
```

---

### STEP 3: Update WarehouseResource with Endpoint (5 minutes)

**File**: `com/fulfilment/application/monolith/warehouses/adapters/api/WarehouseResource.java`

```java
@PUT
@Path("{code}/replace")
@Transactional
public Warehouse replaceWarehouse(
    @PathParam("code") String code,
    Warehouse newWarehouse) {
  
  replaceWarehouseUseCase.replace(code, newWarehouse);
  
  // Return the newly created warehouse
  return warehouseStore.findByBusinessUnitCode(code);
}
```

---

### STEP 4: Update Dependency Injection (3 minutes)

Inject ReplaceWarehouseUseCase into WarehouseResource:
```java
@Inject
ReplaceWarehouseUseCase replaceWarehouseUseCase;
```

---

### STEP 5: Implement Tests (15 minutes)

**Test File**: `ReplaceWarehouseUseCaseTest.java`

Test Cases:
1. ✅ Replace warehouse successfully (happy path)
2. ✅ Old warehouse not found
3. ✅ Old warehouse already archived
4. ✅ New location doesn't exist
5. ✅ New capacity insufficient for old stock
6. ✅ Stock mismatch
7. ✅ Old warehouse marked as archived
8. ✅ New warehouse created with same code
9. ✅ Multiple replacements in same location

---

## Code Quality Checklist

```
DOCUMENTATION:
☐ Interface JavaDoc with @param/@return/@throws
☐ Method-level JavaDoc for all public methods
☐ Clear comments on complex logic
☐ Business rule documentation

VALIDATION:
☐ Old warehouse exists check
☐ Old warehouse not archived check
☐ Location exists check
☐ Capacity accommodation check
☐ Stock matching validation
☐ New warehouse validity check
☐ Null checks on all inputs
☐ Clear error messages

IMPLEMENTATION:
☐ Archive old warehouse (set archivedAt)
☐ Create new warehouse with same code
☐ Transfer stock from old to new
☐ Logging for audit trail
☐ Proper exception types

TESTING:
☐ Happy path test
☐ Old warehouse not found
☐ Already archived warehouse
☐ Invalid location
☐ Insufficient capacity
☐ Stock mismatch
☐ Successful replacement with verification
☐ @DisplayName on all tests
```

---

## Key Implementation Details

### Business Unit Code Transfer
```
Old Warehouse:
  ├─ code: "WH-NYC-001"
  ├─ location: "ZWOLLE-001"
  ├─ capacity: 100
  ├─ stock: 80
  └─ archivedAt: [SET TO NOW]

New Warehouse Created:
  ├─ code: "WH-NYC-001"  ← SAME CODE
  ├─ location: "ZWOLLE-001"  ← (can be same or different)
  ├─ capacity: 150  ← NEW (can be larger)
  ├─ stock: 80  ← TRANSFERRED
  └─ archivedAt: null  ← NEW WAREHOUSE
```

### Soft Delete vs Archive
```
Old warehouse:
- NOT deleted
- marked as archived (archivedAt timestamp)
- creates history trail
- can be queried for audit purposes
```

### Stock Transfer Logic
```
Old warehouse stock → New warehouse stock
Ensures inventory continuity
No stock loss or duplication
```

---

## Implementation Pattern

### Similar to CreateWarehouseUseCase but with differences:

| Aspect | Create | Replace |
|--------|--------|---------|
| Code exists check | Must NOT exist | Must exist |
| Capacity validation | capacity >= stock | capacity >= old.stock |
| Stock validation | stock <= capacity | stock = old.stock |
| Archive old | N/A | Archive by setting archivedAt |
| Code usage | Use provided | Reuse old warehouse code |

---

## Error Handling

### Exception Types

```java
EntityNotFoundException
└─ Old warehouse not found
└─ New location not found

IllegalStateException
└─ Old warehouse already archived
└─ New capacity insufficient
└─ Stock mismatch
└─ Other business rule violations

IllegalArgumentException
└─ Null or blank code
└─ Null or blank location
└─ Missing capacity
```

---

## REST API Endpoint

### PUT /warehouse/{code}/replace

**Request**:
```json
{
  "location": "ZWOLLE-001",
  "capacity": 150,
  "stock": 80
}
```

**Success Response** (200):
```json
{
  "businessUnitCode": "WH-NYC-001",
  "location": "ZWOLLE-001",
  "capacity": 150,
  "stock": 80,
  "createdAt": "2026-02-28T12:00:00",
  "archivedAt": null
}
```

**Error Responses**:
- 404: Warehouse not found / Location not found
- 422: Business rule violation (capacity, stock, archived)
- 500: Server error

---

## Testing Strategy

### Unit Tests
- Test each validation independently
- Test happy path replacement
- Test error conditions

### Integration Tests
- Test full replacement workflow
- Verify old warehouse archived
- Verify new warehouse created
- Verify code transferred
- Verify stock transferred

### Edge Cases
- Replace with same capacity
- Replace with much larger capacity
- Multiple replacements in sequence
- Replace in different location (if allowed)

---

## Estimated Effort

```
Planning:                    15 minutes
Interface creation:           5 minutes
UseCase implementation:      35 minutes
Resource endpoint:            5 minutes
Tests implementation:         20 minutes
Code review & refactoring:   10 minutes
─────────────────────────────
Total:                       90 minutes (1.5 hours)
```

---

## Success Criteria

✅ ReplaceWarehouseOperation interface created
✅ ReplaceWarehouseUseCase fully implemented
✅ All 6 validations enforced
✅ Old warehouse archived (soft delete)
✅ New warehouse created with same code
✅ Stock transferred correctly
✅ REST endpoint working
✅ All tests passing
✅ Production-ready code quality
✅ Comprehensive documentation

---

## Next Steps

1. Create ReplaceWarehouseOperation interface
2. Create ReplaceWarehouseUseCase implementation
3. Add REST endpoint to WarehouseResource
4. Implement comprehensive tests
5. Verify all edge cases
6. Code review and refactoring
7. Final testing and validation

---

**Ready to implement?** Let's build this! 🚀

