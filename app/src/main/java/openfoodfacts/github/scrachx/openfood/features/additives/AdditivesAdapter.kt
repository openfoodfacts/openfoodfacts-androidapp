package openfoodfacts.github.scrachx.openfood.features.additives

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName

/**
 * Created by prajwalm on 16/04/18.
 */
class AdditivesAdapter(private val additives: List<AdditiveName>, private val clickListener: ClickListener?) : RecyclerView.Adapter<AdditiveViewHolder>() {
    interface ClickListener {
        fun onClick(position: Int, name: String?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdditiveViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.additives_item, parent, false)
        return AdditiveViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AdditiveViewHolder, position: Int) {
        holder.additiveName.text = additives[position].name

        holder.itemView.setOnClickListener {
            if (clickListener != null) {
                val pos = holder.adapterPosition
                clickListener.onClick(pos, additives[pos].name)
            }
        }
    }

    override fun getItemCount(): Int {
        return additives.size
    }
}

class AdditiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val additiveName: TextView = itemView.findViewById(R.id.additiveName)
}