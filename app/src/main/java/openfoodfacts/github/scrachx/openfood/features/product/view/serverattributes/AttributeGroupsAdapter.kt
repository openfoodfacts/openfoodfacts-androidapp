package openfoodfacts.github.scrachx.openfood.features.product.view.serverattributes

import android.app.Activity
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.entities.attribute.Attribute
import openfoodfacts.github.scrachx.openfood.models.entities.attribute.AttributeGroup


class AttributeGroupsAdapter(private val attributeGroups: List<AttributeGroup>, private val activity: Activity) : BaseExpandableListAdapter() {
    override fun getGroupCount(): Int {
        return attributeGroups.count()
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return attributeGroups[groupPosition].attributes?.size
                ?: throw ArrayIndexOutOfBoundsException("$groupPosition is greater than ${attributeGroups.count()}")
    }

    override fun getGroup(groupPosition: Int): AttributeGroup {
        return attributeGroups[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Attribute {
        return attributeGroups[groupPosition].attributes?.get(childPosition)
                ?: throw ArrayIndexOutOfBoundsException("$groupPosition is greater than ${attributeGroups.count()}")
    }

    override fun getGroupId(groupPosition: Int): Long {
        return getGroup(groupPosition).id.hashCode().toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return getChild(groupPosition, childPosition).id.hashCode().toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val groupView = convertView ?: activity.layoutInflater.inflate(R.layout.attribute_group_item, parent, false)
        val group = getGroup(groupPosition)

        val titleView = groupView.findViewById(R.id.title_text) as TextView
        titleView.text = group.name

        val shortDescView = groupView.findViewById(R.id.short_desc_text) as TextView
        shortDescView.text = group.id

        return groupView
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val childView = convertView ?: activity.layoutInflater.inflate(R.layout.attribute_item, parent, false)
        val child = getChild(groupPosition, childPosition)

        val titleView = childView.findViewById(R.id.title_text) as TextView
        titleView.text = child.title.orEmpty()

        val shortDeskView = childView.findViewById(R.id.short_desc_text) as TextView
        shortDeskView.text = child.descriptionShort.orEmpty()

        val iconView = childView.findViewById(R.id.logo_view) as ImageView
        val iconUrl = child.iconUrl
        if (iconUrl != null) {
            Picasso.get().load(iconUrl.replace(".svg", ".png")).into(iconView)
        } else {
            iconView.visibility = GONE
        }

        return childView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }


}


class AttributeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var icon: ImageView = itemView.findViewById(R.id.logo_view)
    var title: TextView = itemView.findViewById(R.id.title_text)
    var shortDesc: TextView = itemView.findViewById(R.id.short_desc_text)
}

