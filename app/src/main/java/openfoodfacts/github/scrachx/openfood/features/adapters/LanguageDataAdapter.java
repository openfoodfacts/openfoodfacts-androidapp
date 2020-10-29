/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package openfoodfacts.github.scrachx.openfood.features.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;

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
            v.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_plus_blue_24, 0, 0, 0);
        }
        return v;
    }
}
