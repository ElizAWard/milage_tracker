package net.bedesigns.mileagetracker;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import net.bedesigns.mileagetracker.model.ReceiptItem;

import static org.junit.Assert.assertEquals;

public class CsvExportTest {

    private static ReceiptItem fillUp(int year, int month, int day, double gallons, int odometer) {
        Calendar date = Calendar.getInstance();
        date.clear();
        date.set(year, month, day);
        return new ReceiptItem(date.getTime(), gallons, odometer, null);
    }

    @Test
    public void header_namesTheThreeExportedColumns() {
        assertEquals("date,gallons,odometer", CsvExport.HEADER);
    }

    @Test
    public void emptyList_stillWritesTheHeader() {
        assertEquals("date,gallons,odometer\r\n", CsvExport.toCsv(new ArrayList<>()));
    }

    @Test
    public void singleFillUp_isWrittenUnderTheHeader() {
        List<ReceiptItem> items = Collections.singletonList(fillUp(2026, Calendar.OCTOBER, 27, 12.5, 45000));

        assertEquals("date,gallons,odometer\r\n2026-10-27,12.5,45000\r\n",
                CsvExport.toCsv(items));
    }

    @Test
    public void multipleFillUps_keepDisplayOrderOneRowEach() {
        List<ReceiptItem> items = new ArrayList<>();
        items.add(fillUp(2026, Calendar.OCTOBER, 27, 12.5, 45000));
        items.add(fillUp(2026, Calendar.SEPTEMBER, 3, 9.75, 44800));

        String csv = CsvExport.toCsv(items);

        assertEquals(3, csv.split("\r\n").length);
        assertEquals("2026-10-27,12.5,45000", csv.split("\r\n")[1]);
        assertEquals("2026-09-03,9.75,44800", csv.split("\r\n")[2]);
    }

    @Test
    public void dates_useIsoFormatSoNoColumnNeedsQuoting() {
        // The locale-aware display format would render "Oct 27, 2026" - comma included - which
        // would split into an extra column in any spreadsheet.
        String csv = CsvExport.toCsv(Collections.singletonList(fillUp(2026, Calendar.JANUARY, 2, 1.0, 100)));

        assertEquals("date,gallons,odometer\r\n2026-01-02,1.0,100\r\n", csv);
    }
}
