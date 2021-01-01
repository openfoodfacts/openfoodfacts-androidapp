package openfoodfacts.github.scrachx.openfood.features.adapters.autocomplete

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager
import org.apache.commons.lang3.StringUtils
import java.util.*

class PeriodAfterOpeningAutoCompleteAdapter(
        context: Context?,
        textViewResourceId: Int
) : ArrayAdapter<String>(context!!, textViewResourceId), Filterable {
    private val client = CommonApiManager.productsApi
    private val periodsList = arrayListOf<String>()
    override fun getCount(): Int {
        return periodsList.size
    }

    override fun getItem(position: Int) =
            if (position < 0 || position >= periodsList.size) StringUtils.EMPTY else periodsList[position]

    override fun getFilter() = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            // If no search, 0 results
            if (constraint == null) return FilterResults().apply { count = 0 }

            // Retrieve the autocomplete results from server.
            val list = client.getPeriodAfterOpeningSuggestions(constraint.toString()).blockingGet()

            // Assign the data to the FilterResults
            return FilterResults().apply {
                values = list
                count = list.size
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results != null && results.count > 0) {
                periodsList.clear()
                periodsList.addAll((results.values as ArrayList<String>))
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }

}