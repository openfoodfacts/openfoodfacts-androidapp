package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.RatingItem;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.views.holders.UserRatingsHolder;

public class RatingProductListAdapter extends RecyclerView.Adapter<UserRatingsHolder> {

    private OpenFoodAPIClient api;
    private final List<RatingItem> list;
    private Activity mActivity;

    private LayerDrawable ratingDrawable;

    public RatingProductListAdapter(List<RatingItem> list, Activity activity) {
        this.api = new OpenFoodAPIClient(activity);
        this.list = list == null ? Collections.<RatingItem>emptyList() : list;
        this.mActivity = activity;
    }

    @Override
    public UserRatingsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_rating_list_item, parent, false);
        return new UserRatingsHolder(v, mActivity);
    }

    @Override
    public void onBindViewHolder(UserRatingsHolder holder, int position) {
        ratingDrawable = (LayerDrawable) holder.vProductRating.getProgressDrawable();
        ratingDrawable.getDrawable(2).setColorFilter(ContextCompat.getColor(holder.vProductRating.getContext(), R.color.grey_700), PorterDuff.Mode.SRC_ATOP);

        holder.vProductImage.setImageBitmap(list.get(position).getImageUrl());
        holder.vProductRating.setRating(list.get(position).getStars());
        holder.vProductName.setText(list.get(position).getProductName());
        holder.vComment.setText(list.get(position).getComment());
        holder.mProductBarcode = list.get(position).getBarcode();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public List<RatingItem> getList(){
        return list;
    }

    public void insert(int position, RatingItem data) {
        list.add(position, data);
        notifyItemInserted(position);
    }

    public void remove(RatingItem data) {
        int position = list.indexOf(data);
        list.remove(position);
        notifyItemRemoved(position);
    }
}
