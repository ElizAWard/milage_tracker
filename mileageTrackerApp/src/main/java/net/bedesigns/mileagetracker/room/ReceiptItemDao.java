package net.bedesigns.mileagetracker.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

@Dao
public interface ReceiptItemDao {
    @Upsert
    void InsertOrUpdateReceiptItems(List<ReceiptItemEntity> receipts);

    @Delete
    int DeleteReceiptItem(ReceiptItemEntity receipt);

    @Delete
    int DeleteReceiptItem(List<ReceiptItemEntity> receipt);

    @Query("SELECT DISTINCT vehicleName FROM Fill_ups")
    List<String> GetAllVehicles();

    @Query("SELECT * FROM Fill_ups WHERE vehicleName LIKE :vehicleName")
    List<ReceiptItemEntity> GetAllForVehicle(String vehicleName);
}
