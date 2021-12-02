package net.bedesigns.mileagetracker;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ReceiptItemAdapter extends RecyclerView.Adapter<ReceiptItemAdapter.ReceiptItemViewHolder>{

    @NonNull
    @Override
    public ReceiptItemAdapter.ReceiptItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiptItemViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class ReceiptItemViewHolder extends RecyclerView.ViewHolder {

        public ReceiptItemViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
