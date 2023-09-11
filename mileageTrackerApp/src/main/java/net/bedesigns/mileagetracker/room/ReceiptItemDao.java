package net.bedesigns.mileagetracker.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Upsert;

import net.bedesigns.mileagetracker.model.ReceiptItem;

import java.util.List;

@Dao
public interface ReceiptItemDao {
    @Upsert
    void InsertOrUpdateReceiptItems(List<ReceiptItem> receipts);

    @Delete
    void DeleteReceiptItem(ReceiptItem receipt);

    @Query("SELECT * FROM Fill_ups WHERE vehicleName LIKE :vehicleName")
    List<ReceiptItem> GetAll(String vehicleName);
}
