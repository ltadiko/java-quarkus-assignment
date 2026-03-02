# 📋 TASK 2 IMPLEMENTATION PLAN - StoreResource Transaction Management

## Task Overview

**Objective**: Ensure legacy system integration happens AFTER database commit
**Current Issue**: `LegacyStoreManagerGateway` calls happen before transaction commits
**Solution**: Use Quarkus event-driven architecture with `@Observes(during = TransactionPhase.AFTER_SUCCESS)`

**Estimated Time**: 30 minutes
**Complexity**: ⭐⭐ Medium
**Files to Modify**: 3 files

---

## Current Problem

```java
// ❌ CURRENT (WRONG) - Legacy call before commit
@POST
@Transactional
public Response create(Store store) {
    store.persist();                                    // Not committed yet!
    legacyStoreManagerGateway.createStoreOnLegacySystem(store);  // Might fail
    return Response.ok(store).status(201).build();
}
```

**Why it's wrong**: 
- If legacy system call fails, store is already persisted
- No way to rollback database change
- Database and legacy system out of sync

---

## Solution Architecture

```
Current Flow:               Fixed Flow:
─────────────────         ──────────────────────
1. persist()              1. persist()
2. legacy call (❌)       2. fire event
3. return                 3. return
                          4. Event fires AFTER commit ✅
                          5. legacy call happens
```

---

## Step-by-Step Implementation Plan

### STEP 1: Create Event Classes (5 minutes)

**File**: `src/main/java/com/fulfilment/application/monolith/stores/StoreEvents.java`

Create three event classes:
```java
// Event: Store Created
public class StoreCreatedEvent {
    public Store store;
    public StoreCreatedEvent(Store store) {
        this.store = store;
    }
}

// Event: Store Updated
public class StoreUpdatedEvent {
    public Store store;
    public StoreUpdatedEvent(Store store) {
        this.store = store;
    }
}

// Event: Store Deleted
public class StoreDeletedEvent {
    public Long storeId;
    public StoreDeletedEvent(Long storeId) {
        this.storeId = storeId;
    }
}
```

**Why**: Events decouple database operations from legacy system calls

---

### STEP 2: Create Event Observer Class (5 minutes)

**File**: `src/main/java/com/fulfilment/application/monolith/stores/StoreEventObserver.java`

```java
/**
 * Observes store events and syncs with legacy system after transaction commit.
 * 
 * This observer ensures legacy system calls happen AFTER database changes are committed,
 * maintaining data consistency between operational and legacy systems.
 */
@ApplicationScoped
public class StoreEventObserver {
  
  private static final Logger LOGGER = Logger.getLogger(StoreEventObserver.class);
  
  @Inject
  LegacyStoreManagerGateway legacyStoreManagerGateway;

  /**
   * Observes StoreCreatedEvent and syncs with legacy system after transaction succeeds.
   *
   * @param event the store created event fired after successful database commit
   */
  public void onStoreCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) StoreCreatedEvent event) {
    try {
      LOGGER.infov("Syncing created store to legacy system: {0}", event.store.name);
      legacyStoreManagerGateway.createStoreOnLegacySystem(event.store);
    } catch (Exception e) {
      LOGGER.errorv(e, "Failed to sync created store to legacy system: {0}", event.store.name);
      // Note: Database change already committed, log error for manual intervention
    }
  }

  /**
   * Observes StoreUpdatedEvent and syncs with legacy system after transaction succeeds.
   *
   * @param event the store updated event fired after successful database commit
   */
  public void onStoreUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) StoreUpdatedEvent event) {
    try {
      LOGGER.infov("Syncing updated store to legacy system: {0}", event.store.name);
      legacyStoreManagerGateway.updateStoreOnLegacySystem(event.store);
    } catch (Exception e) {
      LOGGER.errorv(e, "Failed to sync updated store to legacy system: {0}", event.store.name);
      // Note: Database change already committed, log error for manual intervention
    }
  }

  /**
   * Observes StoreDeletedEvent and syncs with legacy system after transaction succeeds.
   *
   * @param event the store deleted event fired after successful database commit
   */
  public void onStoreDeleted(@Observes(during = TransactionPhase.AFTER_SUCCESS) StoreDeletedEvent event) {
    try {
      LOGGER.infov("Syncing deleted store to legacy system with ID: {0}", event.storeId);
      legacyStoreManagerGateway.deleteStoreOnLegacySystem(event.storeId);
    } catch (Exception e) {
      LOGGER.errorv(e, "Failed to sync deleted store to legacy system with ID: {0}", event.storeId);
      // Note: Database change already committed, log error for manual intervention
    }
  }
}
```

