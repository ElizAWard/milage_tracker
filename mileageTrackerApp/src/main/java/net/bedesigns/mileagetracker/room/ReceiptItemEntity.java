package net.bedesigns.mileagetracker.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;

import java.util.Date;

@Entity(tableName = "Fill_ups", primaryKeys = {"vehicleName", "currentMileage"})
public class ReceiptItemEntity {
    @NonNull
    public String vehicleName;
    @NonNull
    public Date date;
    @NonNull
    public int currentMileage;
    @NonNull
    public double gallonsPurchased;

    @Ignore
    public ReceiptItemEntity() {}

    @Ignore
    public ReceiptItemEntity(String vehicleName, Date date, int currentMileage, double gallonsPurchased) {
        this.vehicleName = vehicleName;
        this.date = date;
        this.currentMileage = currentMileage;
        this.gallonsPurchased = gallonsPurchased;
    }
}
