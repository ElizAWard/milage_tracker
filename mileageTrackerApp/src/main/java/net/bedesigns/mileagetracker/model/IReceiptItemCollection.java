package net.bedesigns.mileagetracker.model;

import java.util.Date;
import java.util.List;

public interface IReceiptItemCollection {

    /**
     * Retrieves a list of vehicles stored in this collection.
     * @return The list of vehicles.
     */
    List<String> GetVehicles();

    /**
     * Creates a new vehicle to store records for.
     * @param vehicleName The name of the new vehicle.
     */
    void CreateVehicle(String vehicleName);

    /**
     * Sets the active vehicle to get / set items for.
     * @param vehicleName The name of the vehicle.
     * @return True if successfully set, false otherwise.
     */
    boolean SetActiveVehicle(String vehicleName);

    /**
     * Adds a new {@link ReceiptItem} to the collection.
     * @param receipt The receipt to add.
     */
    void AddReceipt(ReceiptItem receipt);

    /**
     * Deletes a given {@link ReceiptItem} from the collection.
     * @param receipt The receipt to delete.
     * @return True if successfully deleted, false otherwise.
     */
    boolean DeleteReceipt(ReceiptItem receipt);

    /**
     * Retrieves a list of {@link ReceiptItem} from between the start and end dates.
     * @param start The date from which to start the list.
     * @param end The date to end the list.
     * @return The list of {@link ReceiptItem}.
     */
    List<ReceiptItem> GetReceipts(Date start, Date end);

    /**
     * Retrieves a list of {@link ReceiptItem} from between the start date and now.
     * @param start The date from which to start the list.
     * @return The list of {@link ReceiptItem}.
     */
    List<ReceiptItem> GetReceipts(Date start);

    /**
     * Saves the current data to persistent storage.
     */
    void SaveToPersistentStorage();

    /**
     * Imports an XML file into the collection.
     * @param fileName The file name to import
     */
    void ImportFromXml(String fileName);

    /**
     * Exports the collection to an XML file.
     * @param fileName The file name to write.
     */
    void ExportToXml(String fileName);
}
