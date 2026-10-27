package net.bedesigns.mileagetracker;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.bedesigns.mileagetracker.model.ReceiptItem;

/**
 * Saves an export into the device's public Downloads folder, where the Files/Downloads apps can
 * see it. Two paths because scoped storage split the platform in two:
 * <ul>
 *     <li>API 29+ - {@link MediaStore.Downloads}, no permission required.</li>
 *     <li>API 24-28 - a direct write into the shared folder, which needs
 *         {@code WRITE_EXTERNAL_STORAGE} to be granted first (see
 *         {@link #requiresStoragePermission()}).</li>
 * </ul>
 */
public final class CsvDownload {

    static final String MIME_TYPE = "text/csv";

    /** Stamped into every file name so repeated downloads never collide or overwrite each other. */
    static final String NAME_PREFIX = "mileage_tracker_export_";
    static final String NAME_PATTERN = "yyyyMMdd_HHmmss";

    private CsvDownload() {
    }

    /**
     * @return Whether this device needs {@code WRITE_EXTERNAL_STORAGE} before saving. False on
     *         scoped-storage devices, where MediaStore needs no permission at all.
     */
    public static boolean requiresStoragePermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q;
    }

    /**
     * Writes the rows to Downloads under a timestamped name. Must run off the main thread, e.g.
     * on {@link CsvExport#exportExecutor}.
     *
     * @return The file name the CSV was saved as, for the confirmation toast.
     * @throws IOException When Downloads cannot be written to.
     */
    public static String saveToDownloads(Context context, List<ReceiptItem> items)
            throws IOException {
        String fileName = buildFileName();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveWithMediaStore(context, items, fileName);
        } else {
            saveToPublicDownloads(items, fileName);
        }

        return fileName;
    }

    /** Distinct down to the second, so an export is never silently replaced by a later one. */
    static String buildFileName() {
        DateFormat stamp = new SimpleDateFormat(NAME_PATTERN, Locale.US);
        return NAME_PREFIX + stamp.format(new Date()) + ".csv";
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private static void saveWithMediaStore(Context context, List<ReceiptItem> items, String fileName)
            throws IOException {
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, MIME_TYPE);
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        // Claim it while it is half-written so it does not show up as a finished, empty file.
        values.put(MediaStore.Downloads.IS_PENDING, 1);

        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            throw new IOException("Could not create " + fileName + " in Downloads");
        }

        try {
            OutputStream output = resolver.openOutputStream(uri);
            if (output == null) {
                throw new IOException("Could not open " + uri + " for writing");
            }

            try (OutputStream stream = output) {
                writeCsv(stream, items);
            }
        } catch (IOException | RuntimeException e) {
            // Leave no orphaned "pending" entry behind when the write fails.
            resolver.delete(uri, null, null);
            throw e;
        }

        values.clear();
        values.put(MediaStore.Downloads.IS_PENDING, 0);
        resolver.update(uri, values, null, null);
    }

    @SuppressWarnings("deprecation")
    private static void saveToPublicDownloads(List<ReceiptItem> items, String fileName)
            throws IOException {
        File downloads = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);

        if (!downloads.isDirectory() && !downloads.mkdirs()) {
            throw new IOException("Could not create " + downloads);
        }

        try (OutputStream stream = new FileOutputStream(new File(downloads, fileName))) {
            writeCsv(stream, items);
        }
    }

    private static void writeCsv(OutputStream stream, List<ReceiptItem> items) throws IOException {
        stream.write(CsvExport.toCsv(items).getBytes(StandardCharsets.UTF_8));
    }
}
