package openfoodfacts.github.scrachx.openfood.utils

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty

interface AutoUpdatableAdapter {

    fun <T> RecyclerView.Adapter<*>.autoNotify(
        old: List<T>,
        new: List<T>,
        comparator: Comparator<T>,
    ) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return comparator.compare(old[oldItemPosition], new[newItemPosition]) == 0
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return old[oldItemPosition] == new[newItemPosition]
            }

            override fun getOldListSize() = old.size

            override fun getNewListSize() = new.size
        })

        diff.dispatchUpdatesTo(this)
    }

    fun <T> RecyclerView.Adapter<*>.autoNotifying(
        initialValue: List<T> = emptyList(),
        comparator: Comparator<T>,
    ): ReadWriteProperty<RecyclerView.Adapter<*>, List<T>> {
        return Delegates.observable(initialValue) { _, oldList, newList ->
            autoNotify(oldList, newList, comparator)
        }
    }
}
