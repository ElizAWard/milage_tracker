package net.bedesigns.mileagetracker;

import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class CsvDownloadTest {

    @Test
    public void fileNames_carryATimestampSoDownloadsNeverOverwriteEachOther() {
        String name = CsvDownload.buildFileName();

        assertTrue(name, name.matches(
                CsvDownload.NAME_PREFIX + "\\d{8}_\\d{6}\\.csv"));
    }

    @Test
    public void fileNames_doNotClashWithTheShareSheetStagingFile() {
        // The share sheet reuses one fixed name; downloads must not fight it for the same file.
        assertNotEquals(CsvExport.FILE_NAME, CsvDownload.buildFileName());
    }
}
