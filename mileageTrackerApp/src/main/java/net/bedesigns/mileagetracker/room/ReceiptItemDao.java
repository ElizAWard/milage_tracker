package net.bedesigns.mileagetracker.room;

import androidx.lifecycle.LiveData;
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

    @Query("SELECT DISTINCT vehicleName FROM Fill_ups ORDER BY vehicleName")
    List<String> getAllVehicles();

    /**
     * Synchronous read, used by the {@link ReceiptItemRoom} collection. Newest first.
     */
    @Query("SELECT * FROM Fill_ups WHERE vehicleName = :vehicleName ORDER BY currentMileage DESC")
    List<ReceiptItemEntity> getAllForVehicle(String vehicleName);

    /**
     * Observable read for the UI. Room re-delivers this on every insert/update/delete against
     * the {@code Fill_ups} table, so the list stays correct across app restarts for free.
     * Newest first.
     */
    @Query("SELECT * FROM Fill_ups WHERE vehicleName = :vehicleName ORDER BY currentMileage DESC")
    LiveData<List<ReceiptItemEntity>> observeFillUpsForVehicle(String vehicleName);

    @Query("SELECT COUNT(*) FROM Fill_ups WHERE vehicleName = :vehicleName")
    int getFillUpCount(String vehicleName);
}
