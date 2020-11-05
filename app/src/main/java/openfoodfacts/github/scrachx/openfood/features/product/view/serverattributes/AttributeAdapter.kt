package openfoodfacts.github.scrachx.openfood.features.product.view.serverattributes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.entities.attribute.Attribute


class AttributeAdapter(var attributes: List<Attribute>) : RecyclerView.Adapter<AttributeViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttributeViewHolder {
        return AttributeViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.attribute_group_item, parent, false))
    }

    override fun onBindViewHolder(holder: AttributeViewHolder, position: Int) {
        val attribute = attributes[position]
        holder.title.text = attribute.name
        holder.shortDesc.text = attribute.descriptionShort
        val iconUrl = attribute.iconUrl
        if (iconUrl != null) {
            Picasso.get().load(iconUrl.replace(".svg", ".png")).into(holder.icon)
        }
    }

    override fun getItemCount(): Int {
        return attributes.size
    }

}


class AttributeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var icon: ImageView = itemView.findViewById(R.id.logo_view)
    var title: TextView = itemView.findViewById(R.id.title_text)
    var shortDesc: TextView = itemView.findViewById(R.id.short_desc_text)
}

