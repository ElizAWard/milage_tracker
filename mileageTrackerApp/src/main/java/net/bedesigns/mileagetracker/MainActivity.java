package net.bedesigns.mileagetracker;

import static android.widget.Toast.LENGTH_SHORT;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import net.bedesigns.mileagetracker.model.IReceiptItemCollection;
import net.bedesigns.mileagetracker.model.ReceiptItem;
import net.bedesigns.mileagetracker.room.ReceiptItemRoom;
import net.bedesigns.mileagetracker.room.ReceiptItemDao;
import net.bedesigns.mileagetracker.room.ReceiptItemDatabase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private IReceiptItemCollection fillUpLists;
    private static RecyclerView entriesRecyclerView;
    private static ReceiptItemAdapter receiptAdapter;
    private static int previousMileage = 0;

    private static final ItemAddedCallback receiptItemAdded = new ItemAddedCallback() {
        @Override
        public void onItemAdded(Date receiptDate, String currentMileageStr, String gallonsPurchasedStr) {
            int currentMileage = getIntFromString(currentMileageStr);
            double gallonsPurchased = getDoubleFromString(gallonsPurchasedStr);

            ReceiptItem receiptItem = new ReceiptItem(Calendar.getInstance().getTime(),
                    gallonsPurchased, currentMileage, previousMileage);

            if (receiptAdapter != null) {
                receiptAdapter.addReceipt(receiptItem);
            }

            Log.d(TAG, String.format("Receipt created: %s", receiptItem));
            previousMileage = currentMileage;

            receiptAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        fillUpLists = new ReceiptItemRoom(createReceiptItemDao());

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
                        .setPositiveButton(R.string.add, (dialogInterface, i) -> {
                            receiptItemAdded.onItemAdded(Calendar.getInstance().getTime(),
                                    currentMileageTextview.getEditText().getText().toString(),
                                    gallonsTextView.getEditText().getText().toString());

                            Toast.makeText(MainActivity.this, "Fill Up Added", LENGTH_SHORT).show();
                        })
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        });
                alertBuilder.show();
            }
        });

        entriesRecyclerView = findViewById(R.id.entries_recyclerview);
        entriesRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this,
                RecyclerView.VERTICAL, true));
        // TODO get receipts from whatever way we are saving it
        receiptAdapter = new ReceiptItemAdapter(new ArrayList<>());
        entriesRecyclerView.setAdapter(receiptAdapter);
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

    private ReceiptItemDao createReceiptItemDao() {
        ReceiptItemDatabase database = ReceiptItemDatabase.getDatabase(this);
        return database.receiptItemDao();
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

    private static interface ItemAddedCallback {
        void onItemAdded(Date receiptDate, String currentMileage, String gallonsPurchased);
    }
}