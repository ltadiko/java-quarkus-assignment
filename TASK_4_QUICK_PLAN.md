# 📋 TASK 4 PLAN - QUICK SUMMARY

## Task Overview

**Task**: CreateWarehouseUseCase - Business Logic Implementation
**Time**: 60 minutes
**Complexity**: ⭐⭐⭐ Medium-High
**Files**: 1 modify + 1 create (tests)

---

## 5 Validations to Implement

```
1. Business Unit Code Verification ✅
   └─ Code must NOT already exist
   └─ Throw: IllegalStateException if duplicate

2. Location Validation ✅
   └─ Location MUST exist in system
   └─ Throw: EntityNotFoundException if not found

3. Warehouse Creation Feasibility ✅
   └─ Max warehouses per location NOT exceeded
   └─ Throw: IllegalStateException if max reached

4. Location Capacity Validation ✅
   └─ Total capacity (all warehouses) NOT exceeded
   └─ Throw: IllegalStateException if exceeds

5. Warehouse Capacity Validation ✅
   └─ Warehouse capacity >= stock being stored
   └─ Throw: IllegalStateException if insufficient
```

---

## Implementation Steps

```
STEP 1: Understand dependencies          (5 min)
STEP 2: Add Logger                       (2 min)
STEP 3: Add LocationGateway dependency   (3 min)
STEP 4: Implement validations            (30 min)
STEP 5: Create method                    (15 min)
STEP 6: Helper methods                   (20 min)
Write tests:                             (30 min)
────────────────────────────────────────────────
Total: 60 minutes
```

---

## Files to Work On

### Modify: CreateWarehouseUseCase.java
- Add: Logger
- Add: LocationGateway injection
- Add: create() method (30 lines)
- Add: 5 helper methods (80 lines)
- Add: JavaDoc (40 lines)
- Total: ~150 lines (from 25)

### Create: CreateWarehouseUseCaseTest.java
- Add: 11 test methods
- Coverage: All validations + happy path
- Total: ~300 lines

---

## Test Coverage

```
Happy Path:              1 test ✅
Duplicate Code:          1 test ✅
Null/Blank Code:         2 tests ✅
Invalid Location:        1 test ✅
Max Warehouses:          1 test ✅
Exceeds Location Cap:    1 test ✅
Insufficient Capacity:   1 test ✅
Negative Capacity:       1 test ✅
Full Cycle:              1 test ✅
────────────────────────────
Total:                  11 tests ✅
```

---

## Key Implementation Points

### Helper Methods Needed

1. **validateBusinessUnitCodeUnique()** - Check code doesn't exist
2. **validateLocationExists()** - Verify location in system
3. **validateWarehouseCreationFeasible()** - Check max warehouses
4. **validateLocationCapacity()** - Check total capacity
5. **validateWarehouseCapacity()** - Check warehouse capacity

### Exception Types

- **IllegalArgumentException** - Invalid input (null, blank)
- **IllegalStateException** - Business rule violation
- **EntityNotFoundException** - Resource not found

---

## Validation Logic Examples

### Validation 1: Unique Code
```java
Warehouse existing = warehouseStore.findByBusinessUnitCode(code);
if (existing != null) {
  throw new IllegalStateException("Code already exists");
}
```

### Validation 2: Location Exists
```java
Location location = locationGateway.resolveByIdentifier(locationCode);
if (location == null) {
  throw new EntityNotFoundException("Location not found");
}
```

### Validation 3: Max Warehouses
```java
long count = existing.stream()
  .filter(w -> w.location.equals(location.identification))
  .count();
if (count >= location.maxNumberOfWarehouses) {
  throw new IllegalStateException("Max reached");
}
```

### Validation 4: Location Capacity
```java
int used = existing.stream()
  .filter(w -> w.location.equals(location.identification))
  .mapToInt(w -> w.capacity).sum();
if (used + warehouse.capacity > location.maxCapacity) {
  throw new IllegalStateException("Exceeds capacity");
}
```

### Validation 5: Warehouse Capacity
```java
if (warehouse.stock != null && 
    warehouse.stock > warehouse.capacity) {
  throw new IllegalStateException("Insufficient capacity");
}
```

---

## Success Criteria

✅ All 5 validations implemented
✅ create() method fully functional
✅ 11 tests written + passing
✅ 100% JavaDoc coverage
✅ Senior-level code quality
✅ No compilation errors
✅ Proper error messages

---

## Documentation Files

See detailed plan: **TASK_4_IMPLEMENTATION_PLAN.md**

Contains:
- Detailed validation rules
- Complete code examples
- Test plan with all 11 tests
- Helper method signatures
- Error handling strategy

---

## Ready to Implement!

**Next**: Follow TASK_4_IMPLEMENTATION_PLAN.md step-by-step

**Estimated Time**: 60 minutes
**Status**: READY 🚀

