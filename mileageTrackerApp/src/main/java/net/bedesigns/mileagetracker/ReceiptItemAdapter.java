package net.bedesigns.mileagetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.bedesigns.mileagetracker.model.ReceiptItem;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ReceiptItemAdapter extends RecyclerView.Adapter<ReceiptItemAdapter.ReceiptItemViewHolder>{

    /** Displayed when MPG cannot be calculated, e.g. for the very first fill-up of a vehicle. */
    private static final String NO_MPG = "\u2014";

    /** Reports the number of ticked rows whenever it changes, so the host can show a live count. */
    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    private final List<ReceiptItem> items = new ArrayList<>();

    /**
     * Rows ticked while in Delete mode, keyed by odometer reading: that is part of the row's
     * primary key, so a selection stays attached to the right entry even if the list re-orders
     * or refreshes underneath it.
     */
    private final Set<Integer> selectedMileages = new LinkedHashSet<>();

    private boolean selectionMode;
    private OnSelectionChangedListener onSelectionChangedListener;

    /**
     * How a receipt date reads everywhere it is shown (the list and the add dialog), so an entry
     * picked as an old date and one created today are rendered identically.
     */
    public static String formatDate(Date date) {
        return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
    }

    public ReceiptItemAdapter(List<ReceiptItem> items) {
        this.items.addAll(items);
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.onSelectionChangedListener = listener;
    }

    /**
     * Replaces the displayed rows with the given ones. The list is normally sourced straight
     * from Room, so the adapter never has to track additions of its own. Ticks on rows that no
     * longer exist are dropped so the reported count never disagrees with the screen.
     */
    public void setReceipts(List<ReceiptItem> receipts) {
        items.clear();
        items.addAll(receipts);
        pruneSelection();
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    /**
     * Turns the tick boxes on or off. Leaving the mode clears whatever was ticked, so the next
     * time Delete is chosen the user starts from a clean slate.
     */
    public void setSelectionMode(boolean enabled) {
        if (selectionMode == enabled) {
            return;
        }

        selectionMode = enabled;
        if (!enabled) {
            selectedMileages.clear();
        }

        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    /**
     * @return The ticked rows, in the order they appear on screen.
     */
    @NonNull
    public List<ReceiptItem> getSelectedReceipts() {
        List<ReceiptItem> selected = new ArrayList<>();

        for (ReceiptItem item : items) {
            if (selectedMileages.contains(item.getCurrentMileage())) {
                selected.add(item);
            }
        }

        return selected;
    }

    /** Ticks an unticked row, or unticks a ticked one, and tells the host the new total. */
    private void toggleSelection(@NonNull ReceiptItem item) {
        Integer currentMileage = item.getCurrentMileage();

        if (!selectedMileages.remove(currentMileage)) {
            selectedMileages.add(currentMileage);
        }

        int position = items.indexOf(item);
        if (position >= 0) {
            notifyItemChanged(position);
        }
        notifySelectionChanged();
    }

    /** Forgets ticks whose rows have since disappeared (e.g. they were just deleted). */
    private void pruneSelection() {
        if (selectedMileages.isEmpty()) {
            return;
        }

        Set<Integer> remaining = new LinkedHashSet<>();
        for (ReceiptItem item : items) {
            if (selectedMileages.contains(item.getCurrentMileage())) {
                remaining.add(item.getCurrentMileage());
            }
        }
        selectedMileages.retainAll(remaining);
    }

    private void notifySelectionChanged() {
        if (onSelectionChangedListener != null) {
            onSelectionChangedListener.onSelectionChanged(getSelectedReceipts().size());
        }
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
        ReceiptItem item = this.items.get(position);
        holder.bind(item, selectionMode, selectedMileages.contains(item.getCurrentMileage()));
        holder.itemView.setOnClickListener(v -> toggleSelection(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ReceiptItemViewHolder extends RecyclerView.ViewHolder {

        TextView dateDisplay;
        TextView gallonsDisplay;
        TextView odometerDisplay;
        TextView averageMileageDisplay;
        CheckBox checkBox;

        public ReceiptItemViewHolder(@NonNull View itemView) {
            super(itemView);

            dateDisplay = itemView.findViewById(R.id.date_display);
            gallonsDisplay = itemView.findViewById(R.id.gallons_display);
            odometerDisplay = itemView.findViewById(R.id.odometer_display);
            averageMileageDisplay = itemView.findViewById(R.id.average_mileage_display);
            checkBox = itemView.findViewById(R.id.row_checkbox);
        }

        public void bind(ReceiptItem receiptItem, boolean inSelectionMode, boolean isSelected) {
            dateDisplay.setText(formatDate(receiptItem.getDate()));
            gallonsDisplay.setText(receiptItem.getGallonsPurchased().toString());
            odometerDisplay.setText(receiptItem.getCurrentMileage().toString());

            Double averageMpg = receiptItem.getAverageMpg();
            averageMileageDisplay.setText(averageMpg == null
                    ? NO_MPG
                    : String.format(Locale.getDefault(), "%.2f", averageMpg));

            // Ticking is driven by the row as a whole, so the box stays a pure display of state.
            checkBox.setVisibility(inSelectionMode ? View.VISIBLE : View.GONE);
            checkBox.setChecked(isSelected);

            itemView.setBackgroundColor(isSelected
                    ? itemView.getContext().getColor(R.color.selected_row_background)
                    : android.graphics.Color.TRANSPARENT);
            itemView.setClickable(inSelectionMode);
        }
    }
}
