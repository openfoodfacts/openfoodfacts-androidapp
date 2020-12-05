package openfoodfacts.github.scrachx.openfood.features.product.view.summary

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig
import openfoodfacts.github.scrachx.openfood.utils.Utils
import java.lang.ref.WeakReference

class IngredientAnalysisTagsAdapter(context: Context, private val tags: List<AnalysisTagConfig>) : RecyclerView.Adapter<IngredientAnalysisTagsAdapter.IngredientAnalysisTagsViewHolder>() {
    private val contextRef: WeakReference<Context> = WeakReference(context)
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val visibleTags = tags.toMutableList()
    private var onClickListener: ((View, Int) -> Unit)? = null

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientAnalysisTagsViewHolder {
        val view = LayoutInflater.from(contextRef.get()).inflate(R.layout.product_ingredient_analysis_tag, parent, false)
        return IngredientAnalysisTagsViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: IngredientAnalysisTagsViewHolder, position: Int) {
        val context = contextRef.get() ?: return
        val tag = visibleTags[position]
        Utils.picassoBuilder(context)
                .load(tag.iconUrl)
                .into(holder.icon)
        holder.itemView.background = ResourcesCompat.getDrawable(context.resources, R.drawable.rounded_button, null).also {
            it?.setColorFilter(Color.parseColor(tag.color), PorterDuff.Mode.SRC_IN)
        }
        holder.itemView.setTag(R.id.analysis_tag_config, tag)
    }

    // total number of rows
    override fun getItemCount(): Int {
        return visibleTags.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class IngredientAnalysisTagsViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val background = itemView
        val icon: AppCompatImageView = itemView.findViewById(R.id.icon)
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