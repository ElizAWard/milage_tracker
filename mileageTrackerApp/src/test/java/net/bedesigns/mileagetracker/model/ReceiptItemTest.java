package net.bedesigns.mileagetracker.model;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ReceiptItemTest {

    @Test
    public void firstFillUp_hasNoPreviousReadingAndNoMpg() {
        ReceiptItem first = new ReceiptItem(new Date(), 10.0, 120000, null);

        assertNull("The first fill-up has no previous odometer reading, so MPG is unknown.",
                first.getAverageMpg());
    }

    @Test
    public void secondFillUp_computesMpgFromPreviousReading() {
        // 300 miles driven on 15 gallons => 20 mpg
        ReceiptItem second = new ReceiptItem(new Date(), 15.0, 120300, 120000);

        assertEquals(Double.valueOf(20.0), second.getAverageMpg());
    }

    @Test
    public void nonIntegerMpg_isReportedAsIs() {
        // 250 miles on 8 gallons => 31.25 mpg
        ReceiptItem second = new ReceiptItem(new Date(), 8.0, 120250, 120000);

        assertEquals(Double.valueOf(31.25), second.getAverageMpg());
    }

    @Test
    public void zeroGallons_hasNoMpgRatherThanInfinity() {
        ReceiptItem bad = new ReceiptItem(new Date(), 0.0, 120300, 120000);

        assertNull("Zero gallons would divide by zero and yield Infinity.", bad.getAverageMpg());
    }

    @Test
    public void mileageGoingBackwards_hasNoMpg() {
        // Odometer reset / typo; a negative distance is not a real average.
        ReceiptItem bad = new ReceiptItem(new Date(), 15.0, 119000, 120000);

        assertNull(bad.getAverageMpg());
    }

    @Test
    public void mpgChain_survivesAcrossRestarts() {
        // Simulates the value being read back out of the database on a fresh launch.
        ReceiptItem restored = new ReceiptItem(new Date(), 15.0, 120300, 120000);

        assertEquals(Double.valueOf(20.0), restored.getAverageMpg());
    }
}
