package net.bedesigns.mileagetracker.model;

import java.util.Calendar;
import java.util.Date;

public class ReceiptItem {
    private Date date;
    private Double gasPurchased;
    private Integer currentMileage;
    private Integer previousMileage;

    public ReceiptItem(Date date, double gasPurchased, Integer currentMileage, int previousMileage) {
        this.date = date;
        this.currentMileage = currentMileage;
        this.gasPurchased = gasPurchased;
        this.previousMileage = previousMileage;
    }

    public Date getDate() {
        return date;
    }

    public Double getGallonsPurchased() {
        return gasPurchased;
    }
    public Integer getCurrentMileage() {
        return currentMileage;
    }


    public Integer getPreviousMileage() {
        return previousMileage;
    }

    public Double getAverageMpg() {
        int tripMiles = currentMileage - previousMileage;
        return tripMiles / gasPurchased;
    }
}
