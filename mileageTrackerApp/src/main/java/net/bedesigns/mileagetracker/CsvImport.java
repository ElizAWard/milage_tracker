package net.bedesigns.mileagetracker;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.bedesigns.mileagetracker.model.ReceiptItem;

/**
 * Reads back what {@link CsvExport} writes: {@code date,gallons,odometer} rows that become
 * fill-ups. Parsing is pure string work so it can be unit-tested on the JVM; only
 * {@link #read(Context, Uri)} touches the file system.
 */
public final class CsvImport {

    /**
     * How Android classifies {@code .csv} files, offered to the picker so the right documents
     * show up without also listing every file on the device.
     */
    public static final String[] MIME_TYPES = {
            "text/csv", "text/comma-separated-values", "text/plain"
    };

    /** App-lifetime single thread, for the same reason as {@link CsvExport#exportExecutor}. */
    public static final ExecutorService importExecutor = Executors.newSingleThreadExecutor();

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final int COLUMN_DATE = 0;
    private static final int COLUMN_GALLONS = 1;
    private static final int COLUMN_ODOMETER = 2;
    private static final int MIN_COLUMNS = 3;

    private CsvImport() {
    }

    /** Raised when the file is readable but its contents are not a usable fill-up list. */
    public static class CsvImportException extends Exception {

        /** 1-based line in the source file, or 0 when the problem is not tied to one line. */
        public final int lineNumber;

        CsvImportException(String message, int lineNumber) {
            super(lineNumber > 0 ? "line " + lineNumber + ": " + message : message);
            this.lineNumber = lineNumber;
        }
    }

    /**
     * Loads and parses a picked document. Must be run off the main thread, e.g. on
     * {@link #importExecutor}.
     */
    public static List<ReceiptItem> read(Context context, Uri uri)
            throws IOException, CsvImportException {
        StringBuilder content = new StringBuilder();

        try (InputStream input = context.getContentResolver().openInputStream(uri)) {
            if (input == null) {
                throw new IOException("Could not open " + uri);
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
        }

        return parse(content.toString());
    }

    /**
     * Turns CSV text into fill-ups, chaining odometer readings so each row becomes the
     * "previous mileage" of the one after it - which is what gives every row but the first an
     * MPG value.
     *
     * <p>Rows are chained in <b>ascending odometer order</b>, not file order: our own export is
     * written newest-first, so following file order would chain each row to a <em>higher</em>
     * reading than itself and no MPG would ever be computed. Sorting also makes the result
     * independent of how the file happened to be written.
     *
     * @throws CsvImportException with the offending line number when a row cannot be read, or
     *                             when two rows share an odometer reading (they could not both
     *                             exist in the database, which keys on it).
     */
    public static List<ReceiptItem> parse(String csv) throws CsvImportException {
        // A file saved by Excel often starts with a byte order mark, which would otherwise
        // corrupt the very first header/date cell.
        String content = csv.startsWith("\uFEFF") ? csv.substring(1) : csv;

        List<ParsedRow> rows = new ArrayList<>();
        String[] lines = content.split("\r\n|\n|\r", -1);
        boolean checkedHeader = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            int lineNumber = i + 1;
            List<String> columns = splitLine(line);

            // Only the very first row can be the header; everything else named "date" is data.
            if (!checkedHeader) {
                checkedHeader = true;
                if (!columns.isEmpty() && columns.get(COLUMN_DATE).equalsIgnoreCase("date")) {
                    continue;
                }
            }

            if (columns.size() < MIN_COLUMNS) {
                throw new CsvImportException(
                        "expected date, gallons, odometer", lineNumber);
            }

            rows.add(new ParsedRow(
                    parseDate(columns.get(COLUMN_DATE), lineNumber),
                    parseGallons(columns.get(COLUMN_GALLONS), lineNumber),
                    parseOdometer(columns.get(COLUMN_ODOMETER), lineNumber),
                    lineNumber));
        }

        rows.sort(Comparator.comparingInt((ParsedRow row) -> row.odometer));
        rejectDuplicateOdometers(rows);

        List<ReceiptItem> items = new ArrayList<>(rows.size());
        Integer previousMileage = null;

        for (ParsedRow row : rows) {
            // The first (oldest) fill-up has nothing before it, so its MPG stays unknown - the
            // same rule the app follows when fill-ups are entered one at a time.
            items.add(new ReceiptItem(row.date, row.gallons, row.odometer, previousMileage));
            previousMileage = row.odometer;
        }

        return items;
    }

    private static void rejectDuplicateOdometers(List<ParsedRow> rows) throws CsvImportException {
        for (int i = 1; i < rows.size(); i++) {
            if (rows.get(i).odometer == rows.get(i - 1).odometer) {
                throw new CsvImportException(
                        "odometer reading " + rows.get(i).odometer + " appears more than once",
                        rows.get(i).lineNumber);
            }
        }
    }

    private static Date parseDate(String value, int lineNumber) throws CsvImportException {
        String text = value.trim();
        DateFormat format = new SimpleDateFormat(DATE_PATTERN, Locale.US);
        format.setLenient(false);

        // ParsePosition rather than parse(String): the plain overload silently accepts trailing
        // text such as "2026-10-27extra".
        ParsePosition position = new ParsePosition(0);
        Date date = format.parse(text, position);

        if (date == null || position.getIndex() != text.length()) {
            throw new CsvImportException("'" + value + "' is not a " + DATE_PATTERN + " date",
                    lineNumber);
        }

        return date;
    }

    private static double parseGallons(String value, int lineNumber) throws CsvImportException {
        double gallons;

        try {
            gallons = Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new CsvImportException("'" + value + "' is not a gallons amount", lineNumber);
        }

        if (Double.isNaN(gallons) || Double.isInfinite(gallons) || gallons <= 0) {
            throw new CsvImportException("'" + value + "' is not a positive gallons amount",
                    lineNumber);
        }

        return gallons;
    }

    private static int parseOdometer(String value, int lineNumber) throws CsvImportException {
        int odometer;

        try {
            odometer = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new CsvImportException("'" + value + "' is not an odometer reading", lineNumber);
        }

        if (odometer < 0) {
            throw new CsvImportException("'" + value + "' is not a positive odometer reading",
                    lineNumber);
        }

        return odometer;
    }

    /**
     * Splits one CSV line into cells, honouring double quotes so a spreadsheet's quoting (and
     * any comma inside a quoted cell) survives. Embedded line breaks are not supported - both
     * this app's exports and its parser work line by line.
     */
    private static List<String> splitLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);

            if (quoted) {
                if (character == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    current.append(character);
                }
            } else if (character == '"') {
                quoted = true;
            } else if (character == ',') {
                columns.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }

        columns.add(current.toString());
        return columns;
    }

    /** One input line, before it is chained into a {@link ReceiptItem}. */
    private static class ParsedRow {
        final Date date;
        final double gallons;
        final int odometer;
        final int lineNumber;

        ParsedRow(Date date, double gallons, int odometer, int lineNumber) {
            this.date = date;
            this.gallons = gallons;
            this.odometer = odometer;
            this.lineNumber = lineNumber;
        }
    }
}
