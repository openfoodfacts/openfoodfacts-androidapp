package openfoodfacts.github.scrachx.openfood.views.adapters.autocomplete;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

import openfoodfacts.github.scrachx.openfood.network.CommonApiManager;
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI;

public class PeriodAfterOpeningAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    private ProductsAPI client;
    private final ArrayList<String> periodsList;

    public PeriodAfterOpeningAutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        client = CommonApiManager.getInstance().getProductsApi();
        periodsList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return periodsList.size();
    }

    @Override
    public String getItem(int position) {
        if (position < 0 || position >= periodsList.size()) {
            return StringUtils.EMPTY;
        }
        return periodsList.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                filterResults.count = 0;
                if (constraint == null) {
                    return filterResults;
                }
                // Retrieve the autocomplete results from server.
                periodsList.clear();
                periodsList.addAll(client.getPeriodAfterOpeningSuggestions(constraint.toString()).blockingGet());

                // Assign the data to the FilterResults
                filterResults.values = periodsList;
                filterResults.count = periodsList.size();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}
