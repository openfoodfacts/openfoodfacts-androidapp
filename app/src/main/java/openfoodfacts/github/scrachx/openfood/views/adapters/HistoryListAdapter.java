package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

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

}
