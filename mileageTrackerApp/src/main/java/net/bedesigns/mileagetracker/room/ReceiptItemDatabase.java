package net.bedesigns.mileagetracker.room;

// Source: https://developer.android.com/codelabs/android-room-with-a-view?index=..%2F..index#7

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The schema changed: {@code previousMileage} is now nullable so that a vehicle's very first
 * fill-up can be stored without inventing a fake "previous" reading. Bumping the version lets
 * Room rebuild the file via the destructive fallback declared in {@link ReceiptItemDatabase}.
 */
@Database(entities = {ReceiptItemEntity.class}, version = 3)
@TypeConverters({Converters.class})
public abstract class ReceiptItemDatabase extends RoomDatabase {
    public abstract ReceiptItemDao receiptItemDao();

    private static volatile ReceiptItemDatabase Instance;
    private static final int NUMBER_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_THREADS);

    public static ReceiptItemDatabase getDatabase(final Context context) {
        if (Instance == null) {
            synchronized (ReceiptItemDatabase.class) {
                if (Instance == null) {
                    Instance = Room.databaseBuilder(context.getApplicationContext(),
                            ReceiptItemDatabase.class, "ReceiptItemDatabase")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }

        return Instance;
    }
}