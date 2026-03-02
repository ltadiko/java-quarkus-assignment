package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LocationGateway} class.
 *
 * Tests the location resolution functionality to ensure locations are correctly
 * retrieved from the static registry by their identification code.
 */
@DisplayName("LocationGateway Tests")
public class LocationGatewayTest {

  private LocationGateway locationGateway;

  @BeforeEach
  void setUp() {
    locationGateway = new LocationGateway();
  }

  @Test
  @DisplayName("Should return location when valid identification is provided")
  void testResolveExistingLocationReturnSuccess() {
    // Arrange
    final String validIdentification = "ZWOLLE-001";
    final int expectedMaxWarehouses = 1;
    final int expectedMaxCapacity = 40;

    // Act
    Location result = locationGateway.resolveByIdentifier(validIdentification);

    // Assert
    assertNotNull(result, "Location should not be null for valid identification");
    assertEquals(validIdentification, result.identification,
        "Location identification should match requested value");
    assertEquals(expectedMaxWarehouses, result.maxNumberOfWarehouses,
        "Location max warehouses should match expected value");
    assertEquals(expectedMaxCapacity, result.maxCapacity,
        "Location max capacity should match expected value");
  }

  @Test
  @DisplayName("Should return null when invalid identification is provided")
  void testResolveNonExistentLocationReturnNull() {
    // Arrange
    final String invalidIdentification = "INVALID-LOCATION-999";

    // Act
    Location result = locationGateway.resolveByIdentifier(invalidIdentification);

    // Assert
    assertNull(result, "Location should be null for non-existent identification");
  }

  @ParameterizedTest
  @ValueSource(strings = {"ZWOLLE-001", "AMSTERDAM-001", "TILBURG-001", "HELMOND-001", "EINDHOVEN-001"})
  @DisplayName("Should successfully resolve all supported locations")
  void testResolveMultipleValidLocations(String identification) {
    // Act
    Location result = locationGateway.resolveByIdentifier(identification);

    // Assert
    assertNotNull(result, "Location should exist for identification: " + identification);
    assertEquals(identification, result.identification,
        "Retrieved location identification should match requested value");
    assertTrue(result.maxNumberOfWarehouses > 0,
        "Location max warehouses should be positive");
    assertTrue(result.maxCapacity > 0,
        "Location max capacity should be positive");
  }

  @Test
  @DisplayName("Should handle multiple consecutive resolutions correctly")
  void testResolveMultipleLocationsIndependently() {
    // Arrange
    final String amsterdam = "AMSTERDAM-001";
    final String tilburg = "TILBURG-001";

    // Act
    Location amsterdamResult = locationGateway.resolveByIdentifier(amsterdam);
    Location tilburgResult = locationGateway.resolveByIdentifier(tilburg);

    // Assert - Amsterdam validation
    assertNotNull(amsterdamResult, "Amsterdam location should be found");
    assertEquals(amsterdam, amsterdamResult.identification);
    assertEquals(5, amsterdamResult.maxNumberOfWarehouses,
        "Amsterdam should support 5 warehouses");
    assertEquals(100, amsterdamResult.maxCapacity,
        "Amsterdam should have capacity of 100");

    // Assert - Tilburg validation
    assertNotNull(tilburgResult, "Tilburg location should be found");
    assertEquals(tilburg, tilburgResult.identification);
    assertEquals(1, tilburgResult.maxNumberOfWarehouses,
        "Tilburg should support 1 warehouse");
    assertEquals(40, tilburgResult.maxCapacity,
        "Tilburg should have capacity of 40");
  }

  @Test
  @DisplayName("Should return null for null identification input")
  void testResolveWithNullIdentificationReturnNull() {
    // Act
    Location result = locationGateway.resolveByIdentifier(null);

    // Assert
    assertNull(result, "Location should be null when identification is null");
  }

  @Test
  @DisplayName("Should return null for empty string identification")
  void testResolveWithEmptyIdentificationReturnNull() {
    // Act
    Location result = locationGateway.resolveByIdentifier("");

    // Assert
    assertNull(result, "Location should be null for empty identification string");
  }

  @Test
  @DisplayName("Should be case-sensitive when matching identifications")
  void testResolveCaseSensitiveMatching() {
    // Act
    Location resultUpperCase = locationGateway.resolveByIdentifier("ZWOLLE-001");
    Location resultLowerCase = locationGateway.resolveByIdentifier("zwolle-001");
    Location resultMixedCase = locationGateway.resolveByIdentifier("Zwolle-001");

    // Assert
    assertNotNull(resultUpperCase, "Should find location with correct uppercase format");
    assertNull(resultLowerCase, "Should not find location with lowercase format");
    assertNull(resultMixedCase, "Should not find location with mixed case format");
  }
}
