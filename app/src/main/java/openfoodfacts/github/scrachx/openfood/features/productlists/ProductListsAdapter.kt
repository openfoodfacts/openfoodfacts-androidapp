package openfoodfacts.github.scrachx.openfood.features.productlists

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists

class ProductListsAdapter(
        private val activity: Activity,
        private val productLists: MutableList<ProductLists>
) : RecyclerView.Adapter<ProductListsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListsViewHolder {
        val view = activity.layoutInflater.inflate(R.layout.your_product_lists_item, parent, false)
        return ProductListsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductListsViewHolder, position: Int) {
        val listName = productLists[position].listName
        holder.tvListTitle.text = listName

        val numOfProducts = productLists[position].products.size
        holder.tvNumOfProducts.text = productLists[position].numOfProducts.toString()

        productLists[position].numOfProducts = numOfProducts.toLong()

        val listId = productLists[position].id
    }

    override fun getItemCount(): Int {
        return productLists.size
    }

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