**Key Points**:
- `@Observes(during = TransactionPhase.AFTER_SUCCESS)` - Fires AFTER transaction commits
- `@ApplicationScoped` - Single instance for whole application
- Try-catch to handle legacy system failures gracefully

---

### STEP 3: Update StoreResource.java (10 minutes)

**File**: `src/main/java/com/fulfilment/application/monolith/stores/StoreResource.java`

**Changes to make**:

1. **Add Event injection**:
```java
@Inject
Event<StoreCreatedEvent> storeCreatedEvent;

@Inject
Event<StoreUpdatedEvent> storeUpdatedEvent;

@Inject
Event<StoreDeletedEvent> storeDeletedEvent;
```

2. **Update POST method** (create):
```java
// OLD:
store.persist();
legacyStoreManagerGateway.createStoreOnLegacySystem(store);

// NEW:
store.persist();
storeCreatedEvent.fire(new StoreCreatedEvent(store));
```

3. **Update PUT method** (update):
```java
// OLD:
entity.name = updatedStore.name;
entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
legacyStoreManagerGateway.updateStoreOnLegacySystem(updatedStore);

// NEW:
entity.name = updatedStore.name;
entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
storeUpdatedEvent.fire(new StoreUpdatedEvent(entity));
```

4. **Update PATCH method**:
```java
// OLD:
if (entity.name != null) {
  entity.name = updatedStore.name;
}
if (entity.quantityProductsInStock != 0) {
  entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
}
legacyStoreManagerGateway.updateStoreOnLegacySystem(updatedStore);

// NEW:
if (entity.name != null) {
  entity.name = updatedStore.name;
}
if (entity.quantityProductsInStock != 0) {
  entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
}
storeUpdatedEvent.fire(new StoreUpdatedEvent(entity));
```

5. **Update DELETE method**:
```java
// OLD:
entity.delete();
return Response.status(204).build();

// NEW:
Long storeId = entity.id;
entity.delete();
storeDeletedEvent.fire(new StoreDeletedEvent(storeId));
return Response.status(204).build();
```

---

### STEP 4: Write Tests (10 minutes)

**File**: `src/test/java/com/fulfilment/application/monolith/stores/StoreResourceTransactionTest.java`

```java
/**
 * Tests for StoreResource transaction management with event-driven legacy system integration.
 */
@QuarkusTest
public class StoreResourceTransactionTest {
  
  @Inject
  StoreResource storeResource;
  
  @Inject
  LegacyStoreManagerGateway legacyGateway;
  
  @BeforeEach
  void setUp() {
    // Setup test data
  }
  
  @Test
  @DisplayName("Should sync store to legacy system after database commit on create")
  void testCreateStoreWithLegacySync() {
    // Arrange
    Store store = new Store("Test Store");
    store.quantityProductsInStock = 100;
    
    // Act
    Response response = storeResource.create(store);
    
    // Assert
    assertEquals(201, response.getStatus());
    // Verify legacy gateway was called (use mock/spy if available)
    // Legacy system should have received the store
  }
  
  @Test
  @DisplayName("Should propagate database changes even if legacy sync fails")
  void testCreateStoreWithFailingLegacySync() {
    // Arrange
    Store store = new Store("Test Store");
    store.quantityProductsInStock = 100;
    
    // Mock legacy gateway to throw exception
    // doThrow(new RuntimeException()).when(legacyGateway).createStoreOnLegacySystem(any());
    
    // Act
    Response response = storeResource.create(store);
    
    // Assert
    assertEquals(201, response.getStatus());
    // Verify store was still created in database despite legacy failure
    Store created = Store.findById(store.id);
    assertNotNull(created);
  }
  
  @Test
  @DisplayName("Should update store and sync to legacy after commit")
  void testUpdateStoreWithLegacySync() {
    // Arrange
    Store store = new Store("Original Store");
    store.persist();
    
    Store updatedStore = new Store("Updated Store");
    updatedStore.quantityProductsInStock = 200;
    
    // Act
    Store result = storeResource.update(store.id, updatedStore);
    
    // Assert
    assertEquals("Updated Store", result.name);
    assertEquals(200, result.quantityProductsInStock);
    // Verify legacy gateway was called with updated data
  }
}
```

