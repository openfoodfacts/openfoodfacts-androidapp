package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;

import java.util.List;

public class LanguageDataAdapter extends ArrayAdapter {
    public LanguageDataAdapter(@NonNull Context context, int resource, @NonNull List<LocaleHelper.LanguageData> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView v = (TextView) super.getView(position, convertView, parent);
        LocaleHelper.LanguageData data = (LocaleHelper.LanguageData) getItem(position);
        v.setTextColor(ContextCompat.getColor(getContext(), data.isSupported() ? R.color.white : R.color.orange));
        return v;
    }

    public int getPosition(String code) {
        int nb = getCount();
        if (code != null) {
            for (int i = 0; i < nb; i++) {
                if (code.equals(((LocaleHelper.LanguageData) getItem(i)).getCode())) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView v = (TextView) super.getDropDownView(position, convertView, parent);
        LocaleHelper.LanguageData data = (LocaleHelper.LanguageData) getItem(position);
        if (data.isSupported()) {
            v.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            v.setCompoundDrawablesWithIntrinsicBounds(R.drawable.plus_blue, 0, 0, 0);
        }
        return v;
    }
}
