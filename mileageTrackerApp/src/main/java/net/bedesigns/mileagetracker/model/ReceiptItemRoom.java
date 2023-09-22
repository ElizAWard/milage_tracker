package net.bedesigns.mileagetracker.model;

import net.bedesigns.mileagetracker.room.ReceiptItemDao;
import net.bedesigns.mileagetracker.room.ReceiptItemDatabase;
import net.bedesigns.mileagetracker.room.ReceiptItemEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReceiptItemRoom implements IReceiptItemCollection{
    private final ReceiptItemDao dao;
    private final List<String> availableVehicles;
    private List<ReceiptItem> currentVehicleData;
    private String currentVehicle;

    public ReceiptItemRoom(ReceiptItemDao dao) {
        if (dao == null) {
            throw new IllegalArgumentException("Data access object must not be null.");
        }

        this.dao = dao;
        availableVehicles = this.dao.GetAllVehicles();
    }

    @Override
    public List<String> GetVehicles() {
        return new ArrayList<>(availableVehicles);
    }

    @Override
    public void CreateVehicle(String vehicleName) {
        SaveToPersistentStorage();
        availableVehicles.add(vehicleName);
        currentVehicleData = new ArrayList<>();
        currentVehicle = vehicleName;
    }

    @Override
    public void DeleteVehicle(String vehicleName) {
        dao.DeleteReceiptItem(ConvertModelToEntity(currentVehicleData));
        availableVehicles.remove(vehicleName);
        currentVehicleData = new ArrayList<>();
        currentVehicle = null;
    }

    @Override
    public boolean SetActiveVehicle(String vehicleName) {
        boolean isListedVehicle = availableVehicles.contains(vehicleName);

        if (isListedVehicle) {
            SaveToPersistentStorage();
            currentVehicleData = ConvertEntityToModel(dao.GetAllForVehicle(vehicleName));
            currentVehicle = vehicleName;
        }

        return isListedVehicle;
    }

    @Override
    public void AddReceipt(ReceiptItem receipt) {
        currentVehicleData.add(receipt);
    }

    @Override
    public boolean DeleteReceipt(ReceiptItem receipt) {
        boolean removedFromList = currentVehicleData.remove(receipt);
        boolean removedFromDatabase = dao.DeleteReceiptItem(ConvertModelToEntity(receipt)) == 1;

        return removedFromList && removedFromDatabase;
    }

    @Override
    public List<ReceiptItem> GetReceipts(Date start, Date end) {
        throw new UnsupportedOperationException("GetReceipts() not implemented yet.");
    }

    @Override
    public List<ReceiptItem> GetReceipts(Date start) {
        throw new UnsupportedOperationException("GetReceipts() not implemented yet.");
    }

    @Override
    public void SaveToPersistentStorage() {
        ReceiptItemDatabase.databaseWriteExecutor.execute(() ->
                dao.InsertOrUpdateReceiptItems(ConvertModelToEntity(currentVehicleData)));
    }

    @Override
    public void ImportFromXml(String fileName) {
        throw new UnsupportedOperationException("Import from XML not implemented yet.");
    }

    @Override
    public void ExportToXml(String fileName) {
        throw new UnsupportedOperationException("Export to XML not implemented yet.");
    }

    private ReceiptItem ConvertEntityToModel(ReceiptItemEntity entity) {
        return new ReceiptItem(entity.date, entity.currentMileage, entity.gallonsPurchased);
    }

    private ReceiptItemEntity ConvertModelToEntity(ReceiptItem item) {
        return new ReceiptItemEntity(currentVehicle, item.GetDate(), item.GetCurrentMileage(), item.GetGallonsPurchased());
    }

    private List<ReceiptItem> ConvertEntityToModel(List<ReceiptItemEntity> entityList) {
        List<ReceiptItem> itemList = new ArrayList<>();

        for (ReceiptItemEntity entity : entityList) {
            itemList.add(ConvertEntityToModel(entity));
        }

        return itemList;
    }

    private List<ReceiptItemEntity> ConvertModelToEntity(List<ReceiptItem> itemList) {
        List<ReceiptItemEntity> entityList = new ArrayList<>();

        for (ReceiptItem item : itemList) {
            entityList.add(ConvertModelToEntity(item));
        }

        return entityList;
    }
}
