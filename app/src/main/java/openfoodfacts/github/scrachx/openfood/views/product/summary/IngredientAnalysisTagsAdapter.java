package openfoodfacts.github.scrachx.openfood.views.product.summary;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;

public class IngredientAnalysisTagsAdapter extends RecyclerView.Adapter<IngredientAnalysisTagsAdapter.ViewHolder> {
    private List<String> tags;
    private LayoutInflater inflater;
    private OnItemClickListener onClickListener;
    private WeakReference<Context> contextRef;

    // data is passed into the constructor
    public IngredientAnalysisTagsAdapter(Context context, List<String> tags) {
        contextRef = new WeakReference<>(context);
        this.inflater = LayoutInflater.from(context);
        this.tags = tags;
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
            String tag = tags.get(position);
            holder.itemView.setTag(tag);
            switch (tag) {
                case "en:palm-oil-free":
                    holder.icon.setImageResource(R.drawable.ic_monkey_happy);
                    Drawable background = context.getResources().getDrawable(R.drawable.rounded_button);
                    background.setColorFilter(ContextCompat.getColor(context, R.color.monkey_happy), android.graphics.PorterDuff.Mode.SRC_IN);
                    holder.itemView.setBackground(background);
                    holder.itemView.setTag(R.id.analysis_tag, "from_palm_oil");
                    holder.itemView.setTag(R.id.analysis_tag_value, "no");
                    break;
                case "en:may-contain-palm-oil":
                    holder.icon.setImageResource(R.drawable.ic_monkey_happy);
                    background = context.getResources().getDrawable(R.drawable.rounded_button);
                    background.setColorFilter(ContextCompat.getColor(context, R.color.monkey_uncertain), android.graphics.PorterDuff.Mode.SRC_IN);
                    holder.itemView.setBackground(background);
                    holder.itemView.setTag(R.id.analysis_tag, "from_palm_oil");
                    holder.itemView.setTag(R.id.analysis_tag_value, "maybe");
                    break;
                case "en:palm-oil":
                    holder.icon.setImageResource(R.drawable.ic_monkey_unhappy);
                    background = context.getResources().getDrawable(R.drawable.rounded_button);
                    background.setColorFilter(ContextCompat.getColor(context, R.color.monkey_sad), android.graphics.PorterDuff.Mode.SRC_IN);
                    holder.itemView.setBackground(background);
                    holder.itemView.setTag(R.id.analysis_tag, "from_palm_oil");
                    holder.itemView.setTag(R.id.analysis_tag_value, "yes");
                    break;
                case "en:vegetarian":
                    holder.icon.setImageResource(R.drawable.ic_egg);
                    background = context.getResources().getDrawable(R.drawable.rounded_button);
                    background.setColorFilter(ContextCompat.getColor(context, R.color.monkey_happy), android.graphics.PorterDuff.Mode.SRC_IN);
                    holder.itemView.setBackground(background);
                    holder.itemView.setTag(R.id.analysis_tag, "vegetarian");
                    holder.itemView.setTag(R.id.analysis_tag_value, "yes");
                    break;
                case "en:maybe-vegetarian":
                    holder.icon.setImageResource(R.drawable.ic_egg);
                    background = context.getResources().getDrawable(R.drawable.rounded_button);
                    background.setColorFilter(ContextCompat.getColor(context, R.color.monkey_uncertain), android.graphics.PorterDuff.Mode.SRC_IN);
                    holder.itemView.setBackground(background);
                    holder.itemView.setTag(R.id.analysis_tag, "vegetarian");
                    holder.itemView.setTag(R.id.analysis_tag_value, "maybe");
                    break;
                case "en:non-vegetarian":
                    holder.icon.setImageResource(R.drawable.ic_egg);
                    background = context.getResources().getDrawable(R.drawable.rounded_button);
                    background.setColorFilter(ContextCompat.getColor(context, R.color.monkey_sad), android.graphics.PorterDuff.Mode.SRC_IN);
                    holder.itemView.setBackground(background);
                    holder.itemView.setTag(R.id.analysis_tag, "vegetarian");
                    holder.itemView.setTag(R.id.analysis_tag_value, "no");
                    break;
                case "en:vegan":
                    holder.icon.setImageResource(R.drawable.ic_leaf);
                    background = context.getResources().getDrawable(R.drawable.rounded_button);
                    background.setColorFilter(ContextCompat.getColor(context, R.color.monkey_happy), android.graphics.PorterDuff.Mode.SRC_IN);
                    holder.itemView.setBackground(background);
                    holder.itemView.setTag(R.id.analysis_tag, "vegan");
                    holder.itemView.setTag(R.id.analysis_tag_value, "yes");
                    break;
                case "en:maybe-vegan":
                    holder.icon.setImageResource(R.drawable.ic_leaf);
                    background = context.getResources().getDrawable(R.drawable.rounded_button);
                    background.setColorFilter(ContextCompat.getColor(context, R.color.monkey_uncertain), android.graphics.PorterDuff.Mode.SRC_IN);
                    holder.itemView.setBackground(background);
                    holder.itemView.setTag(R.id.analysis_tag, "vegan");
                    holder.itemView.setTag(R.id.analysis_tag_value, "maybe");
                    break;
                case "en:non-vegan":
                    holder.icon.setImageResource(R.drawable.ic_leaf);
                    background = context.getResources().getDrawable(R.drawable.rounded_button);
                    background.setColorFilter(ContextCompat.getColor(context, R.color.monkey_sad), android.graphics.PorterDuff.Mode.SRC_IN);
                    holder.itemView.setBackground(background);
                    holder.itemView.setTag(R.id.analysis_tag, "vegan");
                    holder.itemView.setTag(R.id.analysis_tag_value, "no");
                    break;
                default:
                    holder.icon.setImageDrawable(null);
                    break;
            }
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return tags.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View background;
        AppCompatImageView icon;

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

    // allows clicks events to be caught
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onClickListener = onItemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}