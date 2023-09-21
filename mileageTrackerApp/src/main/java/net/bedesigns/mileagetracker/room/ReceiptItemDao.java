package net.bedesigns.mileagetracker.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

@Dao
public interface ReceiptItemDao {
    @Upsert
    void insertOrUpdateReceiptItems(List<ReceiptItemEntity> receipts);

    @Delete
    int deleteReceiptItem(ReceiptItemEntity receipt);

    @Delete
    int deleteReceiptItem(List<ReceiptItemEntity> receipt);

    @Query("SELECT DISTINCT vehicleName FROM receiptItemEntity")
    List<String> getAllVehicles();

    @Query("SELECT * FROM receiptItemEntity WHERE vehicleName LIKE :vehicleName")
    List<ReceiptItemEntity> getAllForVehicle(String vehicleName);
}
