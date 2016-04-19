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
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.SaveItem;

public class SaveListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<SaveItem> saveItems;

    public SaveListAdapter(Context context, ArrayList<SaveItem> saveItems){
        this.context = context;
        this.saveItems = saveItems;
    }

    @Override
    public int getCount() {
        return saveItems.size();
    }

    @Override
    public Object getItem(int position) {
        return saveItems.get(position);
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
            convertView = mInflater.inflate(R.layout.save_list_item, null);
        }

        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.iconSave);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.titleSave);
        TextView txtBarcode= (TextView) convertView.findViewById(R.id.barcodeSave);
        ImageView imgProduct = (ImageView) convertView.findViewById(R.id.imgSaveProduct);

        imgIcon.setImageResource(saveItems.get(position).getIcon());
        txtTitle.setText(saveItems.get(position).getTitle());
        imgProduct.setImageBitmap(saveItems.get(position).getUrl());
        txtBarcode.setText( saveItems.get(position).getBarcode());

        return convertView;
    }

}