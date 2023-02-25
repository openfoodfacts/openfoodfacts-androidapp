package openfoodfacts.github.scrachx.openfood.features.product.view.ingredients_analysis.adapter

import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.features.product.view.ingredients_analysis.adapter.IngredientAnalysisRecyclerAdapter.IngredientAnalysisViewHolder
import openfoodfacts.github.scrachx.openfood.models.ProductIngredient

class IngredientAnalysisRecyclerAdapter(
    private val productIngredients: List<ProductIngredient>,
    private val activity: Activity,
) : RecyclerView.Adapter<IngredientAnalysisViewHolder>(), CustomTabActivityHelper.ConnectionCallback {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientAnalysisViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.ingredient_analysis_list_item, parent, false)
        return IngredientAnalysisViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientAnalysisViewHolder, position: Int) {
        val id = productIngredients[position].id.replace("\"", "")
        val name = productIngredients[position].text?.replace("\"", "") //removes quotations
        holder.tvIngredientName.text = name ?: id
        holder.tvIngredientName.setOnClickListener {
            val customTabsIntent = CustomTabsIntent.Builder().build().apply {
                intent.putExtra("android.intent.extra.REFERRER", Uri.parse("android-app://" + activity.packageName))
            }
            CustomTabActivityHelper.openCustomTab(
                activity,
                customTabsIntent,
                Uri.parse("${activity.getString(R.string.website)}ingredient/$id"),
                WebViewFallback()
            )
        }
    }

    class IngredientAnalysisViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIngredientName: TextView = itemView.findViewById(R.id.tv_ingredient_name)
    }

    override fun getItemCount() = productIngredients.size
    override fun onCustomTabsConnected() = Unit
    override fun onCustomTabsDisconnected() = Unit
}