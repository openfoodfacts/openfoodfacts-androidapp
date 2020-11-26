package openfoodfacts.github.scrachx.openfood.features.adapters.autocomplete

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager.productsApi
import org.apache.commons.lang.StringUtils
import java.util.*

class EmbCodeAutoCompleteAdapter(
        context: Context?,
        textViewResourceId: Int
) : ArrayAdapter<String>(context!!, textViewResourceId), Filterable {
    private val client = productsApi
    private val codeList: MutableList<String> = arrayListOf()


    override fun getCount() = codeList.size

    override fun getItem(position: Int): String {
        return if (position < 0 || position >= codeList.size) StringUtils.EMPTY else codeList[position]
    }

    override fun getFilter() = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {

            // if no value typed, return
            if (constraint == null) {
                return FilterResults().apply {
                    count = 0
                }
            }
            // Retrieve the autocomplete results from server.
            val list = client.getEMBCodeSuggestions(constraint.toString()).blockingGet()

            // Assign the data to the FilterResults
            return FilterResults().apply {
                values = list
                count = list.size
            }
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults?) {
            if (results != null && results.count > 0) {
                codeList.clear()
                codeList.addAll((results.values as ArrayList<String>))
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }

}