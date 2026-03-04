package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jboss.logging.Logger;

@ApplicationScoped
public class LegacyStoreManagerGateway {

  private static final Logger LOGGER = Logger.getLogger(LegacyStoreManagerGateway.class);

  public void createStoreOnLegacySystem(Store store) {
    LOGGER.infov("Sending store creation to legacy system: {0}", store.name);
    writeToFile(store);
    LOGGER.infov("Successfully sent store creation to legacy system: {0}", store.name);
  }

  public void updateStoreOnLegacySystem(Store store) {
    LOGGER.infov("Sending store update to legacy system: {0}", store.name);
    writeToFile(store);
    LOGGER.infov("Successfully sent store update to legacy system: {0}", store.name);
  }

  private void writeToFile(Store store) {
    try {
      Path tempFile = Files.createTempFile(store.name, ".txt");
      LOGGER.debugv("Temporary file created at: {0}", tempFile);

      String content =
          "Store created. [ name ="
              + store.name
              + " ] [ items on stock ="
              + store.quantityProductsInStock
              + "]";
      Files.write(tempFile, content.getBytes());
      LOGGER.debugv("Data written to temporary file");

      String readContent = new String(Files.readAllBytes(tempFile));
      LOGGER.debugv("Data read from temporary file: {0}", readContent);

      Files.delete(tempFile);
      LOGGER.debugv("Temporary file deleted");

    } catch (Exception e) {
      LOGGER.errorv(e, "Failed to write store data to temporary file for store: {0}", store.name);
    }
  }
}
