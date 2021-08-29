package openfoodfacts.github.scrachx.openfood.features.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.NutrimentListItem

/**
 * @author herau
 */
open class NutrimentsGridAdapter(
    private val nutrimentListItems: List<NutrimentListItem>
) : RecyclerView.Adapter<NutrimentsGridAdapter.NutrimentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NutrimentViewHolder {
        val isViewTypeHeader = viewType == TYPE_HEADER
        val layoutResourceId = if (isViewTypeHeader) R.layout.nutriment_item_list_header else R.layout.nutriment_item_list
        val view = LayoutInflater.from(parent.context).inflate(layoutResourceId, parent, false)

        return if (isViewTypeHeader) {
            val displayServing = nutrimentListItems.any { !it.servingValueStr.isNullOrBlank() }
            NutrimentViewHolder.NutrimentHeaderViewHolder(view, displayServing)
        } else {
            NutrimentViewHolder.NutrimentListViewHolder(view)
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
                holder.fillNutriment(item)
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

            fun fillNutriment(item: NutrimentListItem) {
                fillNutrimentName(item)
                fillNutrimentValue(item)
                fillServingValue(item)
            }


            private fun fillNutrimentName(item: NutrimentListItem) {
                vNutrimentName.text = item.title
            }

            private fun fillNutrimentValue(item: NutrimentListItem) {
                val value = item.value
                if (value == null) {
                    vNutrimentValue.visibility = View.INVISIBLE
                } else {
                    vNutrimentValue.visibility = View.VISIBLE
                    vNutrimentValue.text = "${item.modifierStr} $value ${item.unitStr}"
                }
            }

            private fun fillServingValue(item: NutrimentListItem) {
                val servingValue = item.servingValueStr
                if (servingValue == null) {
                    vNutrimentServingValue.visibility = View.GONE
                } else {
                    vNutrimentServingValue.visibility = View.VISIBLE
                    vNutrimentServingValue.text = "${item.modifierStr} $servingValue ${item.unitStr}"
                }
            }
        }

        internal class NutrimentHeaderViewHolder(
            itemView: View,
            displayServing: Boolean
        ) : NutrimentViewHolder(itemView) {
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


