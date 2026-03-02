# 📚 Event-Driven Architecture Explanation - Task 2 Pattern

## What Pattern Is This?

This is the **Observer Pattern** combined with **Event-Driven Architecture**, specifically implemented using **Quarkus CDI Events**.

---

## Pattern Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                 OBSERVER PATTERN WITH CDI EVENTS                │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  1. PUBLISHER (StoreResource)                                   │
│     └─ Fires Event<StoreCreatedEvent>                           │
│                                                                   │
│  2. EVENT (StoreCreatedEvent)                                   │
│     └─ Data object (Store store)                                │
│                                                                   │
│  3. SUBSCRIBER (StoreEventObserver)                             │
│     └─ @Observes(during = TransactionPhase.AFTER_SUCCESS)      │
│     └─ Executes AFTER transaction commits                       │
│                                                                   │
│  4. RESULT                                                       │
│     └─ Loose coupling between database and legacy system        │
│     └─ Guaranteed consistency                                   │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Does It Use a Local Queue System?

**Short Answer**: **NO**, it does NOT use a separate queue system.

**What Actually Happens**:

```
Timeline of Execution:

T1: store.persist()                              [In transaction]
    └─ Store saved to database

T2: storeCreatedEvent.fire(new StoreCreatedEvent(store))
    └─ Event is queued INTERNALLY by Quarkus CDI
    └─ Event observer is NOT called yet

T3: @Transactional method returns
    └─ Transaction commits to database ✅

T4: AFTER successful commit
    └─ Quarkus fires the queued event
    └─ StoreEventObserver.onStoreCreated() is called
    └─ legacyStoreManagerGateway.createStoreOnLegacySystem() runs

T5: Legacy system is synced ✅
```

**Key Point**: The event is held in **Quarkus's internal event queue** (in-memory), NOT a separate message queue system like RabbitMQ or Kafka.

---

## How Does It Work? (Detailed Flow)

### Step 1: Fire the Event (in StoreResource)

```java
@POST
@Transactional
public Response create(Store store) {
    store.persist();                                    // Database operation
    storeCreatedEvent.fire(new StoreCreatedEvent(store));  // Event fired HERE
    return Response.ok(store).status(201).build();    // Returns immediately
}
```

**What happens**:
- Event is created: `new StoreCreatedEvent(store)`
- Event is fired: `storeCreatedEvent.fire(...)`
- **Control returns immediately** to the method
- Event is queued internally by Quarkus
- Method continues and transaction commits

---

### Step 2: Observe the Event (in StoreEventObserver)

```java
@ApplicationScoped
public class StoreEventObserver {
  
  public void onStoreCreated(
      @Observes(during = TransactionPhase.AFTER_SUCCESS) StoreCreatedEvent event) {
    // This method is NOT called until transaction succeeds!
    legacyStoreManagerGateway.createStoreOnLegacySystem(event.store);
  }
}
```

**Key parameters**:
- `@Observes` - Declares this method observes the event
- `during = TransactionPhase.AFTER_SUCCESS` - Only fires AFTER transaction commits
- `StoreCreatedEvent event` - The event parameter injected by Quarkus

---

### Step 3: Transaction Lifecycle

```
┌─────────────────────────────────────────────────────┐
│  TRANSACTION PHASES (with CDI Events)               │
├─────────────────────────────────────────────────────┤
│                                                      │
│  1. IN_PROGRESS                                     │
│     └─ storeCreatedEvent.fire() called              │
│     └─ Event queued (observer NOT called)           │
│                                                      │
│  2. BEFORE_COMPLETION                               │
│     └─ Observer NOT called                          │
│     └─ Database still in transaction                │
│                                                      │
│  3. AFTER_COMPLETION                                │
│     └─ Transaction succeeded? → Continue            │
│     └─ Transaction failed? → Skip observer          │
│                                                      │
│  4. AFTER_SUCCESS ✅                                │
│     └─ Observer is called HERE!                     │
│     └─ event.onStoreCreated() executed              │
│     └─ Legacy system synced                         │
│                                                      │
│  5. AFTER_FAILURE ❌                                │
│     └─ Observer NOT called (uses different phase)   │
│     └─ Database rolled back                         │
│     └─ Legacy system not called                     │
│                                                      │
└─────────────────────────────────────────────────────┘
```

