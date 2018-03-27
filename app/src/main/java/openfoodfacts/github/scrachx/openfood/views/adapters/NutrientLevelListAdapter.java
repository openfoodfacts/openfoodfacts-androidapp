package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevelItem;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;

public class NutrientLevelListAdapter extends RecyclerView.Adapter<NutrientLevelListAdapter.NutrientViewHolder> {

    private Context context;
    private List<NutrientLevelItem> nutrientLevelItems;

    public NutrientLevelListAdapter(Context context, List<NutrientLevelItem> navDrawerItems) {
        this.context = context;
        this.nutrientLevelItems = navDrawerItems;
    }

    @Override
    public NutrientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.nutrient_lvl_list_item, parent, false);
        return new NutrientViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NutrientViewHolder holder, int position) {

        NutrientLevelItem nutrientLevelItem = nutrientLevelItems.get(position);


        if (nutrientLevelItem.getIcon() <= 0) {
            holder.imgIcon.setVisibility(View.GONE);
        } else {
            holder.imgIcon.setImageDrawable(AppCompatResources.getDrawable(context, nutrientLevelItem.getIcon()));
            holder.imgIcon.setVisibility(View.VISIBLE);
        }


        TextView titleTxt = holder.txtTitle;
        titleTxt.setText("");
        titleTxt.append(nutrientLevelItem.getValue());
        titleTxt.append(" ");
        titleTxt.append(bold(nutrientLevelItem.getCategory()));
        titleTxt.append("\n");
        titleTxt.append(nutrientLevelItem.getLabel());

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return nutrientLevelItems.size();
    }


    class NutrientViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView txtTitle;

        public NutrientViewHolder(View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgLevel);
            txtTitle = itemView.findViewById(R.id.descriptionLevel);


        }
    }

}
