package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang.StringUtils;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.NutrimentListItem;

public class CalculatedNutrimentsGridAdapter extends NutrimentsGridAdapter {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private final List<NutrimentListItem> nutrimentListItems;

    public CalculatedNutrimentsGridAdapter(List<NutrimentListItem> nutrimentListItems) {
        super(nutrimentListItems);
        this.nutrimentListItems = nutrimentListItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        boolean isViewTypeHeader = viewType == TYPE_HEADER;

        int layoutResourceId = isViewTypeHeader ? R.layout.nutrition_fact_header_calc : R.layout.nutriment_item_list;
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutResourceId, parent, false);

        if (isViewTypeHeader) {
            boolean displayServing = false;
            for (NutrimentListItem nutriment : nutrimentListItems) {
                final CharSequence servingValue = nutriment.getServingValue();
                if (servingValue != null && !StringUtils.isBlank(servingValue.toString())) {
                    displayServing = true;
                }
            }
            return new NutrimentHeaderViewHolder(v, displayServing);
        } else {
            return new NutrimentListViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof NutrimentListViewHolder)) {
            return;
        }

        NutrimentListItem item = nutrimentListItems.get(position);
        NutrimentListViewHolder nutrimentListViewHolder = (NutrimentListViewHolder) holder;
        nutrimentListViewHolder.fillNutrimentValue(item);
        nutrimentListViewHolder.fillServingValue(item);
        holder.setIsRecyclable(false);
    }

}

