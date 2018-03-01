package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.HistoryItem;
import openfoodfacts.github.scrachx.openfood.views.holders.HistoryScanHolder;

public class HistoryListAdapter extends RecyclerView.Adapter<HistoryScanHolder> {

    private final List<HistoryItem> list;
    private final String productUrl;
    private Activity mActivity;

    public HistoryListAdapter(List<HistoryItem> list, String productUrl, Activity activity) {
        this.list = list == null ? Collections.<HistoryItem>emptyList() : list;
        this.productUrl = productUrl;
        this.mActivity = activity;
    }

    @Override
    public HistoryScanHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_list_item, parent, false);
        return new HistoryScanHolder(v, productUrl, mActivity);
    }

    @Override
    public void onBindViewHolder(HistoryScanHolder holder, int position) {

        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        holder.txtTitle.setText(list.get(position).getTitle());
        holder.txtBarcode.setText(list.get(position).getBarcode());
        holder.txtBrands.setText(list.get(position).getBrands());
        holder.imgProduct.setImageBitmap(list.get(position).getUrl());

        Date date = list.get(position).getTime();
        calcTime(date, holder);

        //animate(holder);
    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Insert a new item to the RecyclerView on a predefined position
    public void insert(int position, HistoryItem data) {
        list.add(position, data);
        notifyItemInserted(position);
    }

    // Remove a RecyclerView item containing a specified Data object
    public void remove(HistoryItem data) {
        int position = list.indexOf(data);
        list.remove(position);
        notifyItemRemoved(position);
    }


    public void calcTime(Date date, HistoryScanHolder holder) {


        Date now = new Date();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - date.getTime());
        long minutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - date.getTime());
        long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - date.getTime());
        long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - date.getTime());


        String secText = String.valueOf(seconds) + " seconds ago";
        String minText = String.valueOf(minutes) + " minutes ago";
        String hourText = String.valueOf(hours) + " hours ago";
        String dayText = String.valueOf(days) + " days ago";

        if (seconds < 60) {
            holder.txtDate.setText(secText);
        } else if (minutes < 60) {
            holder.txtDate.setText(minText);
        } else if (hours < 24) {
            holder.txtDate.setText(hourText);
        } else {
            holder.txtDate.setText(dayText);
        }
    }


}
