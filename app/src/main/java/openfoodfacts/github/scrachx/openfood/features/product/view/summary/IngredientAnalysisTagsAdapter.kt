package openfoodfacts.github.scrachx.openfood.features.product.view.summary

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat.createBlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig
import openfoodfacts.github.scrachx.openfood.utils.Utils

class IngredientAnalysisTagsAdapter(private val context: Context, private val tags: List<AnalysisTagConfig>) : RecyclerView.Adapter<IngredientAnalysisTagsAdapter.IngredientAnalysisTagsViewHolder>() {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val visibleTags = tags.toMutableList()
    private var onClickListener: ((View, Int) -> Unit)? = null

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientAnalysisTagsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.product_ingredient_analysis_tag, parent, false)
        return IngredientAnalysisTagsViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: IngredientAnalysisTagsViewHolder, position: Int) {
        val tag = visibleTags[position]
        Utils.picassoBuilder(context)
                .load(tag.iconUrl)
                .into(holder.icon)

        holder.background.background = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.rounded_button,
                context.theme
        )?.apply {
            colorFilter = createBlendModeColorFilterCompat(Color.parseColor(tag.color), BlendModeCompat.SRC_IN)
        }

        holder.background.setTag(R.id.analysis_tag_config, tag)
    }

    // total number of rows
    override fun getItemCount() = visibleTags.count()

    // stores and recycles views as they are scrolled off screen
    inner class IngredientAnalysisTagsViewHolder internal constructor(val itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val background = itemView
        val icon: ImageView = itemView.findViewById(R.id.icon)

        override fun onClick(view: View) {
            onClickListener?.let { it(view, adapterPosition) }
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

    fun filterVisibleTags() {
        visibleTags.clear()
        tags.filterTo(visibleTags) { prefs.getBoolean(it.type, true) }
        notifyDataSetChanged()
    }

    // allows clicks events to be caught
    fun setOnItemClickListener(listener: (View, Int) -> Unit) {
        onClickListener = listener
    }

    // data is passed into the constructor
    init {
        filterVisibleTags()
    }
}