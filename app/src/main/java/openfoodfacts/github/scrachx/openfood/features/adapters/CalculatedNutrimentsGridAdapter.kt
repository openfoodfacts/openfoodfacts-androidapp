package openfoodfacts.github.scrachx.openfood.features.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.NutrimentListItem

class CalculatedNutrimentsGridAdapter(private val nutrimentListItems: List<NutrimentListItem>) : NutrimentsGridAdapter(nutrimentListItems) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NutrimentViewHolder {
        val isViewTypeHeader = viewType == TYPE_HEADER
        val layoutResourceId = if (isViewTypeHeader) R.layout.nutrition_fact_header_calc else R.layout.nutriment_item_list
        val v = LayoutInflater.from(parent.context).inflate(layoutResourceId, parent, false)
        return if (isViewTypeHeader) {
            var displayServing = false
            for (nutriment in nutrimentListItems) {
                val servingValue = nutriment.servingValue
                if (servingValue != null && !servingValue.isNullOrBlank()) {
                    displayServing = true
                }
            }
            NutrimentHeaderViewHolder(v, displayServing)
        } else {
            NutrimentListViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: NutrimentViewHolder, position: Int) {
        if (holder !is NutrimentListViewHolder) {
            return
        }
        val item = nutrimentListItems[position]
        holder.fillNutrimentValue(item)
        holder.fillServingValue(item)
        holder.setIsRecyclable(false)
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }
}