package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevelItem;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;

public class NutrientLevelListAdapter extends BaseAdapter {

    private Context context;
    private List<NutrientLevelItem> nutrientLevelItems;

    public NutrientLevelListAdapter(Context context, List<NutrientLevelItem> navDrawerItems) {
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


        NutrientLevelItem nutrientLevelItem = nutrientLevelItems.get(position);

        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.imgLevel);
        if (nutrientLevelItem.getIcon() <= 0) {
            imgIcon.setVisibility(View.GONE);
        } else {
            imgIcon.setImageDrawable(AppCompatResources.getDrawable(context, nutrientLevelItem.getIcon()));
            imgIcon.setVisibility(View.VISIBLE);
        }

        TextView txtTitle = (TextView) convertView.findViewById(R.id.descriptionLevel);
        // need to clear the text because using append method (append method is mandatory for html text)
        txtTitle.setText("");
        txtTitle.append(nutrientLevelItem.getValue());
        txtTitle.append(" ");
        txtTitle.append(bold(nutrientLevelItem.getCategory()));
        txtTitle.append("\n");
        txtTitle.append(nutrientLevelItem.getLabel());

        return convertView;
    }

}
