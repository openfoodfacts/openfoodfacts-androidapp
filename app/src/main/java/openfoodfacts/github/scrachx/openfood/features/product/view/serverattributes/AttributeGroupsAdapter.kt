package openfoodfacts.github.scrachx.openfood.features.product.view.serverattributes

import android.app.Activity
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.entities.attribute.AttributeGroup
import openfoodfacts.github.scrachx.openfood.utils.Utils.picassoBuilder


class AttributeGroupsAdapter(
        private val attributeGroups: List<AttributeGroup>,
        private val activity: Activity
) : BaseExpandableListAdapter() {
    override fun getGroupCount() = attributeGroups.count()

    override fun getChildrenCount(groupPosition: Int) = attributeGroups[groupPosition].attributes?.count()
            ?: throw ArrayIndexOutOfBoundsException("$groupPosition is greater than ${attributeGroups.count()}")

    override fun getGroup(groupPosition: Int) = attributeGroups[groupPosition]

    override fun getGroupId(groupPosition: Int) = getGroup(groupPosition).id.hashCode().toLong()

    override fun getChild(groupPosition: Int, childPosition: Int) = attributeGroups[groupPosition].attributes?.get(childPosition)
            ?: throw ArrayIndexOutOfBoundsException("$groupPosition is greater than ${attributeGroups.count()}")

    override fun getChildId(groupPosition: Int, childPosition: Int) =
            getChild(groupPosition, childPosition).id.hashCode().toLong()

    override fun hasStableIds() = false

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val groupView = convertView ?: activity.layoutInflater.inflate(R.layout.attribute_group_item, parent, false)
        val group = getGroup(groupPosition)

        groupView.findViewById<TextView>(R.id.title_text).text = group.name

        return groupView
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val childView = convertView ?: activity.layoutInflater.inflate(R.layout.attribute_item, parent, false)
        val attribute = getChild(groupPosition, childPosition)

        childView.findViewById<TextView>(R.id.title_text).text = attribute.title.orEmpty()

        childView.findViewById<TextView>(R.id.short_desc_text).text = attribute.descriptionShort.orEmpty()

        val iconView = childView.findViewById<ImageView>(R.id.logo_view)
        val iconUrl = attribute.iconUrl
        if (!iconUrl.isNullOrEmpty()) {
            picassoBuilder(activity)
                    .load(iconUrl.replaceAfterLast(".", "png"))
                    .into(iconView)
        } else {
            iconView.visibility = GONE
        }

        return childView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int) = true
}