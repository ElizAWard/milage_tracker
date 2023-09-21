package net.bedesigns.mileagetracker.room;

// Source: https://developer.android.com/codelabs/android-room-with-a-view?index=..%2F..index#7

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ReceiptItemEntity.class}, version = 1)
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
                            .build();
                }
            }
        }

        return Instance;
    }
}
