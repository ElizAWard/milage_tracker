package net.bedesigns.mileagetracker;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.bedesigns.mileagetracker.model.ReceiptItem;
import net.bedesigns.mileagetracker.room.ReceiptItemDao;
import net.bedesigns.mileagetracker.room.ReceiptItemDatabase;
import net.bedesigns.mileagetracker.room.ReceiptItemEntity;

/**
 * Owns the Room-backed collection of fill-ups and exposes it to the UI as {@link LiveData}.
 * <p>
 * Every read comes from the database rather than an in-memory list, so entries survive an app
 * restart. Room re-emits {@link #getFillUps()} automatically after every insert or delete.
 */
public class FillUpViewModel extends AndroidViewModel {

    /**
     * There is no vehicle management UI yet, but {@link ReceiptItemEntity} keys on
     * {@code vehicleName}, so everything is filed under this single placeholder vehicle.
     */
    public static final String DEFAULT_VEHICLE = "My Vehicle";

    private final ReceiptItemDao dao;
    private final LiveData<List<ReceiptItemEntity>> fillUps;

    public FillUpViewModel(@NonNull Application application) {
        super(application);

        ReceiptItemDatabase database = ReceiptItemDatabase.getDatabase(application);
        dao = database.receiptItemDao();
        fillUps = dao.observeFillUpsForVehicle(DEFAULT_VEHICLE);
    }

    /**
     * @return The saved fill-ups for {@link #DEFAULT_VEHICLE}, newest first. Emits the current
     *         contents on subscribe and again on every subsequent change.
     */
    @NonNull
    public LiveData<List<ReceiptItemEntity>> getFillUps() {
        return fillUps;
    }

    /**
     * Saves a fill-up. The write happens on a background thread; observe {@link #getFillUps()}
     * rather than assuming the row is committed when this returns.
     */
    public void addReceipt(@NonNull ReceiptItem receipt) {
        ReceiptItemDatabase.databaseWriteExecutor.execute(() ->
                dao.insertOrUpdateReceiptItems(Collections.singletonList(toEntity(receipt))));
    }

    public void deleteReceipt(@NonNull ReceiptItem receipt) {
        ReceiptItemDatabase.databaseWriteExecutor.execute(() ->
                dao.deleteReceiptItem(toEntity(receipt)));
    }

    /**
     * Removes several fill-ups at once (the multi-select Delete flow). Room keys each row by
     * vehicle + odometer reading, so matching entities are all it needs to drop them.
     */
    public void deleteReceipts(@NonNull List<ReceiptItem> receipts) {
        List<ReceiptItemEntity> entities = toEntities(receipts);

        ReceiptItemDatabase.databaseWriteExecutor.execute(() -> dao.deleteReceiptItem(entities));
    }

    /**
     * Removes every fill-up for {@link #DEFAULT_VEHICLE} (the "Delete All" menu action), once
     * the caller has confirmed it with the user. The observed list re-emits when it lands.
     */
    public void deleteAllReceipts() {
        ReceiptItemDatabase.databaseWriteExecutor.execute(() ->
                dao.deleteAllForVehicle(DEFAULT_VEHICLE));
    }

    /**
     * Saves several fill-ups at once (the import flow), replacing any existing row that shares
     * an odometer reading, since that reading is part of the primary key.
     */
    public void importReceipts(@NonNull List<ReceiptItem> receipts) {
        ReceiptItemDatabase.databaseWriteExecutor.execute(() ->
                dao.insertOrUpdateReceiptItems(toEntities(receipts)));
    }

    private static ReceiptItemEntity toEntity(ReceiptItem receipt) {
        return new ReceiptItemEntity(DEFAULT_VEHICLE, receipt.getDate(), receipt.getGallonsPurchased(),
                receipt.getCurrentMileage(), receipt.getPreviousMileage());
    }

    private static List<ReceiptItemEntity> toEntities(List<ReceiptItem> receipts) {
        List<ReceiptItemEntity> entities = new ArrayList<>(receipts.size());

        for (ReceiptItem receipt : receipts) {
            entities.add(toEntity(receipt));
        }

        return entities;
    }

    public static List<ReceiptItem> toModel(List<ReceiptItemEntity> entities) {
        List<ReceiptItem> items = new ArrayList<>(entities.size());

        for (ReceiptItemEntity entity : entities) {
            items.add(new ReceiptItem(entity.date, entity.gallonsPurchased,
                    entity.currentMileage, entity.previousMileage));
        }

        return items;
    }
}
