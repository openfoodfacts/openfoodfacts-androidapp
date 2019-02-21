package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;

public class PeriodAfterOpeningAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    private static OpenFoodAPIService client;
    private ArrayList<String> mPeriodsAfterOpeningList;

    public PeriodAfterOpeningAutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mPeriodsAfterOpeningList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mPeriodsAfterOpeningList.size();
    }

    @Override
    public String getItem(int position) {
        return mPeriodsAfterOpeningList.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter filter;
        filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results from server.
                    client = CommonApiManager.getInstance().getOpenFoodApiService();
                    client.getPeriodAfterOpeningSuggestions(constraint.toString())
                            .subscribe(new SingleObserver<ArrayList<String>>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(ArrayList<String> strings) {
                                    mPeriodsAfterOpeningList.clear();
                                    mPeriodsAfterOpeningList.addAll(strings);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e(PeriodAfterOpeningAutoCompleteAdapter.class.getSimpleName(), e.getMessage());
                                }
                            });

                    // Assign the data to the FilterResults
                    filterResults.values = mPeriodsAfterOpeningList;
                    filterResults.count = mPeriodsAfterOpeningList.size();
                }
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
        return filter;
    }
}