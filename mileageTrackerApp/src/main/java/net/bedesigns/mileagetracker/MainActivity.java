package net.bedesigns.mileagetracker;

import static android.widget.Toast.LENGTH_SHORT;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import net.bedesigns.mileagetracker.model.ReceiptItem;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private FillUpViewModel viewModel;
    private ReceiptItemAdapter receiptAdapter;
    private FloatingActionButton fab;
    /** Footer shown only in Delete mode: live tick count plus Cancel/Delete. */
    private View selectionBar;
    private TextView selectionCount;
    private View deleteSelectionButton;
    /**
     * Backing out of Delete mode (rather than the whole app) while it is active, so a stray back
     * press cannot silently discard a half-made selection.
     */
    private final OnBackPressedCallback selectionBackPress =
            new OnBackPressedCallback(false) {
                @Override
                public void handleOnBackPressed() {
                    exitSelectionMode();
                }
            };
    /**
     * Odometer reading of the most recent fill-up, used as the "previous" reading for the next
     * one. Restored from the database on start, so MPG stays correct across restarts.
     */
    private Integer previousMileage;
    /**
     * Date attached to the fill-up currently being entered. Defaulted to today every time the
     * dialog opens, and changed only if the user picks a different day from the date picker.
     * Time of day is always zeroed: a receipt only has a day, never an hour.
     */
    private Calendar receiptDate = Calendar.getInstance();
    /**
     * Mirror of what is on screen, kept by the LiveData observer so Export always has a
     * snapshot to write even if the database query is mid-refresh.
     */
    private final List<ReceiptItem> currentItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(FillUpViewModel.class);

        // TODO fab could be an exploding fab and allow for adding vehicles
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                View addItemView = layoutInflater.inflate(R.layout.add_item_layout, null);
                TextInputLayout currentMileageTextview = addItemView.findViewById(R.id.item_current_mileage);
                TextInputLayout gallonsTextView = addItemView.findViewById(R.id.item_gallons);

                // Each dialog opens dated today; the picker lets an older receipt be back-dated.
                receiptDate = Calendar.getInstance();
                truncateTime(receiptDate);
                EditText receiptDateView = addItemView.findViewById(R.id.item_receipt_date_value);
                receiptDateView.setText(ReceiptItemAdapter.formatDate(receiptDate.getTime()));
                receiptDateView.setOnClickListener(v -> showDatePicker(receiptDateView));

                alertBuilder.setTitle(R.string.add_new_fill_up)
                        .setView(addItemView)
                        .setCancelable(false)
                        .setPositiveButton(R.string.add, (dialogInterface, i) -> addFillUp(
                                currentMileageTextview.getEditText().getText().toString(),
                                gallonsTextView.getEditText().getText().toString()))
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());
                alertBuilder.show();
            }
        });

        RecyclerView entriesRecyclerView = findViewById(R.id.entries_recyclerview);
        // Newest first (the query orders by mileage DESC, index 0 = latest), so the list is laid
        // out top-down rather than reversed. reversed=true would push the newest row to the bottom.
        entriesRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this,
                RecyclerView.VERTICAL, false));
        receiptAdapter = new ReceiptItemAdapter(new ArrayList<>());
        entriesRecyclerView.setAdapter(receiptAdapter);

        selectionBar = findViewById(R.id.selection_bar);
        selectionCount = findViewById(R.id.selection_count);
        deleteSelectionButton = findViewById(R.id.delete_selection);
        findViewById(R.id.cancel_selection).setOnClickListener(v -> exitSelectionMode());
        deleteSelectionButton.setOnClickListener(v -> confirmDeleteSelection());
        receiptAdapter.setOnSelectionChangedListener(this::updateSelectionBar);
        getOnBackPressedDispatcher().addCallback(this, selectionBackPress);

        // The list is driven entirely by the database: whatever Room has saved is what renders,
        // both on a fresh start and after every add.
        viewModel.getFillUps().observe(this, entities -> {
            List<ReceiptItem> items = FillUpViewModel.toModel(entities);
            receiptAdapter.setReceipts(items);

            currentItems.clear();
            currentItems.addAll(items);

            // Query orders by mileage DESC, so index 0 is the most recent fill-up.
            previousMileage = items.isEmpty() ? null : items.get(0).getCurrentMileage();

            Log.d(TAG, String.format("Loaded %d saved fill-up(s)", items.size()));
        });
    }

    private void addFillUp(String currentMileageStr, String gallonsPurchasedStr) {
        Integer currentMileage = getIntFromString(currentMileageStr);
        Double gallonsPurchased = getDoubleFromString(gallonsPurchasedStr);

        if (currentMileage == null || currentMileage < 0 || gallonsPurchased == null || gallonsPurchased <= 0) {
            Toast.makeText(this, R.string.invalid_fill_up, Toast.LENGTH_LONG).show();
            return;
        }

        ReceiptItem receiptItem = new ReceiptItem(receiptDate.getTime(),
                gallonsPurchased, currentMileage, previousMileage);

        viewModel.addReceipt(receiptItem);
        previousMileage = currentMileage;

        Toast.makeText(this, R.string.fill_up_added, LENGTH_SHORT).show();
    }

    /**
     * Opens a day/month/year picker seeded with whatever date the field is currently showing
     * (today, unless the user has already back-dated this entry).
     */
    private void showDatePicker(EditText receiptDateView) {
        new DatePickerDialog(MainActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
            receiptDate.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
            receiptDate.set(Calendar.MILLISECOND, 0);
            receiptDateView.setText(ReceiptItemAdapter.formatDate(receiptDate.getTime()));
        }, receiptDate.get(Calendar.YEAR),
                receiptDate.get(Calendar.MONTH),
                receiptDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private static void truncateTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Switches the list into Delete mode: every row grows a tick box, the add button steps out
     * of the way, and the footer offers Cancel/Delete until the user backs out or commits.
     */
    private void enterSelectionMode() {
        receiptAdapter.setSelectionMode(true);
        selectionBar.setVisibility(View.VISIBLE);
        fab.hide();
        selectionBackPress.setEnabled(true);
        updateSelectionBar(0);
    }

    private void exitSelectionMode() {
        receiptAdapter.setSelectionMode(false);
        selectionBar.setVisibility(View.GONE);
        fab.show();
        selectionBackPress.setEnabled(false);
    }

    /** Keeps the footer honest: "3 selected", and Delete only live once something is ticked. */
    private void updateSelectionBar(int selectedCount) {
        selectionCount.setText(getString(R.string.fill_ups_selected, selectedCount));
        deleteSelectionButton.setEnabled(selectedCount > 0);
    }

    /** Asks for confirmation before dropping the ticked rows for good. */
    private void confirmDeleteSelection() {
        List<ReceiptItem> selected = receiptAdapter.getSelectedReceipts();
        if (selected.isEmpty()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_selected_title, selected.size()))
                .setMessage(R.string.delete_selected_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteReceipts(selected);
                    exitSelectionMode();
                    Toast.makeText(this, R.string.fill_ups_deleted, LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * "Delete All" wipes the whole list, so it gets the same confirmation step as the ticked
     * rows - one tap of a menu item must never destroy the data on its own.
     */
    private void confirmDeleteAll() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_all_title)
                .setMessage(R.string.delete_selected_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    // Leaving Delete mode first drops any ticks that are about to vanish anyway.
                    if (receiptAdapter.isSelectionMode()) {
                        exitSelectionMode();
                    }
                    viewModel.deleteAllReceipts();
                    Toast.makeText(this, R.string.all_fill_ups_deleted, LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Writes the current fill-ups to CSV on a background thread, then offers the file to
     * whatever app the user wants to send it to (Drive, Gmail, a file manager, ...).
     */
    private void exportCsv() {
        if (currentItems.isEmpty()) {
            Toast.makeText(this, R.string.nothing_to_export, LENGTH_SHORT).show();
            return;
        }

        // Snapshot now: the observer may swap the list out from under the background write.
        List<ReceiptItem> snapshot = new ArrayList<>(currentItems);

        CsvExport.exportExecutor.execute(() -> {
            try {
                File file = CsvExport.write(getApplicationContext(), snapshot);
                runOnUiThread(() -> shareCsv(file));
            } catch (IOException e) {
                Log.e(TAG, "CSV export failed", e);
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, R.string.export_failed, Toast.LENGTH_LONG).show());
            }
        });
    }

    /** Opens the share sheet for a freshly written export, granting read access to the target. */
    private void shareCsv(File file) {
        Uri csvUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND)
                .setType("text/csv")
                .putExtra(Intent.EXTRA_STREAM, csvUri)
                .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, getString(R.string.export)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_export) {
            exportCsv();
            return true;
        }

        // Enters (or, if already ticking rows, leaves) Delete mode.
        if (id == R.id.action_delete) {
            if (receiptAdapter.isSelectionMode()) {
                exitSelectionMode();
            } else {
                enterSelectionMode();
            }
            return true;
        }

        // Wipes the whole list, after asking first.
        if (id == R.id.action_delete_all) {
            confirmDeleteAll();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static Integer getIntFromString(String input) {
        if (input != null && !input.isEmpty()) {
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error converting to Integer", e);
            }
        }

        return null;
    }

    private static Double getDoubleFromString(String input) {
        if (input != null && !input.isEmpty()) {
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error converting to Double", e);
            }
        }

        return null;
    }
}
