package net.bedesigns.mileagetracker.room;

import net.bedesigns.mileagetracker.model.IReceiptItemCollection;
import net.bedesigns.mileagetracker.model.ReceiptItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ReceiptItemRoom implements IReceiptItemCollection {
    private final ReceiptItemDao dao;
    private List<String> availableVehicles;
    private List<ReceiptItem> currentVehicleData;
    private String currentVehicle;

    public ReceiptItemRoom(ReceiptItemDao dao) {
        this.dao = Objects.requireNonNull(dao, "Data access object must not be null.");
        ReceiptItemDatabase.databaseWriteExecutor.execute(() ->
                availableVehicles = this.dao.getAllVehicles());
    }

    @Override
    public List<String> getVehicles() {
        return new ArrayList<>(availableVehicles);
    }

    @Override
    public void createVehicle(String vehicleName) {
        saveToPersistentStorage();
        availableVehicles.add(vehicleName);
        currentVehicleData = new ArrayList<>();
        currentVehicle = vehicleName;
    }

    @Override
    public void deleteVehicle(String vehicleName) {
        ReceiptItemDatabase.databaseWriteExecutor.execute(() ->
                dao.deleteReceiptItem(convertModelToEntity(currentVehicleData)));
        availableVehicles.remove(vehicleName);
        currentVehicleData = new ArrayList<>();
        currentVehicle = null;
    }

    @Override
    public boolean setActiveVehicle(String vehicleName) {
        boolean isListedVehicle = availableVehicles.contains(vehicleName);

        if (isListedVehicle) {
            saveToPersistentStorage();
            ReceiptItemDatabase.databaseWriteExecutor.execute(() ->
                    currentVehicleData = convertEntityToModel(dao.getAllForVehicle(vehicleName)));
            currentVehicle = vehicleName;
        }

        return isListedVehicle;
    }

    @Override
    public void addReceipt(ReceiptItem receipt) {
        currentVehicleData.add(receipt);
    }

    @Override
    public boolean deleteReceipt(ReceiptItem receipt) {
        boolean removedFromList = currentVehicleData.remove(receipt);
        boolean removedFromDatabase;

        Future<Integer> future = ReceiptItemDatabase.databaseWriteExecutor.submit(() ->
                dao.deleteReceiptItem(convertModelToEntity(receipt)));

        try {
            removedFromDatabase = future.get() == 1;
        } catch (ExecutionException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        return removedFromList && removedFromDatabase;
    }

    @Override
    public List<ReceiptItem> getReceipts(Date start, Date end) {
        // TODO Implement GetReceipts()
        throw new UnsupportedOperationException("GetReceipts() not implemented yet.");
    }

    @Override
    public List<ReceiptItem> getReceipts(Date start) {
        // TODO Implement GetReceipts()
        throw new UnsupportedOperationException("GetReceipts() not implemented yet.");
    }

    @Override
    public void saveToPersistentStorage() {
        ReceiptItemDatabase.databaseWriteExecutor.execute(() ->
                dao.insertOrUpdateReceiptItems(convertModelToEntity(currentVehicleData)));
    }

    @Override
    public void importFromXml(String fileName) {
        // TODO Implement ImportFromXml()
        throw new UnsupportedOperationException("Import from XML not implemented yet.");
    }

    @Override
    public void exportToXml(String fileName) {
        // TODO Implement ExportToXml()
        throw new UnsupportedOperationException("Export to XML not implemented yet.");
    }

    private ReceiptItem convertEntityToModel(ReceiptItemEntity entity) {
        return new ReceiptItem(entity.date, entity.currentMileage, entity.gallonsPurchased);
    }

    private ReceiptItemEntity convertModelToEntity(ReceiptItem item) {
        return new ReceiptItemEntity(currentVehicle, item.getDate(), item.getCurrentMileage(), item.getGallonsPurchased());
    }

    private List<ReceiptItem> convertEntityToModel(List<ReceiptItemEntity> entityList) {
        List<ReceiptItem> itemList = new ArrayList<>();

        for (ReceiptItemEntity entity : entityList) {
            itemList.add(convertEntityToModel(entity));
        }

        return itemList;
    }

    private List<ReceiptItemEntity> convertModelToEntity(List<ReceiptItem> itemList) {
        List<ReceiptItemEntity> entityList = new ArrayList<>();

        for (ReceiptItem item : itemList) {
            entityList.add(convertModelToEntity(item));
        }

        return entityList;
    }
}
