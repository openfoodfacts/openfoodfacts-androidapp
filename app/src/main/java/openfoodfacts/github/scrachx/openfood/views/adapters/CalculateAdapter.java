package openfoodfacts.github.scrachx.openfood.views.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.NutrimentItem;

import java.util.List;

public class CalculateAdapter extends RecyclerView.Adapter {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private final List<NutrimentItem> nutrimentItems;

    public CalculateAdapter(List<NutrimentItem> nutrimentItems) {
        super();
        this.nutrimentItems = nutrimentItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == TYPE_HEADER) {
            int layoutResourceId = R.layout.nutrition_fact_header_new;
            View v = LayoutInflater.from(parent.getContext()).inflate(layoutResourceId, parent, false);
            return new NutrimentHeaderViewHolder(v);
        } else {
            int layoutResourceId = R.layout.nutriment_item_list;
            View v = LayoutInflater.from(parent.getContext()).inflate(layoutResourceId, parent, false);
            return new NutrimentViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof NutrimentViewHolder)) {
            return;
        }

        NutrimentItem item = nutrimentItems.get(position);
        NutrimentViewHolder nutrimentViewHolder = (NutrimentViewHolder) holder;
        nutrimentViewHolder.fillNutrimentValue(item);
        nutrimentViewHolder.fillServingValue(item);
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

    static class NutrimentViewHolder extends RecyclerView.ViewHolder {
        private TextView vNutrimentName;
        private TextView vNutrimentValue;
        private TextView vNutrimentServingValue;

        NutrimentViewHolder(View v) {
            super(v);
            vNutrimentName = v.findViewById(R.id.nutriment_name);
            vNutrimentValue = v.findViewById(R.id.nutriment_value);
            vNutrimentServingValue = v.findViewById(R.id.nutriment_serving_value);
        }

        void fillNutrimentValue(NutrimentItem item) {
            vNutrimentName.setText(item.getTitle());
            vNutrimentValue.append(item.getModifier());
            vNutrimentValue.append(item.getValue());
            vNutrimentValue.append(" ");
            vNutrimentValue.append(item.getUnit());
        }

        void fillServingValue(NutrimentItem item) {
            vNutrimentServingValue.append(item.getModifier());
            vNutrimentServingValue.append(item.getServingValue());
            vNutrimentServingValue.append(" ");
            vNutrimentServingValue.append(item.getUnit());
        }
    }

    class NutrimentHeaderViewHolder extends RecyclerView.ViewHolder {
        NutrimentHeaderViewHolder(View itemView) {
            super(itemView);
        }
    }
}

