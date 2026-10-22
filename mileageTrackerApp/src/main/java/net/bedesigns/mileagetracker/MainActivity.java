package net.bedesigns.mileagetracker;

import static android.widget.Toast.LENGTH_SHORT;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import net.bedesigns.mileagetracker.model.ReceiptItem;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private FillUpViewModel viewModel;
    private ReceiptItemAdapter receiptAdapter;
    /**
     * Odometer reading of the most recent fill-up, used as the "previous" reading for the next
     * one. Restored from the database on start, so MPG stays correct across restarts.
     */
    private Integer previousMileage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(FillUpViewModel.class);

        // TODO fab could be an exploding fab and allow for adding vehicles
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                View addItemView = layoutInflater.inflate(R.layout.add_item_layout, null);
                TextInputLayout currentMileageTextview = addItemView.findViewById(R.id.item_current_mileage);
                TextInputLayout gallonsTextView = addItemView.findViewById(R.id.item_gallons);

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

        // The list is driven entirely by the database: whatever Room has saved is what renders,
        // both on a fresh start and after every add.
        viewModel.getFillUps().observe(this, entities -> {
            List<ReceiptItem> items = FillUpViewModel.toModel(entities);
            receiptAdapter.setReceipts(items);

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

        ReceiptItem receiptItem = new ReceiptItem(Calendar.getInstance().getTime(),
                gallonsPurchased, currentMileage, previousMileage);

        viewModel.addReceipt(receiptItem);
        previousMileage = currentMileage;

        Toast.makeText(this, R.string.fill_up_added, LENGTH_SHORT).show();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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
