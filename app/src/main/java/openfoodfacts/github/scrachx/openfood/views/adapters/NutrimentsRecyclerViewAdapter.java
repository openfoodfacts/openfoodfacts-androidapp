package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.NutrimentItem;

/**
 * @author herau
 */
public class NutrimentsRecyclerViewAdapter extends RecyclerView.Adapter {

    private final List<NutrimentItem> nutrimentItems;

    public NutrimentsRecyclerViewAdapter(List<NutrimentItem> nutrimentItems) {
        super();
        this.nutrimentItems = nutrimentItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nutriment_item_list, parent, false);

        return new NutrimentsRecyclerViewAdapter.NutrimentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        NutrimentItem item = nutrimentItems.get(position);

        NutrimentViewHolder nutrimentViewHolder = (NutrimentViewHolder) holder;

        nutrimentViewHolder.vNutrimentName.setText(item.getTitle());
        nutrimentViewHolder.vNutrimentValue.setText(item.getValue() + " " + item.getUnit());
        nutrimentViewHolder.vNutrimentServingValue.setText(item.getServingValue() + " " + item.getUnit());
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
            vNutrimentName = (TextView) v.findViewById(R.id.nutriment_name);
            vNutrimentValue = (TextView) v.findViewById(R.id.nutriment_value);
            vNutrimentServingValue = (TextView) v.findViewById(R.id.nutriment_serving_value);
        }
    }
}
