package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.NutrimentItem;

/**
 * @author herau
 */
public class NutrimentsRecyclerViewAdapter extends RecyclerView.Adapter {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int UNSELECTED = -1;

    private final List<NutrimentItem> nutrimentItems;
    private RecyclerView recyclerView;
    private int selectedItem = UNSELECTED;

    public NutrimentsRecyclerViewAdapter(List<NutrimentItem> nutrimentItems, RecyclerView recyclerView) {
        super();
        this.recyclerView = recyclerView;
        this.nutrimentItems = nutrimentItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        boolean isViewTypeHeader = viewType == TYPE_HEADER;

        int layoutResourceId = isViewTypeHeader ? R.layout.nutriment_item_list_header : R.layout.nutriment_item_list;
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutResourceId, parent, false);

        return isViewTypeHeader ? new NutrimentHeaderViewHolder(v) : new NutrimentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof NutrimentViewHolder)) {
            return;
        }

        boolean isSelected = position == selectedItem;
        NutrimentItem item = nutrimentItems.get(position);

        NutrimentViewHolder nutrimentViewHolder = (NutrimentViewHolder) holder;

        nutrimentViewHolder.vNutrimentName.setText(item.getTitle());
        nutrimentViewHolder.vNutrimentValue.append(item.getValue());
        nutrimentViewHolder.vNutrimentValue.append(" ");
        nutrimentViewHolder.vNutrimentValue.append(item.getUnit());

        nutrimentViewHolder.vNutrimentServingValue.append(item.getServingValue());
        nutrimentViewHolder.vNutrimentServingValue.append(" ");
        nutrimentViewHolder.vNutrimentServingValue.append(item.getUnit());

        nutrimentViewHolder.vNutrimentPreparedValue.append(item.getPreparedValue());
        nutrimentViewHolder.vNutrimentPreparedValue.append(" ");
        nutrimentViewHolder.vNutrimentPreparedValue.append(item.getUnit());

        nutrimentViewHolder.vNutrimentServingPreparedValue.append(item.getPreparedServingValue());
        nutrimentViewHolder.vNutrimentServingPreparedValue.append(" ");
        nutrimentViewHolder.vNutrimentServingPreparedValue.append(item.getUnit());

        if (isSelected) {
            nutrimentViewHolder.imageView.animate().rotation(180).start();
        }
        nutrimentViewHolder.expandButton.setSelected(isSelected);
        nutrimentViewHolder.expandableLayout.setExpanded(isSelected, false);

    }

    @Override
    public int getItemViewType(int position) {
        return isPositionHeader(position) ? TYPE_HEADER : TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    @Override
    public int getItemCount() {
        return nutrimentItems.size();
    }

    class NutrimentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, ExpandableLayout.OnExpansionUpdateListener {
        TextView vNutrimentName;
        TextView vNutrimentValue;
        TextView vNutrimentServingValue;
        TextView vNutrimentPreparedValue;
        TextView vNutrimentServingPreparedValue;
        ExpandableLayout expandableLayout;
        LinearLayout expandButton;
        ImageView imageView;

        public NutrimentViewHolder(View v) {
            super(v);
            expandableLayout = v.findViewById(R.id.expandable_layout);
            vNutrimentName = v.findViewById(R.id.nutriment_name);
            vNutrimentValue = v.findViewById(R.id.nutriment_value);
            vNutrimentServingValue = v.findViewById(R.id.nutriment_serving_value);
            imageView = v.findViewById(R.id.dropdown_image);
            vNutrimentPreparedValue = v.findViewById(R.id.nutriment_prepared_value);
            vNutrimentServingPreparedValue = v.findViewById(R.id.nutriment_prepared_serving_value);
            expandableLayout.setInterpolator(new OvershootInterpolator());
            expandableLayout.setOnExpansionUpdateListener(this);
            expandButton = v.findViewById(R.id.expand_button);
            expandButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            NutrimentViewHolder holder = (NutrimentViewHolder) recyclerView.findViewHolderForAdapterPosition(selectedItem);
            if (holder != null) {
                holder.expandButton.setSelected(false);
                holder.expandableLayout.collapse();
                holder.imageView.animate().rotation(360).start();
            }
            int position = getAdapterPosition();
            if (position == selectedItem) {
                selectedItem = UNSELECTED;
            } else {
                expandButton.setSelected(true);
                expandableLayout.expand();
                selectedItem = position;
                imageView.animate().rotation(180).start();
            }
        }

        @Override
        public void onExpansionUpdate(float expansionFraction, int state) {
            if (state == ExpandableLayout.State.EXPANDING) {
                recyclerView.smoothScrollToPosition(getAdapterPosition());
            }
        }
    }

    class NutrimentHeaderViewHolder extends RecyclerView.ViewHolder {
        public NutrimentHeaderViewHolder(View itemView) {
            super(itemView);
        }
    }
}
