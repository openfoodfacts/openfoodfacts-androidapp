package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevelItem;

public class NutrientLevelListAdapter extends BaseAdapter {

    private Context context;
    private List<NutrientLevelItem> nutrientLevelItems;

    public NutrientLevelListAdapter(Context context, ArrayList<NutrientLevelItem> navDrawerItems){
        this.context = context;
        this.nutrientLevelItems = navDrawerItems;
    }

    @Override
    public int getCount() {
        return nutrientLevelItems.size();
    }

    @Override
    public Object getItem(int position) {
        return nutrientLevelItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.nutrient_lvl_list_item, null);
        }

        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.imgLevel);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.descriptionLevel);

        imgIcon.setImageResource(nutrientLevelItems.get(position).getIcon());
        txtTitle.setText(nutrientLevelItems.get(position).getTitle());

        return convertView;
    }

}
