package net.bedesigns.mileagetracker.model;

import java.time.Instant;
import java.util.Calendar;

public class ReceiptItem {
//    private Instant date;
    private long date;
    private int currentMileage;
    private double gasPurchased;

    public ReceiptItem(int currentMileage, double gasPurchased) {
        this(Calendar.getInstance().getTimeInMillis(), currentMileage, gasPurchased);
    }

    public ReceiptItem(Long date, int currentMileage, double gasPurchased) {
        this.date = date;
        this.currentMileage = currentMileage;
        this.gasPurchased = gasPurchased;
    }
}
