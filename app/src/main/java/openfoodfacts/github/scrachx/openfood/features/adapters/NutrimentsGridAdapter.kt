package openfoodfacts.github.scrachx.openfood.features.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.NutrimentListItem
import org.apache.commons.lang.StringUtils

/**
 * @author herau
 */
open class NutrimentsGridAdapter(private val nutrimentListItems: List<NutrimentListItem>) : RecyclerView.Adapter<NutrimentsGridAdapter.NutrimentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NutrimentViewHolder {
        val isViewTypeHeader = viewType == TYPE_HEADER
        val layoutResourceId = if (isViewTypeHeader) R.layout.nutriment_item_list_header else R.layout.nutriment_item_list
        val v = LayoutInflater.from(parent.context).inflate(layoutResourceId, parent, false)

        return if (isViewTypeHeader) {
            var displayServing = false
            for (nutriment in nutrimentListItems) {
                val servingValue = nutriment.servingValue
                if (servingValue.isBlank()) displayServing = true
            }
            NutrimentViewHolder.NutrimentHeaderViewHolder(v, displayServing)
        } else {
            NutrimentViewHolder.NutrimentListViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: NutrimentViewHolder, position: Int) {
        when (holder) {
            is NutrimentViewHolder.NutrimentHeaderViewHolder -> {
                val item = nutrimentListItems[position]
                holder.vNutrimentValue.setText(if (item.displayVolumeHeader) R.string.for_100ml else R.string.for_100g)
            }
            is NutrimentViewHolder.NutrimentListViewHolder -> {
                val item = nutrimentListItems[position]
                holder.fillNutrimentValue(item)
                holder.fillServingValue(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isPositionHeader(position)) TYPE_HEADER else TYPE_ITEM
    }

    private fun isPositionHeader(position: Int) = position == 0

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount() = nutrimentListItems.size


    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }


    sealed class NutrimentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal class NutrimentListViewHolder(v: View) : NutrimentViewHolder(v) {
            private val vNutrimentName: TextView = v.findViewById(R.id.nutriment_name)
            private val vNutrimentServingValue: TextView = v.findViewById(R.id.nutriment_serving_value)
            private val vNutrimentValue: TextView = v.findViewById(R.id.nutriment_value)

            fun fillNutrimentValue(item: NutrimentListItem) {
                vNutrimentName.text = item.title
                vNutrimentValue.append("${item.modifier} ${item.value} ${item.unit}")
            }

            fun fillServingValue(item: NutrimentListItem) {
                val servingValue = item.servingValue
                if (StringUtils.isBlank(servingValue.toString())) {
                    vNutrimentServingValue.visibility = View.GONE
                } else {
                    vNutrimentServingValue.append(String.format("%s %s %s",
                            item.modifier,
                            servingValue,
                            item.unit))
                }
            }
        }

        internal class NutrimentHeaderViewHolder(itemView: View, displayServing: Boolean) : NutrimentViewHolder(itemView) {
            val vNutrimentValue: TextView = itemView.findViewById(R.id.nutriment_value)
            private val nutrimentServingValue: TextView = itemView.findViewById(R.id.nutriment_serving_value)

            init {
                if (!displayServing) {
                    nutrimentServingValue.visibility = View.GONE
                }
            }
        }
    }
}


