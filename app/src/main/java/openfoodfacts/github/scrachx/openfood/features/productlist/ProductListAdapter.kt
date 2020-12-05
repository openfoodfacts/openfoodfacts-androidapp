package openfoodfacts.github.scrachx.openfood.features.productlist

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListAdapter.YourListProductsViewHolder
import openfoodfacts.github.scrachx.openfood.models.entities.YourListedProduct
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.CustomTextView

class ProductListAdapter(
        private val context: Context,
        private val products: MutableList<YourListedProduct>,
        private val isLowBatteryMode: Boolean
) : RecyclerView.Adapter<YourListProductsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YourListProductsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.your_listed_products_item, parent, false)
        return YourListProductsViewHolder(view)
    }

    override fun onBindViewHolder(holder: YourListProductsViewHolder, position: Int) {
        holder.imgProgressBar.visibility = View.VISIBLE
        val productName = products[position].productName
        val barcode = products[position].barcode
        holder.tvTitle.text = productName
        holder.tvDetails.text = products[position].productDetails
        holder.tvBarcode.text = barcode
        if (!isLowBatteryMode) {
            Picasso.get()
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
            holder.imgProduct.background = context.resources.getDrawable(R.drawable.placeholder_thumb)
            holder.imgProgressBar.visibility = View.INVISIBLE
        }
        holder.itemView.setOnClickListener { v: View ->
            val cm = v.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
            if (isConnected) {
                val api = OpenFoodAPIClient(v.context)
                api.openProduct(barcode, (v.context as Activity))
            }
        }
    }

    fun remove(data: YourListedProduct) {
        val position = products.indexOf(data)
        products.remove(data)
        notifyItemRemoved(position)
    }

    override fun getItemCount() = products.size

    class YourListProductsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: AppCompatImageView = itemView.findViewById(R.id.imgProductYourListedProduct)
        val imgProgressBar: ProgressBar = itemView.findViewById(R.id.imageProgressbarYourListedProduct)
        val tvBarcode: CustomTextView = itemView.findViewById(R.id.barcodeYourListedProduct)
        val tvDetails: TextView = itemView.findViewById(R.id.productDetailsYourListedProduct)
        val tvTitle: TextView = itemView.findViewById(R.id.titleYourListedProduct)
    }
}