package openfoodfacts.github.scrachx.openfood.views.product.summary;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

public class IngredientAnalysisTagsAdapter extends RecyclerView.Adapter<IngredientAnalysisTagsAdapter.ViewHolder> {
    private final WeakReference<Context> contextRef;
    private final LayoutInflater inflater;
    private final SharedPreferences prefs;
    private final List<AnalysisTagConfig> tags;
    private final List<AnalysisTagConfig> visibleTags = new ArrayList<>();
    private OnItemClickListener onClickListener;

    // data is passed into the constructor
    public IngredientAnalysisTagsAdapter(Context context, List<AnalysisTagConfig> tags) {
        contextRef = new WeakReference<>(context);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.inflater = LayoutInflater.from(context);
        this.tags = tags;
        this.visibleTags.addAll(tags);

        filterVisibleTags();
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.product_ingredient_analysis_tag, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Context context = contextRef.get();
        if (context != null) {
            AnalysisTagConfig tag = visibleTags.get(position);
            Utils.picassoBuilder(context)
                .load(tag.getIconUrl())
                .into(holder.icon);
            Drawable background = context.getResources().getDrawable(R.drawable.rounded_button);
            background.setColorFilter(Color.parseColor(tag.getColor()), android.graphics.PorterDuff.Mode.SRC_IN);
            holder.itemView.setBackground(background);

            holder.itemView.setTag(R.id.analysis_tag_config, tag);
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return visibleTags.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final View background;
        final AppCompatImageView icon;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            background = itemView;
            icon = itemView.findViewById(R.id.icon);
        }

        @Override
        public void onClick(View view) {
            if (onClickListener != null) {
                onClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    public void filterVisibleTags() {
        visibleTags.clear();
        for (AnalysisTagConfig tag :
            tags) {
            if (prefs.getBoolean(tag.getType(), true)) {
                visibleTags.add(tag);
            }
        }

        notifyDataSetChanged();
    }

    // allows clicks events to be caught
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onClickListener = onItemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}