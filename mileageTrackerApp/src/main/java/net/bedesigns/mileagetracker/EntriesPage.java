package net.bedesigns.mileagetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EntriesPage extends FrameLayout {

    private static final String TAG = EntriesPage.class.getSimpleName();

    private RecyclerView recyclerView;
    private ReceiptItemAdapter receiptAdapter;

    public EntriesPage(@NonNull Context context) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.entries_page_layout, null);
        recyclerView = rootView.findViewById(R.id.entries_recyclerview);
        // TODO get receipts from whatever way we are saving it
        receiptAdapter = new ReceiptItemAdapter(new ArrayList<>());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(rootView.getContext());
        recyclerView.setAdapter(receiptAdapter);

        addView(rootView);
    }

    public void addReceipt(ReceiptItem item) {
        if (receiptAdapter != null) {
            receiptAdapter.addReceipt(item);
        }
    }
}
