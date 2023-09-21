package net.bedesigns.mileagetracker.model;

import java.util.Date;
import java.util.List;

public interface IReceiptItemCollection {

    /**
     * Retrieves a list of vehicles stored in this collection.
     * @return The list of vehicles.
     */
    List<String> getVehicles();

    /**
     * Creates a new vehicle to store records for.
     * <p>
     * This will set the new vehicle as the active vehicle.
     * @param vehicleName The name of the new vehicle.
     */
    void createVehicle(String vehicleName);

    /**
     * Removes an added vehicle to store records for.
     * <p>
     * This will set the active vehicle to null. Use the {@link #setActiveVehicle(String)} method immediately after calling this method.
     * @param vehicleName The name of the new vehicle.
     */
    void deleteVehicle(String vehicleName);

    /**
     * Sets the active vehicle to get / set items for.
     * @param vehicleName The name of the vehicle.
     * @return True if successfully set, false otherwise.
     */
    boolean setActiveVehicle(String vehicleName);

    /**
     * Adds a new {@link ReceiptItem} to the collection.
     * @param receipt The receipt to add.
     */
    void addReceipt(ReceiptItem receipt);

    /**
     * Deletes a given {@link ReceiptItem} from the collection.
     * @param receipt The receipt to delete.
     * @return True if successfully deleted, false otherwise.
     */
    boolean deleteReceipt(ReceiptItem receipt);

    /**
     * Retrieves a list of {@link ReceiptItem} from between the start and end dates.
     * @param start The date from which to start the list.
     * @param end The date to end the list.
     * @return The list of {@link ReceiptItem}.
     */
    List<ReceiptItem> getReceipts(Date start, Date end);

    /**
     * Retrieves a list of {@link ReceiptItem} from between the start date and now.
     * @param start The date from which to start the list.
     * @return The list of {@link ReceiptItem}.
     */
    List<ReceiptItem> getReceipts(Date start);

    /**
     * Saves the current data to persistent storage.
     */
    void saveToPersistentStorage();

    /**
     * Imports an XML file into the collection.
     * @param fileName The file name to import
     */
    void importFromXml(String fileName);

    /**
     * Exports the collection to an XML file.
     * @param fileName The file name to write.
     */
    void exportToXml(String fileName);
}
