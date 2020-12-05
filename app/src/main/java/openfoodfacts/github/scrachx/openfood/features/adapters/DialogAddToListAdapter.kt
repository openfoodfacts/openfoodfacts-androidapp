package openfoodfacts.github.scrachx.openfood.features.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.adapters.DialogAddToListAdapter.ListViewHolder
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists
import openfoodfacts.github.scrachx.openfood.models.entities.YourListedProduct
import openfoodfacts.github.scrachx.openfood.utils.Utils.daoSession

//recyclerview adapter to display product lists in a dialog
class DialogAddToListAdapter(private val mContext: Context, private val productLists: List<ProductLists>,
                             private val barcode: String, private val productName: String, private val productDetails: String, private val imageUrl: String) : RecyclerView.Adapter<ListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.dialog_add_to_list_recycler_item, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val listName = productLists[position].listName
        holder.tvListTitle.text = listName
        holder.itemView.setOnClickListener {
            val listId = productLists[position].id
            val product = YourListedProduct().also {
                it.barcode = barcode
                it.listId = listId
                it.listName = listName
                it.productName = productName
                it.productDetails = productDetails
                it.imageUrl = imageUrl
            }
            daoSession.yourListedProductDao.insertOrReplace(product)
            mContext.startActivity(Intent(mContext, ProductListActivity::class.java).apply {
                putExtra("listName", listName)
                putExtra("listId", listId)
            })
        }
    }

    override fun getItemCount(): Int {
        return productLists.size
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvListTitle: TextView = itemView.findViewById(R.id.tvDialogListName)
    }
}