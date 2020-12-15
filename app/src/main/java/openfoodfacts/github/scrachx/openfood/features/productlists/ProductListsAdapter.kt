package openfoodfacts.github.scrachx.openfood.features.productlists

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists

class ProductListsAdapter(
        internal val context: Context,
        private val productLists: MutableList<ProductLists>
) : RecyclerView.Adapter<ProductListsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.your_product_lists_item, parent, false)
        return ProductListsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductListsViewHolder, position: Int) {
        val listName = productLists[position].listName
        holder.tvListTitle.text = listName

        val numOfProducts = productLists[position].products.size
        productLists[position].numOfProducts = numOfProducts.toLong()
        holder.tvNumOfProducts.text = numOfProducts.toString()
    }

    override fun getItemCount() = productLists.size

    fun remove(data: ProductLists) {
        val position = productLists.indexOf(data)
        productLists.removeAt(position)
        notifyItemRemoved(position)
    }

}

class ProductListsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val itemCardView: CardView = itemView.findViewById(R.id.cvYourProductList)
    val tvListTitle: TextView = itemView.findViewById(R.id.tvProductListName)
    val tvNumOfProducts: TextView = itemView.findViewById(R.id.tvlistSize)
}