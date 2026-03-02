package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jboss.logging.Logger;

/**
 * Gateway implementation for resolving locations from a static registry.
 *
 * This class maintains a static collection of supported warehouse locations with their
 * capacity and warehouse limits. It implements the {@link LocationResolver} port to provide
 * location lookup functionality for warehouse operations.
 *
 * @see LocationResolver
 * @see Location
 */
@ApplicationScoped
public class LocationGateway implements LocationResolver {

  private static final Logger LOGGER = Logger.getLogger(LocationGateway.class);
  private static final List<Location> LOCATIONS = initializeLocations();

  /**
   * Resolves a location by its unique identification code.
   *
   * Performs a case-sensitive search through the registered locations and returns
   * the matching location or null if not found.
   *
   * @param identifier the unique location identification code (e.g., "ZWOLLE-001")
   * @return the matching {@link Location} or null if no location is found with the given identifier
   *
   * @see Location
   */
  @Override
  public Location resolveByIdentifier(String identifier) {
    if (identifier == null || identifier.isBlank()) {
      LOGGER.debug("Attempted to resolve location with null or blank identifier");
      return null;
    }

    return LOCATIONS.stream()
        .filter(location -> identifier.equals(location.identification))
        .findFirst()
        .orElse(null);
  }

  /**
   * Initializes the static registry of supported locations with their capacity and warehouse limits.
   *
   * This method populates the location registry with all supported warehouse locations in the system.
   * Each location specifies its maximum number of warehouses and total capacity.
   *
   * @return an unmodifiable list of all registered locations
   */
  private static List<Location> initializeLocations() {
    List<Location> locations = new ArrayList<>();

    // Netherlands - Zwolle region
    locations.add(new Location("ZWOLLE-001", 1, 40));
    locations.add(new Location("ZWOLLE-002", 2, 50));

    // Netherlands - Amsterdam region
    locations.add(new Location("AMSTERDAM-001", 5, 100));
    locations.add(new Location("AMSTERDAM-002", 3, 75));

    // Netherlands - Tilburg region
    locations.add(new Location("TILBURG-001", 1, 40));

    // Netherlands - Helmond region
    locations.add(new Location("HELMOND-001", 1, 45));

    // Netherlands - Eindhoven region
    locations.add(new Location("EINDHOVEN-001", 2, 70));

    // Netherlands - Vetsby region
    locations.add(new Location("VETSBY-001", 1, 90));

    LOGGER.infov("Location registry initialized with {0} locations", locations.size());
    return Collections.unmodifiableList(locations);
  }
}
