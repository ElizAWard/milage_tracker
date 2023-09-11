package net.bedesigns.mileagetracker.room;

import androidx.room.Entity;

import java.util.Date;

@Entity(tableName = "Fill_ups", primaryKeys = {"vehicleName", "currentMileage"})
public class ReceiptItemEntity {
    public String vehicleName;
    public int currentMileage;
    public Date date;
    public double gasPurchased;
}
