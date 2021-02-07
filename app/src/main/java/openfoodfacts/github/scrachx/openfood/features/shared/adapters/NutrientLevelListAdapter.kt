package openfoodfacts.github.scrachx.openfood.features.shared.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.NO_ID
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.shared.adapters.NutrientLevelListAdapter.NutrientViewHolder
import openfoodfacts.github.scrachx.openfood.models.NutrientLevelItem
import openfoodfacts.github.scrachx.openfood.utils.bold

class NutrientLevelListAdapter(
        private val context: Context,
        private val nutrientLevelItems: List<NutrientLevelItem>
) : RecyclerView.Adapter<NutrientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            NutrientViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.nutrient_lvl_list_item, parent, false))

    override fun onBindViewHolder(holder: NutrientViewHolder, position: Int) {
        val (category, value, label, icon) = nutrientLevelItems[position]

        if (icon == NO_ID) {
            holder.imgIcon.visibility = View.GONE
        } else {
            holder.imgIcon.setImageDrawable(AppCompatResources.getDrawable(context, icon))
            holder.imgIcon.visibility = View.VISIBLE
        }

        holder.txtTitle.let {
            it.text = ""
            it.append(value)
            it.append(" ")
            it.append(bold(category))
            it.append("\n")
            it.append(label)
        }
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount() = nutrientLevelItems.size

    class NutrientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgIcon: ImageView = itemView.findViewById(R.id.imgLevel)
        val txtTitle: TextView = itemView.findViewById(R.id.descriptionLevel)
    }
}