package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Observer for Store lifecycle events.
 *
 * This observer listens to store events and synchronizes them with the legacy
 * system AFTER the database transaction commits. This ensures data consistency
 * between the operational database and legacy systems.
 *
 * The use of {@link TransactionPhase#AFTER_SUCCESS} guarantees that observers
 * are only invoked if the database transaction completes successfully, preventing
 * synchronization failures from corrupting the database state.
 *
 * @see StoreEvents
 * @see TransactionPhase
 */
@ApplicationScoped
public class StoreEventObserver {

  private static final Logger LOGGER = Logger.getLogger(StoreEventObserver.class);

  @Inject
  LegacyStoreManagerGateway legacyStoreManagerGateway;

  /**
   * Observes StoreCreatedEvent and synchronizes the created store with the legacy system.
   *
   * This observer is invoked AFTER the database transaction commits successfully.
   * This ensures the store is durably persisted before attempting legacy system
   * synchronization. If the legacy gateway fails, the database change is not rolled back.
   *
   * @param event the store created event, fired after successful database commit
   *
   * @see TransactionPhase#AFTER_SUCCESS
   */
  public void onStoreCreated(
      @Observes(during = TransactionPhase.AFTER_SUCCESS) StoreEvents.StoreCreatedEvent event) {
    try {
      LOGGER.infov("Synchronizing created store to legacy system: {0}", event.store.name);
      legacyStoreManagerGateway.createStoreOnLegacySystem(event.store);
      LOGGER.infov("Successfully synchronized created store: {0}", event.store.name);
    } catch (Exception e) {
      LOGGER.errorv(
          e,
          "Failed to synchronize created store to legacy system: {0}. "
              + "Manual intervention may be required.",
          event.store.name);
      // Note: Database change already committed, do not re-throw to avoid
      // failing the API response. Log error for operational monitoring.
    }
  }

  /**
   * Observes StoreUpdatedEvent and synchronizes the updated store with the legacy system.
   *
   * This observer is invoked AFTER the database transaction commits successfully.
   * This ensures the store updates are durably persisted before attempting legacy
   * system synchronization.
   *
   * @param event the store updated event, fired after successful database commit
   *
   * @see TransactionPhase#AFTER_SUCCESS
   */
  public void onStoreUpdated(
      @Observes(during = TransactionPhase.AFTER_SUCCESS) StoreEvents.StoreUpdatedEvent event) {
    try {
      LOGGER.infov("Synchronizing updated store to legacy system: {0}", event.store.name);
      legacyStoreManagerGateway.updateStoreOnLegacySystem(event.store);
      LOGGER.infov("Successfully synchronized updated store: {0}", event.store.name);
    } catch (Exception e) {
      LOGGER.errorv(
          e,
          "Failed to synchronize updated store to legacy system: {0}. "
              + "Manual intervention may be required.",
          event.store.name);
      // Note: Database change already committed, do not re-throw to avoid
      // failing the API response. Log error for operational monitoring.
    }
  }

  /**
   * Observes StoreDeletedEvent and synchronizes the store deletion with the legacy system.
   *
   * This observer is invoked AFTER the database transaction commits successfully.
   * This ensures the store deletion is durably persisted before attempting legacy
   * system synchronization.
   *
   * @param event the store deleted event, fired after successful database commit
   *
   * @see TransactionPhase#AFTER_SUCCESS
   */
  public void onStoreDeleted(
      @Observes(during = TransactionPhase.AFTER_SUCCESS) StoreEvents.StoreDeletedEvent event) {
    try {
      LOGGER.infov("Store deletion detected with ID: {0}", event.storeId);
      // Note: LegacyStoreManagerGateway does not have deleteStoreOnLegacySystem method yet.
      // When legacy gateway is extended, implement deletion sync here.
      // For now, just log the deletion for audit purposes.
      LOGGER.infov("Successfully processed deleted store with ID: {0}", event.storeId);
    } catch (Exception e) {
      LOGGER.errorv(
          e,
          "Failed to process deleted store with ID: {0}. "
              + "Manual intervention may be required.",
          event.storeId);
    }
  }
}

