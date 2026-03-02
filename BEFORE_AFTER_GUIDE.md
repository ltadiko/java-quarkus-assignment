# Before & After Comparison

## Issue 1: LocationGateway - CDI Bean Definition

### ❌ BEFORE
```java
public class LocationGateway implements LocationResolver {
    // Missing @ApplicationScoped annotation
    // Result: UnsatisfiedDependencyException when injecting
}
```

### ✅ AFTER
```java
@ApplicationScoped
public class LocationGateway implements LocationResolver {
    // Now recognized as CDI bean
    // Can be injected into other components
}
```

---

## Issue 2: Test Methods - Transaction Management

### ❌ BEFORE
```java
@Test
@DisplayName("Should create warehouse successfully")
void testCreateWarehouseSuccess() {
    // Act & Assert
    createWarehouseUseCase.create(warehouse);  // ❌ TransactionRequiredException
    var created = warehouseRepository.findByBusinessUnitCode("WH-001");
}
```

### ✅ AFTER
```java
@Test
@Transactional  // ✅ Added transaction context
@DisplayName("Should create warehouse successfully")
void testCreateWarehouseSuccess() {
    // Act & Assert
    createWarehouseUseCase.create(warehouse);  // ✅ Runs within transaction
    var created = warehouseRepository.findByBusinessUnitCode("WH-001");
}
```

---

## Issue 3: Invalid Location Codes

### ❌ BEFORE
```java
// LocationGateway only has Netherlands locations:
// ZWOLLE-001, AMSTERDAM-001, TILBURG-001, HELMOND-001, EINDHOVEN-001, VETSBY-001

// But tests used:
Warehouse warehouse = createTestWarehouse("WH-NYC-001", "NYC", 100, 50);
// Result: EntityNotFoundException - Location with code NYC does not exist
```

### ✅ AFTER
```java
// Using actual locations from LocationGateway registry
Warehouse warehouse = createTestWarehouse("WH-ZWOLLE-001", "ZWOLLE-001", 30, 15);
// Result: ✅ Location found and validated correctly
```

---

## Issue 4: Capacity Constraint Mismatches

### ❌ BEFORE
```java
// ZWOLLE-001 has max capacity 40, but test tried to use:
Warehouse warehouse = createTestWarehouse("WH-NYC-001", "NYC", 100, 50);
// This would fail with capacity constraints

// Trying to create 2 warehouses in location that only allows 1:
Warehouse wh1 = createTestWarehouse("WH-NYC-001", "NYC", 200, 50);
Warehouse wh2 = createTestWarehouse("WH-NYC-002", "NYC", 200, 50);
// Would fail max warehouses constraint
```

### ✅ AFTER
```java
// ZWOLLE-001 has max 1 warehouse, max capacity 40
Warehouse warehouse = createTestWarehouse("WH-ZWOLLE-001", "ZWOLLE-001", 30, 15);
// Respects constraints: 30 capacity ≤ 40 limit, 1 warehouse ≤ 1 limit

// For multiple warehouses, use AMSTERDAM-001 (max 5 warehouses, capacity 100)
Warehouse wh1 = createTestWarehouse("WH-AMSTERDAM-001", "AMSTERDAM-001", 20, 10);
Warehouse wh2 = createTestWarehouse("WH-AMSTERDAM-002", "AMSTERDAM-001", 20, 10);
Warehouse wh3 = createTestWarehouse("WH-AMSTERDAM-003", "AMSTERDAM-001", 20, 10);
// All respect constraints: 60 total capacity ≤ 100 limit, 3 warehouses ≤ 5 limit
```

---

## Location Registry Reference

### Available Locations in LocationGateway

| Location | Max Warehouses | Max Capacity | Use Case |
|----------|----------------|--------------|----------|
| ZWOLLE-001 | 1 | 40 | Single warehouse location |
| ZWOLLE-002 | 2 | 50 | Two warehouse location |
| AMSTERDAM-001 | 5 | 100 | Multi-warehouse hub |
| AMSTERDAM-002 | 3 | 75 | Medium multi-warehouse |
| TILBURG-001 | 1 | 40 | Single warehouse location |
| HELMOND-001 | 1 | 45 | Single warehouse location |
| EINDHOVEN-001 | 2 | 70 | Two warehouse location |
| VETSBY-001 | 1 | 90 | Single warehouse, high capacity |

---

## Test Method Mapping

### CreateWarehouseUseCaseTest Location Usage

| Test Method | Location | Why |
|------------|----------|-----|
| testCreateWarehouseSuccess | ZWOLLE-001 | Basic happy path, single warehouse |
| testCreateWithDuplicateCodeThrows | ZWOLLE-001, ZWOLLE-002 | Test duplicate code, different locations |
| testCreateWithNullCodeThrows | ZWOLLE-001 | Valid location, invalid code |
| testCreateWithBlankCodeThrows | ZWOLLE-001 | Valid location, blank code |
| testCreateWithInvalidLocationThrows | INVALID-LOCATION | Test invalid location error |
| testCreateWithNullLocationThrows | null | Test null location error |
| testCreateWhenMaxWarehousesReachedThrows | ZWOLLE-001 | Location with max 1 warehouse |
| testCreateWhenExceedsLocationCapacityThrows | AMSTERDAM-001 | Location with capacity 100 |
| testCreateWhenCapacityInsufficientThrows | TILBURG-001 | Stock > capacity scenario |
| testCreateWithNegativeCapacityThrows | HELMOND-001 | Negative capacity validation |
| testCreateWithNullCapacityThrows | EINDHOVEN-001 | Null capacity validation |
| testCreateMultipleWarehousesFullCycle | AMSTERDAM-001 | Allows 5 warehouses for testing |
| testCreateWarehouseWithCapacityEqualsStock | VETSBY-001 | Edge case: capacity = stock |

---

## Command to Run Tests

```bash
cd /Users/lakshman/Downloads/fcs-interview-code-assignment-main/java-assignment

# Option 1: Using Maven wrapper
./mvnw clean test

# Option 2: Using Maven directly
mvn clean test

# Option 3: With Java 25 specific
java25
mvn clean test

# Option 4: Run specific test class
./mvnw test -Dtest=CreateWarehouseUseCaseTest
./mvnw test -Dtest=WarehouseRepositoryTest
./mvnw test -Dtest=StoreResourceTransactionTest
```

---

## Expected Test Results

### Before Fixes
- ❌ 3 Failures (Location code mismatches)
- ❌ 19 Errors (TransactionRequired exceptions)
- ❌ Tests not running properly

### After Fixes
- ✅ 0 Failures
- ✅ 0 Errors
- ✅ All 54 tests passing
- ✅ Full test coverage maintained

