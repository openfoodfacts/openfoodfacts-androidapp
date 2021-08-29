package openfoodfacts.github.scrachx.openfood.features.productlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListAdapter.ProductViewHolder
import openfoodfacts.github.scrachx.openfood.features.shared.views.CustomTextView
import openfoodfacts.github.scrachx.openfood.models.entities.ListedProduct

class ProductListAdapter(
    private val context: Context,
    val products: MutableList<ListedProduct>,
    private val isLowBatteryMode: Boolean,
    private val picasso: Picasso,
    private val onItemClickListener: (ListedProduct) -> Unit = { }
) : RecyclerView.Adapter<ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ProductViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.your_listed_products_item, parent, false)
        )

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.imgProgressBar.visibility = View.VISIBLE

        holder.tvTitle.text = products[position].productName
        holder.tvDetails.text = products[position].productDetails
        holder.tvBarcode.text = products[position].barcode

        if (!isLowBatteryMode && products[position].imageUrl.isNotEmpty()) {
            picasso
                .load(products[position].imageUrl)
                .placeholder(R.drawable.placeholder_thumb)
                .error(R.drawable.ic_no_red_24dp)
                .fit()
                .centerCrop()
                .into(holder.imgProduct, object : Callback {
                    override fun onSuccess() {
                        holder.imgProgressBar.visibility = View.GONE
                    }

                    override fun onError(ex: Exception) {
                        holder.imgProgressBar.visibility = View.GONE
                    }
                })
        } else {
            holder.imgProduct.background = ResourcesCompat.getDrawable(context.resources, R.drawable.placeholder_thumb, context.theme)
            holder.imgProgressBar.visibility = View.INVISIBLE
        }

        // Set on click listener
        holder.itemView.setOnClickListener { onItemClickListener(products[position]) }
    }

    fun remove(product: ListedProduct) {
        val position = products.indexOf(product)
        products.remove(product)
        notifyItemRemoved(position)
    }

    override fun getItemCount() = products.size

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: AppCompatImageView = itemView.findViewById(R.id.imgProductYourListedProduct)
        val imgProgressBar: ProgressBar = itemView.findViewById(R.id.imageProgressbarYourListedProduct)
        val tvBarcode: CustomTextView = itemView.findViewById(R.id.barcodeYourListedProduct)
        val tvDetails: TextView = itemView.findViewById(R.id.productDetailsYourListedProduct)
        val tvTitle: TextView = itemView.findViewById(R.id.titleYourListedProduct)
    }
}