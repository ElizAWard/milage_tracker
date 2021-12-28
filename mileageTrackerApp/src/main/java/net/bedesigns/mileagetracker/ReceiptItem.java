package net.bedesigns.mileagetracker;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;

public class ReceiptItem {

    private static final SimpleDateFormat americanFormat = new SimpleDateFormat("MM/dd/yyyy");

//    private Instant date;
    private long date;
    private double previousMileage;
    private double currentMileage;
    private Double tripMiles;
    private double gasPurchased;

    public ReceiptItem(double previousMileage, double currentMileage, double gasPurchased) {
        this(Calendar.getInstance().getTimeInMillis(), previousMileage, currentMileage, gasPurchased);
    }

    public ReceiptItem(Long date, double previousMileage, double currentMileage, double gasPurchased) {
        this(date, previousMileage, currentMileage, null, gasPurchased);
    }

    public ReceiptItem(Long date, double previousMileage, double currentMileage, Double tripMiles, double gasPurchased) {
        this.date = date;
        this.currentMileage = currentMileage;
        this.tripMiles = tripMiles;
        this.gasPurchased = gasPurchased;
    }

    public Double getCurrentMileage() {
        return currentMileage;
    }

    public Double getPreviousMileage() {
        return previousMileage;
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

    public String getDate() {
        return americanFormat.format(date);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Date: %s%nPrevious Mileage: %s%nCurrent Mileage: %s%nTrip Meter: %s%nGas Purchase: %s",
                americanFormat.format(date), previousMileage, currentMileage, tripMiles, gasPurchased));

        return builder.toString();
    }


}
