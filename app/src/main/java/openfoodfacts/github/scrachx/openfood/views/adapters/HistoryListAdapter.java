package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.app.Activity;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.HistoryItem;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.YourListedProducts;
import openfoodfacts.github.scrachx.openfood.views.holders.HistoryScanHolder;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HistoryListAdapter extends RecyclerView.Adapter<HistoryScanHolder> {
    private final List<HistoryItem> list;
    private Activity mActivity;
    private Resources res;
    private boolean isLowBatteryMode;

    public HistoryListAdapter(List<HistoryItem> list, Activity activity, boolean isLowBatteryMode) {
        this.list = list == null ? Collections.emptyList() : list;
        this.mActivity = activity;
        res = activity.getResources();
        this.isLowBatteryMode = isLowBatteryMode;
    }

    @Override
    public HistoryScanHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_list_item, parent, false);
        return new HistoryScanHolder(v, mActivity);
    }

    @Override
    public void onBindViewHolder(HistoryScanHolder holder, int position) {
        holder.historyImageProgressbar.setVisibility(View.VISIBLE);
        HistoryItem item = list.get(position);

        String productBrandsQuantityDetails = YourListedProducts.getProductBrandsQuantityDetails(item);

        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        holder.txtTitle.setText(item.getTitle());
        holder.txtBarcode.setText(item.getBarcode());
        holder.txtProductDetails.setText(productBrandsQuantityDetails);
        if (BuildConfig.FLAVOR.equals("opf") || BuildConfig.FLAVOR.equals("opff") || BuildConfig.FLAVOR.equals("obf")) {
            holder.imgNutritionGrade.setVisibility(View.GONE);
        }
        if (Utils.getSmallImageGrade(item.getNutritionGrade()) != 0) {
            holder.imgNutritionGrade.setImageDrawable(ContextCompat.getDrawable(mActivity, Utils.getSmallImageGrade(item.getNutritionGrade())));
        } else {
            holder.imgNutritionGrade.setVisibility(View.INVISIBLE);
        }
        if (item.getUrl() == null) {
            holder.historyImageProgressbar.setVisibility(View.GONE);
        }

        // Load Image if isBatteryLoad is false
        if (!isLowBatteryMode) {
            Picasso.get()
                .load(item.getUrl())
                .placeholder(R.drawable.placeholder_thumb)
                .error(R.drawable.ic_no_red_24dp)
                .fit()
                .centerCrop()
                .into(holder.imgProduct, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.historyImageProgressbar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception ex) {
                        holder.historyImageProgressbar.setVisibility(View.GONE);
                    }
                });
        } else {
            holder.imgProduct.setBackground(mActivity.getResources().getDrawable(R.drawable.placeholder_thumb));
            holder.historyImageProgressbar.setVisibility(View.INVISIBLE);
        }

        Date date = list.get(position).getTime();
        calcTime(date, holder);
    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return list.size();
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

    private void calcTime(Date date, HistoryScanHolder holder) {

        Date now = new Date();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - date.getTime());
        long minutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - date.getTime());
        long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - date.getTime());
        long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - date.getTime());

        if (seconds < 60) {
            holder.txtDate.setText(res.getQuantityString(R.plurals.seconds, (int) seconds, (int) seconds));
        } else if (minutes < 60) {
            holder.txtDate.setText(res.getQuantityString(R.plurals.minutes, (int) minutes, (int) minutes));
        } else if (hours < 24) {
            holder.txtDate.setText(res.getQuantityString(R.plurals.hours, (int) hours, (int) hours));
        } else {
            holder.txtDate.setText(res.getQuantityString(R.plurals.days, (int) days, (int) days));
        }
    }
}
