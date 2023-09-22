package net.bedesigns.mileagetracker;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import net.bedesigns.mileagetracker.model.ReceiptItem;

import java.util.ArrayList;
import java.util.List;

public class MileagePagerAdapter extends PagerAdapter {

    private static final String TAG = MileagePagerAdapter.class.getSimpleName();

    private Context context;
    private EntriesPage entriesPage;
    private List<ReceiptItem> receipts = new ArrayList<>();

    public MileagePagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String pageTitle;
        if (position == 0) {
            pageTitle = "Entries";
        } else {
            pageTitle = "Graphs";
        }

        return pageTitle;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        if (position == 0) {
            Log.d(TAG, "Instantiate index 0");
            entriesPage = new EntriesPage(collection.getContext());
            collection.addView(entriesPage);
            return entriesPage;
        }

        Log.d(TAG, String.format("Instantiate index: %s", position));
        TextView newTextView = new TextView(context);
        newTextView.setText(getPageTitle(position));
        newTextView.setTextColor(context.getResources().getColor(android.R.color.holo_purple));

        return newTextView;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    public void addReceipt(ReceiptItem receiptItem) {
        if (entriesPage != null) {
            entriesPage.addReceipt(receiptItem);
        }
    }
}
