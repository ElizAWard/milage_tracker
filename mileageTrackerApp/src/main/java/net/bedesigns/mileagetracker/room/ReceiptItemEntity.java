package net.bedesigns.mileagetracker.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import java.util.Date;

@Entity(tableName = "Fill_ups", primaryKeys = {"vehicleName", "currentMileage"})
public class ReceiptItemEntity {
    @NonNull
    public String vehicleName;
    @NonNull
    public int currentMileage;
    @NonNull
    public Date date;
    @NonNull
    public double gasPurchased;
}
