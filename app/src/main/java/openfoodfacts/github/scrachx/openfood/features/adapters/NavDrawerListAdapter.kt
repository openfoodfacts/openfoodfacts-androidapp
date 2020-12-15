package openfoodfacts.github.scrachx.openfood.features.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.NavDrawerItem
import java.util.*

class NavDrawerListAdapter(private val context: Context, private val navDrawerItems: ArrayList<NavDrawerItem>) : BaseAdapter() {

    override fun getCount() = navDrawerItems.size

    override fun getItem(position: Int) = navDrawerItems[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val mInflater = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val newView = convertView ?: mInflater.inflate(R.layout.drawer_list_item, parent)
        val imgIcon = newView.findViewById<ImageView>(R.id.icon)
        val txtTitle = newView.findViewById<TextView>(R.id.title)
        imgIcon.setImageDrawable(AppCompatResources.getDrawable(context, navDrawerItems[position].icon))
        txtTitle.text = navDrawerItems[position].title
        return newView
    }
}