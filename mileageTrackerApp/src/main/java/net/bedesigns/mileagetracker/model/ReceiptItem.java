package net.bedesigns.mileagetracker.model;

import java.util.Calendar;
import java.util.Date;

public class ReceiptItem {
//    private Instant date;
    private Date date;
    private int currentMileage;
    private double gasPurchased;

    public ReceiptItem(int currentMileage, double gasPurchased) {
        this(Calendar.getInstance().getTime(), currentMileage, gasPurchased);
    }

    public ReceiptItem(Date date, int currentMileage, double gasPurchased) {
        this.date = date;
        this.currentMileage = currentMileage;
        this.gasPurchased = gasPurchased;
    }

    public Date GetDate() {
        return date;
    }

    public int GetCurrentMileage() {
        return currentMileage;
    }

    public double GetGallonsPurchased() {
        return gasPurchased;
    }
}
