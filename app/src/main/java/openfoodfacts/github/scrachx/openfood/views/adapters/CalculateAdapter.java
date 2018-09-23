package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.NutrimentItem;


public class CalculateAdapter extends RecyclerView.Adapter {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final List<NutrimentItem> nutrimentItems;

    public CalculateAdapter(List<NutrimentItem> nutrimentItems) {
        super();
        this.nutrimentItems = nutrimentItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == TYPE_HEADER)
        {
            int layoutResourceId=R.layout.nutrition_fact_header_new;
            View v =LayoutInflater.from(parent.getContext()).inflate(layoutResourceId, parent, false);
            return new NutrimentHeaderViewHolder(v);
        }
        else {
            int layoutResourceId = R.layout.nutriment_item_list;
            View v = LayoutInflater.from(parent.getContext()).inflate(layoutResourceId, parent, false);
            return new NutrimentViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof NutrimentViewHolder)) {
            return;
        }

        NutrimentItem item = nutrimentItems.get(position);

        NutrimentViewHolder nutrimentViewHolder = (NutrimentViewHolder) holder;

        nutrimentViewHolder.vNutrimentName.setText(item.getTitle());
        nutrimentViewHolder.vNutrimentValue.append(item.getModifier());
        nutrimentViewHolder.vNutrimentValue.append(item.getValue());
        nutrimentViewHolder.vNutrimentValue.append(" ");
        nutrimentViewHolder.vNutrimentValue.append(item.getUnit());

        nutrimentViewHolder.vNutrimentServingValue.append(item.getModifier());
        nutrimentViewHolder.vNutrimentServingValue.append(item.getServingValue());
        nutrimentViewHolder.vNutrimentServingValue.append(" ");
        nutrimentViewHolder.vNutrimentServingValue.append(item.getUnit());
        holder.setIsRecyclable(false);

    }

    @Override
    public int getItemViewType(int position) {
        return isPositionHeader(position) ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    @Override
    public int getItemCount() {
        return nutrimentItems.size();
    }

    class NutrimentViewHolder extends RecyclerView.ViewHolder {
        TextView vNutrimentName;
        TextView vNutrimentValue;
        TextView vNutrimentServingValue;

        public NutrimentViewHolder(View v) {
            super(v);
            vNutrimentName = v.findViewById(R.id.nutriment_name);
            vNutrimentValue = v.findViewById(R.id.nutriment_value);
            vNutrimentServingValue = v.findViewById(R.id.nutriment_serving_value);
        }
    }

    class NutrimentHeaderViewHolder extends RecyclerView.ViewHolder {
        public NutrimentHeaderViewHolder(View itemView) {
            super(itemView);
        }
    }
}

