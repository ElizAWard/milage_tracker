package net.bedesigns.mileagetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.bedesigns.mileagetracker.model.ReceiptItem;

import java.util.ArrayList;
import java.util.List;

public class ReceiptItemAdapter extends RecyclerView.Adapter<ReceiptItemAdapter.ReceiptItemViewHolder>{

    private List<ReceiptItem> items = new ArrayList<>();

    public ReceiptItemAdapter(List<ReceiptItem> items) {
        this.items.addAll(items);
    }

    @NonNull
    @Override
    public ReceiptItemAdapter.ReceiptItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemLayout = inflater.inflate(R.layout.receipt_item, parent, false);
        return new ReceiptItemViewHolder(itemLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiptItemViewHolder holder, int position) {
        holder.bind(this.items.get(position));
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    public void addReceipt(ReceiptItem item) {
        this.items.add(item);
        notifyDataSetChanged();
    }

    static class ReceiptItemViewHolder extends RecyclerView.ViewHolder {

        TextView dateDisplay;
        TextView milesDisplay;
        TextView averageMileageDisplay;

        public ReceiptItemViewHolder(@NonNull View itemView) {
            super(itemView);
            dateDisplay = itemView.findViewById(R.id.date_display);
            milesDisplay = itemView.findViewById(R.id.miles_display);
            averageMileageDisplay = itemView.findViewById(R.id.average_mileage_display);
        }

        public void bind(ReceiptItem receiptItem) {
            dateDisplay.setText(receiptItem.getDate().toString());
            milesDisplay.setText(receiptItem.getCurrentMileage());
            // TODO calculate actual average
            averageMileageDisplay.setText("12");
        }
    }
}
