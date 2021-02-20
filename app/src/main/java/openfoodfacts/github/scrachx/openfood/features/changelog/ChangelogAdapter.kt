package openfoodfacts.github.scrachx.openfood.features.changelog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R

class ChangelogAdapter(private val items: List<ChangelogListItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 1
        private const val VIEW_TYPE_ITEM = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(inflater.inflate(R.layout.view_changelog_item_header, parent, false))
            VIEW_TYPE_ITEM -> ItemViewHolder(inflater.inflate(R.layout.view_changelog_item, parent, false))
            else -> throw IllegalStateException("Unexpected value: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_HEADER -> (holder as HeaderViewHolder).bind((items[position] as ChangelogListItem.Header))
            VIEW_TYPE_ITEM -> (holder as ItemViewHolder).bind((items[position] as ChangelogListItem.Item))
            else -> throw IllegalStateException("Unexpected value: " + holder.itemViewType)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is ChangelogListItem.Header) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }

    private class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val versionLabel: TextView = view.findViewById(R.id.changelog_list_header_version)
        private val dateLabel: TextView = view.findViewById(R.id.changelog_list_header_date)

        fun bind(item: ChangelogListItem.Header) {
            versionLabel.text = item.version
            dateLabel.text = item.date
        }
    }

    private class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val itemLabel: TextView = itemView.findViewById(R.id.changelog_list_item_label)

        fun bind(item: ChangelogListItem.Item) {
            itemLabel.text = item.description
        }
    }
}
