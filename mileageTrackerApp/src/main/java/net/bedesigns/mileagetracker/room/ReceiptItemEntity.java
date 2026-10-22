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
    public double gallonsPurchased;
    @NonNull
    public int currentMileage;
    /**
     * Odometer reading of the previous fill-up, or null when this is the very first
     * fill-up recorded for the vehicle (in which case there is nothing to compute MPG from).
     */
    public Integer previousMileage;

    public ReceiptItemEntity() {}

    @Ignore
    public ReceiptItemEntity(String vehicleName, Date date, double gallonsPurchased,
                             int currentMileage, Integer previousMileage) {
        this.vehicleName = vehicleName;
        this.date = date;
        this.currentMileage = currentMileage;
        this.gallonsPurchased = gallonsPurchased;
        this.previousMileage = previousMileage;
    }
}
