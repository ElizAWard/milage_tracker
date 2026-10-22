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
import java.util.Locale;

public class ReceiptItemAdapter extends RecyclerView.Adapter<ReceiptItemAdapter.ReceiptItemViewHolder>{

    /** Displayed when MPG cannot be calculated, e.g. for the very first fill-up of a vehicle. */
    private static final String NO_MPG = "\u2014";

    private final List<ReceiptItem> items = new ArrayList<>();

    public ReceiptItemAdapter(List<ReceiptItem> items) {
        this.items.addAll(items);
    }

    /**
     * Replaces the displayed rows with the given ones. The list is normally sourced straight
     * from Room, so the adapter never has to track additions of its own.
     */
    public void setReceipts(List<ReceiptItem> receipts) {
        items.clear();
        items.addAll(receipts);
        notifyDataSetChanged();
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
        return items.size();
    }

    static class ReceiptItemViewHolder extends RecyclerView.ViewHolder {

        TextView dateDisplay;
        TextView gallonsDisplay;
        TextView odometerDisplay;
        TextView averageMileageDisplay;

        public ReceiptItemViewHolder(@NonNull View itemView) {
            super(itemView);

            dateDisplay = itemView.findViewById(R.id.date_display);
            gallonsDisplay = itemView.findViewById(R.id.gallons_display);
            odometerDisplay = itemView.findViewById(R.id.odometer_display);
            averageMileageDisplay = itemView.findViewById(R.id.average_mileage_display);
        }

        public void bind(ReceiptItem receiptItem) {
            dateDisplay.setText(receiptItem.getDate().toString());
            gallonsDisplay.setText(receiptItem.getGallonsPurchased().toString());
            odometerDisplay.setText(receiptItem.getCurrentMileage().toString());

            Double averageMpg = receiptItem.getAverageMpg();
            averageMileageDisplay.setText(averageMpg == null
                    ? NO_MPG
                    : String.format(Locale.getDefault(), "%.2f", averageMpg));
        }
    }
}