---

## Comparison: With vs Without Events

### ❌ WITHOUT Events (WRONG - Current Implementation)

```java
@POST
@Transactional
public Response create(Store store) {
    store.persist();                          // T1: Saved to DB
    
    // ❌ PROBLEM: Still in transaction!
    // If this fails, store is already persisted
    legacyStoreManagerGateway.createStoreOnLegacySystem(store);  // T2: Might fail
    
    return Response.ok(store).status(201).build();
}

// Timeline:
// T1: persist() - saved to database
// T2: legacy call - might fail here
// T3: if T2 fails - exception thrown, transaction ROLLS BACK
// RESULT: Store deleted from DB ❌, but legacy system may have it ❌
```

### ✅ WITH Events (CORRECT - Task 2 Solution)

```java
@POST
@Transactional
public Response create(Store store) {
    store.persist();                          // T1: Saved to DB
    
    // ✅ CORRECT: Fire event
    storeCreatedEvent.fire(new StoreCreatedEvent(store));  // T2: Event queued
    
    return Response.ok(store).status(201).build();  // T3: Return immediately
}

// Observer (called AFTER transaction succeeds):
@Observes(during = TransactionPhase.AFTER_SUCCESS)
public void onStoreCreated(StoreCreatedEvent event) {
    // T4: AFTER transaction commits
    legacyStoreManagerGateway.createStoreOnLegacySystem(event.store);  // Safe!
}

// Timeline:
// T1: persist() - saved to database ✅
// T2: fire event - queued in memory
// T3: return - method returns
// T4: transaction commits ✅
// T5: observer called - legacy sync happens
// RESULT: Store in DB ✅, legacy system in sync ✅
```

---

## In-Memory Queue Mechanism

Quarkus uses an **in-memory event queue** built into its CDI container:

```
┌─────────────────────────────────────────────────┐
│      QUARKUS CDI EVENT QUEUE (In-Memory)        │
├─────────────────────────────────────────────────┤
│                                                  │
│  Event<StoreCreatedEvent> storeCreatedEvent     │
│                                                  │
│  When fire() is called:                         │
│  ├─ Event object created                        │
│  ├─ Added to internal queue                     │
│  ├─ Linked to transaction phase                 │
│  └─ Waits for phase to occur                    │
│                                                  │
│  When transaction commits:                      │
│  ├─ Check if AFTER_SUCCESS phase                │
│  ├─ Dequeue all AFTER_SUCCESS events            │
│  ├─ Call all @Observes methods                  │
│  └─ Queue becomes empty                         │
│                                                  │
└─────────────────────────────────────────────────┘
```

**Important**: This is **NOT a persistent queue**. It's:
- ✅ In-memory only
- ✅ Fast (no I/O)
- ✅ Guaranteed within same JVM
- ❌ Lost if application crashes
- ❌ Not distributed across multiple servers

---

## Real-World Analogy

Think of it like a **restaurant order system**:

```
WITHOUT Events (❌):
1. Kitchen starts cooking (persist())
2. Waiter calls supplier while cooking (legacy call)
3. If supplier call fails → dish is already cooked (can't undo!)
4. Customer gets inconsistent service

WITH Events (✅):
1. Kitchen starts cooking (persist())
2. Waiter writes order ticket → puts in a box (fire event)
3. Waiter delivers dish to table (return)
4. AFTER customer confirms receipt (transaction succeeds)
5. Waiter calls supplier (observer runs)
6. Everything is consistent ✅
```

---

## Transaction Phases Explained

Quarkus provides several phases for event observation:

```java
public enum TransactionPhase {
    // ❌ Observer called DURING transaction (not recommended)
    IN_PROGRESS,
    
    // ❌ Observer called BEFORE transaction commits (data not saved yet)
    BEFORE_COMPLETION,
    
    // ✅ Observer called AFTER successful commit (RECOMMENDED!)
    AFTER_SUCCESS,
    
    // Observer called AFTER failed commit (for cleanup)
    AFTER_FAILURE,
    
    // Observer called regardless of success/failure
    AFTER_COMPLETION
}
```

**For Task 2, we use**: `TransactionPhase.AFTER_SUCCESS`
- Guarantees database is committed
- Perfect for external system integration
- Observer is skipped if transaction rolls back

