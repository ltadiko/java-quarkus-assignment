# ✅ TASK 5 PLANNING COMPLETE

## Overview

**Task 5**: ReplaceWarehouse UseCase - Advanced warehouse replacement logic
**Status**: 📋 PLAN COMPLETE
**Estimated Duration**: 90 minutes (1.5 hours)
**Complexity**: ⭐⭐⭐⭐ Medium-High
**Files to Create**: 2 (Interface + UseCase)
**Files to Modify**: 1 (REST endpoint)

---

## What is Task 5?

### Business Context
When a company needs to upgrade or replace a warehouse in the same business unit (e.g., WH-NYC-001), they need to:
1. **Archive the old warehouse** (keep history)
2. **Create a new warehouse** in the same location
3. **Reuse the business unit code** (maintain tracking)
4. **Transfer the stock** from old to new warehouse

### The Challenge
This is NOT just creating a new warehouse. It requires:
- Finding and validating the **old** warehouse (exists)
- Ensuring the **new** warehouse can hold the old warehouse's stock
- Properly archiving the old one
- Creating the new one with the **same business unit code**

---

## 6 Validation Rules

| # | Rule | Check | Exception |
|---|------|-------|-----------|
| 1 | **Old Warehouse Exists** | Code must be found | EntityNotFoundException |
| 2 | **Not Already Archived** | Old warehouse active | IllegalStateException |
| 3 | **New Location Valid** | Location exists | EntityNotFoundException |
| 4 | **Capacity Sufficient** | new.capacity >= old.stock | IllegalStateException |
| 5 | **Stock Matching** | new.stock = old.stock | IllegalStateException |
| 6 | **New Warehouse Valid** | All new warehouse validations | Various |

---

## Implementation Structure

### 1. Create Interface
```java
public interface ReplaceWarehouseOperation {
  void replace(String oldBusinessUnitCode, Warehouse newWarehouse);
}
```

### 2. Create UseCase
```java
@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {
  // 6 validation methods
  // 2 processing methods
  // 1 main orchestration method
}
```

### 3. Add REST Endpoint
```java
@PUT
@Path("{code}/replace")
public Warehouse replaceWarehouse(
    @PathParam("code") String code,
    Warehouse newWarehouse)
```

### 4. Create Tests
```java
@QuarkusTest
public class ReplaceWarehouseUseCaseTest {
  // 9 test methods covering all scenarios
}
```

---

## Key Implementation Points

### Code Transfer
```
Old: "WH-NYC-001" → New: "WH-NYC-001"
Same code, new warehouse instance
```

### Soft Delete
```
Old warehouse archivedAt = NOW
Not deleted, just marked inactive
Creates audit trail
```

### Stock Transfer
```
Old warehouse stock 80 → New warehouse stock 80
Automatic transfer if not provided
Validation if provided
```

### Archival Process
```
1. Find old warehouse
2. Validate it exists and is active
3. Archive it (set archivedAt timestamp)
4. Create new warehouse with same code
5. Transfer stock automatically
```

---

## Example Flow

### Replace "WH-NYC-001" in "ZWOLLE-001"

**Before**:
```
Old Warehouse:
├─ Code: WH-NYC-001
├─ Location: ZWOLLE-001
├─ Capacity: 100
├─ Stock: 80
└─ Status: Active
```

**Request**:
```json
PUT /warehouse/WH-NYC-001/replace
{
  "location": "ZWOLLE-001",
  "capacity": 150,
  "stock": 80
}
```

**After**:
```
Old Warehouse (Archived):
├─ Code: WH-NYC-001
├─ Status: Archived
└─ archivedAt: 2026-02-28T14:30:00

New Warehouse (Active):
├─ Code: WH-NYC-001
├─ Location: ZWOLLE-001
├─ Capacity: 150
├─ Stock: 80
└─ Status: Active
```

---

## Tests to Implement (9 tests)

✅ Happy path - successful replacement
✅ Old warehouse not found
✅ Old warehouse already archived
✅ New location doesn't exist
✅ New capacity insufficient
✅ Stock mismatch
✅ Verify old warehouse archived
✅ Verify new warehouse created
✅ Verify code transferred

---

## Differences from Task 4 (CreateWarehouse)

| Feature | Create | Replace |
|---------|--------|---------|
| Code check | Must NOT exist | Must exist |
| Capacity vs stock | capacity >= stock | capacity >= old.stock |
| Stock source | From warehouse param | From old warehouse |
| Archive | N/A | Archive old warehouse |
| Code reuse | N/A | Use old warehouse code |
| Location constraint | New location | Old location (typically) |

---

## REST API Details

### Endpoint
```
PUT /warehouse/{businessUnitCode}/replace
```

### Parameters
- **Path**: `{businessUnitCode}` - Old warehouse code
- **Body**: New warehouse data

### Request Body
```json
{
  "location": "string",
  "capacity": 150,
  "stock": 80
}
```

### Success Response (200)
```json
{
  "businessUnitCode": "WH-NYC-001",
  "location": "ZWOLLE-001",
  "capacity": 150,
  "stock": 80,
  "createdAt": "2026-02-28T14:30:00",
  "archivedAt": null
}
```

### Error Responses
- **404**: Warehouse or location not found
- **422**: Business rule violation
- **500**: Server error

---

## Implementation Checklist

### Code
- [ ] Create ReplaceWarehouseOperation interface
- [ ] Create ReplaceWarehouseUseCase class
- [ ] Implement 6 validation methods
- [ ] Implement archive method
- [ ] Implement main orchestration logic
- [ ] Add REST endpoint
- [ ] Inject dependencies

### Tests
- [ ] Happy path test
- [ ] Error condition tests (6 types)
- [ ] Edge case tests
- [ ] Verification tests

### Quality
- [ ] JavaDoc on all public methods
- [ ] Clear error messages
- [ ] Logging for audit
- [ ] No null pointer risks
- [ ] All tests passing

### Documentation
- [ ] Inline code comments
- [ ] JavaDoc complete
- [ ] Test coverage documented

---

## Ready to Implement?

The complete implementation plan is in:
📄 **TASK_5_IMPLEMENTATION_PLAN.md**

This document contains:
- ✅ Detailed validation rules
- ✅ Step-by-step implementation guide
- ✅ Complete code templates
- ✅ Test cases
- ✅ Error handling strategy

---

## Next Action

Ready to start implementation? Say **"start task 5"** and we'll:

1. ✅ Create the interface
2. ✅ Implement the use case
3. ✅ Add REST endpoint
4. ✅ Write comprehensive tests
5. ✅ Verify all edge cases
6. ✅ Ensure production quality

---

**Task 5 Plan Status**: ✅ COMPLETE & READY

