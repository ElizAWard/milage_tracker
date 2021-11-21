package net.bedesigns.mileagetracker;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;

public class ReceiptItem {

    private static final SimpleDateFormat americanFormat = new SimpleDateFormat("MM/dd/yyyy");

//    private Instant date;
    private long date;
    private Integer currentMileage;
    private Integer tripMiles;
    private double gasPurchased;

    public ReceiptItem(int currentMileage, double gasPurchased) {
        this(Calendar.getInstance().getTimeInMillis(), currentMileage, gasPurchased);
    }

    public ReceiptItem(Long date, int currentMileage, double gasPurchased) {
        this(date, currentMileage, null, gasPurchased);
    }

    public ReceiptItem(Long date, Integer currentMileage, Integer tripMiles, double gasPurchased) {
        this.date = date;
        this.currentMileage = currentMileage;
        this.tripMiles = tripMiles;
        this.gasPurchased = gasPurchased;
    }

    public Integer getCurrentMileage() {
        return currentMileage;
    }

    public double getGasPurchased() {
        return gasPurchased;
    }

    public double getMilePerGallon(int previousMileage) {
        double mileageDifference = currentMileage - previousMileage;
        return mileageDifference / gasPurchased;
    }

    public double getMilesPerGallon() {
        if (tripMiles != null) {
            return tripMiles / gasPurchased;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Date: %s%nCurrent Mileage: %s%nTrip Meter: %s%nGas Purchase: %s",
                americanFormat.format(date), currentMileage, tripMiles, gasPurchased));

        return builder.toString();
    }


}