---

## Code Flow Diagram

```
StoreResource.create()
    │
    ├─[1] store.persist()
    │     └─ Database operation
    │
    ├─[2] storeCreatedEvent.fire(new StoreCreatedEvent(store))
    │     └─ Event created and queued
    │     └─ Observer method reference stored
    │
    ├─[3] return Response.ok(store).status(201).build()
    │     └─ Method returns immediately
    │
    ├─[4] @Transactional scope ends
    │     └─ Transaction commits to database
    │
    ├─[5] TransactionPhase.AFTER_SUCCESS triggers
    │     └─ Quarkus checks event queue
    │
    ├─[6] StoreEventObserver.onStoreCreated() called
    │     └─ legacyStoreManagerGateway.createStoreOnLegacySystem(store)
    │
    └─[7] Response sent to client
          └─ Legacy system sync happens asynchronously
```

---

## Key Advantages

### 1. **Transaction Safety** ✅
```java
// Database change is committed BEFORE legacy call
// If legacy call fails, database is still correct
storeCreatedEvent.fire(...);  // Safe to use
```

### 2. **Loose Coupling** ✅
```java
// StoreResource doesn't know about legacy details
// StoreEventObserver handles all legacy logic
// Easy to change or remove legacy integration
```

### 3. **Error Resilience** ✅
```java
// Even if legacy gateway throws exception:
try {
    legacyStoreManagerGateway.createStoreOnLegacySystem(store);
} catch (Exception e) {
    // Log error, but don't fail the API response
    // Store is already in database
    LOGGER.error("Legacy sync failed", e);
}
```

### 4. **Testability** ✅
```java
// Can mock the observer
// Can test StoreResource without legacy gateway
// Can test observer independently
```

---

## What If You Needed Persistent Queue?

If you needed **distributed** or **persistent** queue (for multi-server setup), you would use:

- **RabbitMQ** - Message broker
- **Kafka** - Event streaming
- **AWS SQS** - Cloud queue
- **Redis** - Message queue

But for **local single-server** scenarios like Task 2, **Quarkus CDI Events** is perfect.

---

## Event vs Traditional Approach

```
TRADITIONAL (Without Events):
Store.create() 
  ├─ Save to DB
  ├─ Call legacy directly
  └─ Hope it works

TIGHTLY COUPLED ❌
```

```
EVENT-DRIVEN (With Events):
Store.create()
  ├─ Save to DB
  ├─ Fire event
  └─ Return

LegacySync.sync()
  └─ Listen for event
  └─ Call legacy
  └─ Handle failures

LOOSELY COUPLED ✅
```

---

## Important Notes

### ⚠️ Not Async
```java
// This is NOT asynchronous!
storeCreatedEvent.fire(event);  // Waits for observer to complete
                                 // (unless you use @Async)
```

### ⚠️ No Retries Built-in
```java
// If observer fails, it fails
// You need to handle retries manually
try {
    legacyGateway.create(store);
} catch (Exception e) {
    // Implement retry logic here if needed
}
```

### ⚠️ In-Memory Only
```java
// If server crashes before AFTER_SUCCESS:
// Event is lost
// You need persistent queue for high reliability
```

---

## Summary

| Aspect | Detail |
|--------|--------|
| **Pattern** | Observer Pattern + CDI Events |
| **Queue Type** | In-memory (Quarkus CDI container) |
| **Persistent** | No (single JVM only) |
| **Async** | No (by default) |
| **Use Case** | Single-server, high-reliability integration |
| **Best For** | Task 2 requirements |
| **Distributed** | Not suitable (use RabbitMQ/Kafka) |

---

## When to Use CDI Events vs Message Queue

### Use CDI Events (Task 2) ✅
- Single server application
- Simple integrations
- Low latency critical
- No need for persistence
- Request-response pattern

### Use Message Queue (RabbitMQ/Kafka)
- Multiple servers/microservices
- Need persistence
- Complex workflows
- Async processing needed
- Pub/Sub pattern

---

**In summary**: Task 2 uses Quarkus's built-in **in-memory event queue**, not an external message queue system. It's simple, fast, and perfect for ensuring database commits before legacy system calls! 🚀

