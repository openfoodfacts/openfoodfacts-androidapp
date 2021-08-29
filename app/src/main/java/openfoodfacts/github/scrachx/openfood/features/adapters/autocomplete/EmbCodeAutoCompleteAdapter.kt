package openfoodfacts.github.scrachx.openfood.features.adapters.autocomplete

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import kotlinx.coroutines.runBlocking
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import org.apache.commons.lang3.StringUtils
import java.util.*

class EmbCodeAutoCompleteAdapter(
    context: Context,
    textViewResourceId: Int,
    private val client: OpenFoodAPIClient
) : ArrayAdapter<String>(context, textViewResourceId), Filterable {
    private val codeList: MutableList<String> = arrayListOf()


    override fun getCount() = codeList.size

    override fun getItem(position: Int) =
        if (position in 0..codeList.size) codeList[position] else StringUtils.EMPTY

    override fun getFilter() = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {

            // if no value typed, return
            if (constraint == null) return FilterResults().apply { count = 0 }

            // Retrieve the autocomplete results from server.
            val list = runBlocking { client.getEMBCodeSuggestions(constraint.toString()) }

            // Assign the data to the FilterResults
            return FilterResults().apply {
                values = list
                count = list.size
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results != null && results.count > 0) {
                codeList.clear()
                codeList += results.values as ArrayList<String>
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }

}
