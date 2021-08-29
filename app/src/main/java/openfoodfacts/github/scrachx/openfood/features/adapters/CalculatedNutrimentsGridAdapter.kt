package openfoodfacts.github.scrachx.openfood.features.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.NutrimentListItem

class CalculatedNutrimentsGridAdapter(private val nutrimentListItems: List<NutrimentListItem>) : NutrimentsGridAdapter(nutrimentListItems) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NutrimentViewHolder {
        val isViewTypeHeader = viewType == TYPE_HEADER
        val layoutResourceId = if (isViewTypeHeader) R.layout.nutrition_fact_header_calc else R.layout.nutriment_item_list
        val view = LayoutInflater.from(parent.context).inflate(layoutResourceId, parent, false)

        return if (isViewTypeHeader) {
            val displayServing = nutrimentListItems.any { !it.servingValueStr.isNullOrBlank() }
            NutrimentViewHolder.NutrimentHeaderViewHolder(view, displayServing)
        } else {
            NutrimentViewHolder.NutrimentListViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: NutrimentViewHolder, position: Int) {
        if (holder !is NutrimentViewHolder.NutrimentListViewHolder) return

        val item = nutrimentListItems[position]
        holder.fillNutriment(item)
        holder.setIsRecyclable(false)
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }
}