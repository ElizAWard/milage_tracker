package net.bedesigns.mileagetracker;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import net.bedesigns.mileagetracker.model.ReceiptItem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CsvImportTest {

    private static final String HEADER = "date,gallons,odometer\r\n";

    /** Three fill-ups, oldest first, written the way CsvExport writes them. */
    private static String threeRows() {
        return HEADER
                + "2026-01-01,10.0,100\r\n"
                + "2026-02-01,10.0,400\r\n"
                + "2026-03-01,15.0,700\r\n";
    }

    private static ReceiptItem fillUp(int year, int month, int day, double gallons, int odometer) {
        return fillUp(year, month, day, gallons, odometer, null);
    }

    private static ReceiptItem fillUp(int year, int month, int day, double gallons, int odometer,
                                      Integer previous) {
        Calendar date = Calendar.getInstance();
        date.clear();
        date.set(year, month, day);
        return new ReceiptItem(date.getTime(), gallons, odometer, previous);
    }

    @Test
    public void headerRow_isSkippedRatherThanParsed() throws Exception {
        List<ReceiptItem> items = CsvImport.parse(threeRows());

        assertEquals(3, items.size());
        assertEquals(Integer.valueOf(100), items.get(0).getCurrentMileage());
    }

    @Test
    public void eachRowBecomesThePreviousReadingOfTheRowAfterIt() throws Exception {
        List<ReceiptItem> items = CsvImport.parse(threeRows());

        assertNull("The oldest fill-up has nothing before it.", items.get(0).getPreviousMileage());
        assertEquals(Integer.valueOf(100), items.get(1).getPreviousMileage());
        assertEquals(Integer.valueOf(400), items.get(2).getPreviousMileage());
    }

    @Test
    public void everyRowButTheFirstGetsAnMpgValue() throws Exception {
        List<ReceiptItem> items = CsvImport.parse(threeRows());

        assertNull("No previous reading, so no MPG.", items.get(0).getAverageMpg());
        assertEquals("300 miles on 10 gallons", Double.valueOf(30.0), items.get(1).getAverageMpg());
        assertEquals("300 miles on 15 gallons", Double.valueOf(20.0), items.get(2).getAverageMpg());
    }

    @Test
    public void newestFirstFile_isStillChainedOldestFirst() throws Exception {
        // Our own export is written newest first. Chaining in file order would tie each row to
        // a HIGHER reading than itself, and no MPG would be calculated at all.
        String newestFirst = HEADER
                + "2026-03-01,15.0,700\r\n"
                + "2026-02-01,10.0,400\r\n"
                + "2026-01-01,10.0,100\r\n";

        List<ReceiptItem> items = CsvImport.parse(newestFirst);

        assertEquals(Integer.valueOf(100), items.get(0).getCurrentMileage());
        assertNull(items.get(0).getPreviousMileage());
        assertEquals(Double.valueOf(30.0), items.get(1).getAverageMpg());
        assertEquals(Double.valueOf(20.0), items.get(2).getAverageMpg());
    }

    @Test
    public void exportedFile_roundTripsBackIntoTheSameFillUps() throws Exception {
        List<ReceiptItem> original = new ArrayList<>();
        original.add(fillUp(2026, Calendar.JANUARY, 1, 10.0, 100));
        original.add(fillUp(2026, Calendar.FEBRUARY, 1, 10.0, 400, 100));
        original.add(fillUp(2026, Calendar.MARCH, 1, 15.0, 700, 400));

        List<ReceiptItem> reimported = CsvImport.parse(CsvExport.toCsv(original));

        assertEquals(3, reimported.size());
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i).getDate(), reimported.get(i).getDate());
            assertEquals(original.get(i).getCurrentMileage(), reimported.get(i).getCurrentMileage());
            assertEquals(original.get(i).getGallonsPurchased(), reimported.get(i).getGallonsPurchased());
            assertEquals(original.get(i).getPreviousMileage(), reimported.get(i).getPreviousMileage());
        }
    }

    @Test
    public void quotedColumns_areUnwrapped() throws Exception {
        String quoted = "\"date\",\"gallons\",\"odometer\"\r\n\"2026-01-01\",\"10.0\",\"100\"\r\n";

        List<ReceiptItem> items = CsvImport.parse(quoted);

        assertEquals(1, items.size());
        assertEquals(Integer.valueOf(100), items.get(0).getCurrentMileage());
    }

    @Test
    public void blankLinesAndByteOrderMark_areTolerated() throws Exception {
        List<ReceiptItem> items = CsvImport.parse("\uFEFF" + HEADER + "\r\n2026-01-01,10.0,100\r\n");

        assertEquals(1, items.size());
    }

    @Test
    public void headerOnlyFile_producesNoFillUps() throws Exception {
        assertTrue(CsvImport.parse(HEADER).isEmpty());
    }

    @Test
    public void unparseableRow_reportsItsLineNumber() {
        String bad = HEADER
                + "2026-01-01,10.0,100\r\n"
                + "someday,10.0,400\r\n";

        try {
            CsvImport.parse(bad);
            fail("A date that is not a date should be rejected.");
        } catch (CsvImport.CsvImportException e) {
            assertEquals(3, e.lineNumber);
            assertTrue(e.getMessage(), e.getMessage().startsWith("line 3:"));
        }
    }

    @Test
    public void nonPositiveGallons_isRejected() {
        try {
            CsvImport.parse(HEADER + "2026-01-01,0,100\r\n");
            fail("Zero gallons would produce a meaningless MPG.");
        } catch (CsvImport.CsvImportException e) {
            assertEquals(2, e.lineNumber);
        }
    }

    @Test
    public void shortRow_isRejected() {
        try {
            CsvImport.parse(HEADER + "2026-01-01,10.0\r\n");
            fail("A row without an odometer column should be rejected.");
        } catch (CsvImport.CsvImportException e) {
            assertEquals(2, e.lineNumber);
        }
    }

    @Test
    public void duplicateOdometer_isRejectedBecauseTheDatabaseKeysOnIt() {
        String duplicated = HEADER
                + "2026-01-01,10.0,400\r\n"
                + "2026-02-01,11.0,400\r\n";

        try {
            CsvImport.parse(duplicated);
            fail("Two rows cannot share an odometer reading.");
        } catch (CsvImport.CsvImportException e) {
            assertEquals(3, e.lineNumber);
            assertTrue(e.getMessage(), e.getMessage().contains("400"));
        }
    }

    @Test
    public void emptyInput_yieldsNothingRatherThanThrowing() throws Exception {
        assertTrue(CsvImport.parse("").isEmpty());
        assertTrue(CsvImport.parse("\r\n").isEmpty());
    }
}
