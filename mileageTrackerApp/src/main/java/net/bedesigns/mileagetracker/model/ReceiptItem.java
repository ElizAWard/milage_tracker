package net.bedesigns.mileagetracker.model;

import java.util.Date;

public class ReceiptItem {
    private final Date date;
    private final Double gasPurchased;
    private final Integer currentMileage;
    private final Integer previousMileage;

    /**
     * @param previousMileage The odometer reading of the previous fill-up, or null when this is
     *                        the first fill-up recorded for the vehicle. MPG cannot be computed
     *                        for the first fill-up.
     */
    public ReceiptItem(Date date, double gasPurchased, Integer currentMileage, Integer previousMileage) {
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

    /**
     * Miles per gallon for this fill-up.
     *
     * @return The MPG, or null when it cannot be meaningfully calculated: the first fill-up has
     *         no previous reading, and values that would produce NaN/Infinity are rejected too.
     */
    public Double getAverageMpg() {
        if (previousMileage == null || currentMileage == null || gasPurchased == null || gasPurchased <= 0) {
            return null;
        }

        int tripMiles = currentMileage - previousMileage;
        return tripMiles > 0 ? tripMiles / gasPurchased : null;
    }
}
