package com.fulfilment.application.monolith.stores;

/**
 * Domain events for Store lifecycle operations.
 *
 * These events decouple Store persistence from legacy system integration.
 * Events are fired after database operations and observed by StoreEventObserver
 * to ensure legacy system synchronization happens after database commits.
 *
 * @see StoreEventObserver
 */
public class StoreEvents {

  /**
   * Event fired when a store is successfully created.
   *
   * This event is fired after the store entity is persisted to the database.
   * Observers can use this event to perform post-creation operations such as
   * synchronizing with legacy systems.
   */
  public static class StoreCreatedEvent {
    public final Store store;

    /**
     * Constructs a StoreCreatedEvent with the newly created store.
     *
     * @param store the store entity that was created, must not be null
     */
    public StoreCreatedEvent(Store store) {
      this.store = store;
    }
  }

  /**
   * Event fired when a store is successfully updated.
   *
   * This event is fired after the store entity is updated in the database.
   * Observers can use this event to perform post-update operations such as
   * synchronizing changes with legacy systems.
   */
  public static class StoreUpdatedEvent {
    public final Store store;

    /**
     * Constructs a StoreUpdatedEvent with the updated store.
     *
     * @param store the store entity that was updated, must not be null
     */
    public StoreUpdatedEvent(Store store) {
      this.store = store;
    }
  }

  /**
   * Event fired when a store is successfully deleted.
   *
   * This event is fired after the store entity is deleted from the database.
   * Observers can use this event to perform post-deletion operations such as
   * synchronizing the deletion with legacy systems.
   */
  public static class StoreDeletedEvent {
    public final Long storeId;

    /**
     * Constructs a StoreDeletedEvent with the ID of the deleted store.
     *
     * @param storeId the ID of the store that was deleted, must not be null
     */
    public StoreDeletedEvent(Long storeId) {
      this.storeId = storeId;
    }
  }
}

