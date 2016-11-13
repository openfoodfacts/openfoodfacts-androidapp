package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.NutrimentItem;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

public class NutritionInfoAdapter extends BaseAdapter {

    private Context mContext;
    private List<NutrimentItem> mNutItem;

    public NutritionInfoAdapter(Context context, List<NutrimentItem> nutItem) {
        this.mContext = context;
        this.mNutItem = nutItem;
    }

    @Override
    public int getCount() {
        return mNutItem.size();
    }

    @Override
    public Object getItem(int i) {
        return mNutItem.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            view = layoutInflater.inflate(R.layout.gauge_nutrition_fact, null);
        }

        final TextView title = (TextView) view.findViewById(R.id.title_text_view);
        final TextView value = (TextView) view.findViewById(R.id.value_text_view);
        final FrameLayout flc = (FrameLayout) view.findViewById(R.id.cview);
        final FrameLayout flr = (FrameLayout) view.findViewById(R.id.rview);

        title.setText(mNutItem.get(i).getTitle());
        value.setText(mNutItem.get(i).getValue());
        ((GradientDrawable) flr.getBackground()).setColor(Utils.getColor(view.getContext(), mNutItem.get(i).getColor()));
        ((GradientDrawable) flr.getBackground()).setStroke((int)view.getResources().getDimension(R.dimen.gauge_nutrition_rounded_circle_width), Utils.getColor(view.getContext(), mNutItem.get(i).getColor()));
        ((GradientDrawable) flc.getBackground()).setStroke((int)view.getResources().getDimension(R.dimen.gauge_nutrition_circle_width), Utils.getColor(view.getContext(), mNutItem.get(i).getColor()));


        return view;
    }
}
