package net.bedesigns.mileagetracker;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.bedesigns.mileagetracker.model.ReceiptItem;

/**
 * Renders fill-ups as CSV and writes them somewhere a {@code FileProvider} can expose them to a
 * share sheet. Split from the UI so the format itself is unit-testable without a device.
 */
public final class CsvExport {

    /**
     * ISO day: sorts like a date should, and - unlike the locale-aware display format - never
     * contains a comma, so the columns need no quoting.
     */
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    /** Column order is the contract with whatever opens the file, so it lives in one place. */
    static final String HEADER = "date,gallons,odometer";

    /** Overwritten on every export: it is only ever a staging file for the share sheet. */
    static final String FILE_NAME = "mileage_tracker_export.csv";

    /** Folder under the app's private files dir, mirrored by res/xml/file_paths.xml. */
    static final String EXPORT_DIR = "exports";

    /** RFC 4180 line ending, so spreadsheet apps read the rows back correctly. */
    private static final String LINE_SEPARATOR = "\r\n";

    /**
     * Runs the file write off the main thread. App-lifetime rather than one-per-activity, so
     * rotating the screen a few times does not stack up idle threads - the same reasoning as
     * {@code ReceiptItemDatabase.databaseWriteExecutor}.
     */
    public static final ExecutorService exportExecutor = Executors.newSingleThreadExecutor();

    private CsvExport() {
    }

    /**
     * @param items Fill-ups in display order (newest first).
     * @return The whole file as text: one header line, then one line per fill-up. Empty input
     *         still produces the header, so the columns are never ambiguous.
     */
    public static String toCsv(List<ReceiptItem> items) {
        StringBuilder csv = new StringBuilder(HEADER).append(LINE_SEPARATOR);
        DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);

        for (ReceiptItem item : items) {
            csv.append(dateFormat.format(item.getDate()))
                    .append(',').append(item.getGallonsPurchased())
                    .append(',').append(item.getCurrentMileage())
                    .append(LINE_SEPARATOR);
        }

        return csv.toString();
    }

    /**
     * Writes {@link #toCsv(List)} to the app's private exports folder, replacing any previous
     * export. Must be run off the main thread, e.g. via {@link #exportExecutor}.
     *
     * @return The written file, ready to be handed to a {@code FileProvider}.
     * @throws IOException When the folder cannot be created or the file cannot be written.
     */
    public static File write(Context context, List<ReceiptItem> items) throws IOException {
        File exportsDir = new File(context.getFilesDir(), EXPORT_DIR);
        if (!exportsDir.isDirectory() && !exportsDir.mkdirs()) {
            throw new IOException("Could not create export folder: " + exportsDir);
        }

        File file = new File(exportsDir, FILE_NAME);
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(toCsv(items));
        }

        return file;
    }
}
