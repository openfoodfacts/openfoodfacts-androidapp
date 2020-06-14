package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang.StringUtils;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.NutrimentListItem;

import static android.view.View.GONE;

/**
 * @author herau
 */
public class NutrimentsGridAdapter extends RecyclerView.Adapter {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private final List<NutrimentListItem> nutrimentListItems;

    public NutrimentsGridAdapter(List<NutrimentListItem> nutrimentListItems) {
        super();
        this.nutrimentListItems = nutrimentListItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        boolean isViewTypeHeader = viewType == TYPE_HEADER;

        int layoutResourceId = isViewTypeHeader ? R.layout.nutriment_item_list_header : R.layout.nutriment_item_list;
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NutrimentHeaderViewHolder) {
            NutrimentListItem item = nutrimentListItems.get(position);
            NutrimentHeaderViewHolder nutrimentViewHolder = (NutrimentHeaderViewHolder) holder;
            nutrimentViewHolder.vNutrimentValue.setText(item.shouldDisplayVolumeHeader() ? R.string.for_100ml : R.string.for_100g);
        }
        if (!(holder instanceof NutrimentListViewHolder)) {
            return;
        }

        NutrimentListItem item = nutrimentListItems.get(position);

        NutrimentListViewHolder nutrimentListViewHolder = (NutrimentListViewHolder) holder;
        nutrimentListViewHolder.fillNutrimentValue(item);
        nutrimentListViewHolder.fillServingValue(item);
    }

    @Override
    public int getItemViewType(int position) {
        return isPositionHeader(position) ? TYPE_HEADER : TYPE_ITEM;
    }

    boolean isPositionHeader(int position) {
        return position == 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return nutrimentListItems.size();
    }

    static class NutrimentListViewHolder extends RecyclerView.ViewHolder {
        private final TextView vNutrimentName;
        private final TextView vNutrimentServingValue;
        private final TextView vNutrimentValue;

        public NutrimentListViewHolder(View v) {
            super(v);
            vNutrimentName = v.findViewById(R.id.nutriment_name);
            vNutrimentValue = v.findViewById(R.id.nutriment_value);
            vNutrimentServingValue = v.findViewById(R.id.nutriment_serving_value);
        }

        void fillNutrimentValue(NutrimentListItem item) {
            vNutrimentName.setText(item.getTitle());
            vNutrimentValue.append(String.format("%s %s %s",
                item.getModifier(),
                item.getValue(),
                item.getUnit()));
        }

        void fillServingValue(NutrimentListItem item) {
            final CharSequence servingValue = item.getServingValue();
            if (StringUtils.isBlank(servingValue.toString())) {
                vNutrimentServingValue.setVisibility(GONE);
            } else {
                vNutrimentServingValue.append(String.format("%s %s %s",
                    item.getModifier(),
                    servingValue,
                    item.getUnit()));
            }
        }
    }

    class NutrimentHeaderViewHolder extends RecyclerView.ViewHolder {
        final TextView vNutrimentValue;

        public NutrimentHeaderViewHolder(View itemView, boolean displayServing) {
            super(itemView);
            vNutrimentValue = itemView.findViewById(R.id.nutriment_value);
            if (!displayServing) {
                itemView.findViewById(R.id.nutriment_serving_value).setVisibility(GONE);
            }
        }
    }
}
