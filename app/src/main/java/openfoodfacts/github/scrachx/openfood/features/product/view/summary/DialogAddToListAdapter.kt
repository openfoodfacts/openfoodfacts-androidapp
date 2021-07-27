package openfoodfacts.github.scrachx.openfood.features.product.view.summary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.product.view.summary.DialogAddToListAdapter.ListViewHolder
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.entities.ListedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists

//recyclerview adapter to display product lists in a dialog
class DialogAddToListAdapter(
        private val context: Context,
        private val productLists: List<ProductLists>,
        private val barcode: String,
        private val productName: String,
        private val productDetails: String,
        private val imageUrl: String,
        private val daoSession: DaoSession
) : RecyclerView.Adapter<ListViewHolder>() {
    override fun getItemCount() = productLists.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_to_list_recycler_item, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val list = productLists[position]

        holder.tvListTitle.text = list.listName
        holder.itemView.setOnClickListener {
            val product = ListedProduct().also {
                it.barcode = barcode
                it.listId = list.id
                it.listName = list.listName
                it.productName = productName
                it.productDetails = productDetails
                it.imageUrl = imageUrl
            }
            daoSession.listedProductDao.insertOrReplace(product)
        }
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvListTitle: TextView = itemView.findViewById(R.id.tvDialogListName)
    }
}