package net.bedesigns.mileagetracker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class MileagePagerAdapter extends PagerAdapter {

    private Context context;

    public MileagePagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return String.format("Page %s", position + 1);
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        TextView newTextView = new TextView(context);
        newTextView.setText(getPageTitle(position));
        newTextView.setTextColor(context.getResources().getColor(android.R.color.holo_purple));

        return newTextView;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }
}