---

## Code Quality Checklist

```
DOCUMENTATION:
☐ StoreEvents.java has class JavaDoc
☐ Each event class has JavaDoc
☐ StoreEventObserver.java has class JavaDoc
☐ Each observer method has JavaDoc explaining TransactionPhase.AFTER_SUCCESS
☐ StoreResource methods have @param/@return docs if added

VALIDATION:
☐ Event objects not null before firing
☐ Exception handling in observer methods
☐ Logging for debugging

CODE ORGANIZATION:
☐ Events in separate file (StoreEvents.java)
☐ Observer in separate file (StoreEventObserver.java)
☐ StoreResource modified cleanly
☐ No code duplication

TESTING:
☐ Happy path test (successful sync)
☐ Failure scenario test (legacy system fails)
☐ All CRUD operations tested
☐ Event observation verified
```

---

## Files to Create

1. **StoreEvents.java** (new file)
   - StoreCreatedEvent class
   - StoreUpdatedEvent class
   - StoreDeletedEvent class
   - ~40 lines

2. **StoreEventObserver.java** (new file)
   - Observer for StoreCreatedEvent
   - Observer for StoreUpdatedEvent
   - Observer for StoreDeletedEvent
   - ~80 lines

3. **StoreResourceTransactionTest.java** (new file)
   - Test successful creation with sync
   - Test failure resilience
   - Test all CRUD operations
   - ~100 lines

## Files to Modify

1. **StoreResource.java** (modify existing)
   - Add @Inject Event<StoreCreatedEvent>
   - Add @Inject Event<StoreUpdatedEvent>
   - Add @Inject Event<StoreDeletedEvent>
   - Replace legacy calls with event fires
   - ~30 lines added/modified

---

## Key Concepts

### TransactionPhase.AFTER_SUCCESS
- Fires observer **ONLY** if transaction commits successfully
- If transaction rolls back, observer is NOT called
- Perfect for external system integration

### Event Firing
```java
// Fire event after database change
storeCreatedEvent.fire(new StoreCreatedEvent(store));
// Method returns immediately
// Event observer runs AFTER transaction commits
```

### Error Handling Strategy
- If legacy sync fails, database change is already committed
- Log error for manual intervention
- Don't re-throw exception (would break API response)

---

## Success Criteria

✅ **Task 2 is COMPLETE when**:
- [ ] StoreEvents.java created with 3 event classes
- [ ] StoreEventObserver.java created with 3 observer methods
- [ ] StoreResource.java modified to fire events instead of calling legacy gateway
- [ ] All existing tests still pass
- [ ] New tests verify event-driven behavior
- [ ] Code follows senior-level standards
- [ ] All methods have JavaDoc
- [ ] No code quality issues

---

## Troubleshooting

**Problem**: Legacy gateway not being called
**Solution**: Check @Observes(during = TransactionPhase.AFTER_SUCCESS) is correct

**Problem**: Observer methods not being called
**Solution**: 
- Verify Event<YourEvent> is injected
- Ensure event.fire() is called
- Check exception isn't swallowing event

**Problem**: Tests failing
**Solution**:
- Use @QuarkusTest
- Inject LegacyStoreManagerGateway
- Verify mock/spy behavior

---

## Time Breakdown

```
Understanding: 5 min
Creating Events: 5 min
Creating Observer: 5 min
Modifying StoreResource: 10 min
Writing Tests: 10 min
Verification: 5 min
─────────────────────
Total: 40 minutes
```

---

## Next Task Preview

**After Task 2**: Task 3 - WarehouseRepository CRUD (40 min)
- Implement 4 CRUD methods
- Easier than Task 2
- Good foundation for later tasks

---

**Ready to implement Task 2?** 
Follow this plan step-by-step and you'll have it done in 30-40 minutes!

Good luck! 🚀

