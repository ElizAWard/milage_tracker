package net.bedesigns.mileagetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ReceiptItemAdapter extends RecyclerView.Adapter<ReceiptItemAdapter.ReceiptItemViewHolder>{

    @NonNull
    @Override
    public ReceiptItemAdapter.ReceiptItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemLayout = inflater.inflate(R.layout.receipt_item, parent, false);
        return new ReceiptItemViewHolder(itemLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiptItemViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
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
    }
}
