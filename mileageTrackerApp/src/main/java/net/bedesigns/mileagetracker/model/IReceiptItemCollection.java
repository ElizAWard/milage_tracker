package net.bedesigns.mileagetracker.model;

import java.util.Date;
import java.util.List;

public interface IReceiptItemCollection {

    /**
     * Retrives a list of vehicles stored in this collection.
     * @return The list of vehicles.
     */
    public List<String> GetVehicles();

    /**
     * Creates a new vehicle to store records for.
     * @param vehicleName The name of the new vehicle.
     */
    public void CreateVehicle(String vehicleName);

    /**
     * Adds a new {@link ReceiptItem} to the collection.
     * @param receipt The receipt to add.
     */
    public void AddReceipt(ReceiptItem receipt);

    /**
     * Deletes a given {@link ReceiptItem} from the collection.
     * @param receipt The receipt to delete.
     */
    public void DeleteReceipt(ReceiptItem receipt);

    /**
     * Retrieves a list of {@link ReceiptItem} from between the start and end dates.
     * @param start The date from which to start the list.
     * @param end The date to end the list.
     * @return The list of {@link ReceiptItem}.
     */
    public List<ReceiptItem> GetReceipts(Date start, Date end);

    /**
     * Retrieves a list of {@link ReceiptItem} from between the start date and now.
     * @param start The date from which to start the list.
     * @return The list of {@link ReceiptItem}.
     */
    public List<ReceiptItem> GetReceipts(Date start);

    /**
     * Saves the current data to persistent storage.
     */
    public void SaveToPersistentStorage();

    /**
     * Imports an XML file into the collection.
     * @param fileName The file name to import
     */
    public void ImportFromXml(String fileName);

    /**
     * Exports the collection to an XML file.
     * @param fileName The file name to write.
     */
    public void ExportToXml(String fileName);
}
