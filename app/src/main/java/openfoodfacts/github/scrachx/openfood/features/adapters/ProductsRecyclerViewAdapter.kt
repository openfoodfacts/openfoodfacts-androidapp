package openfoodfacts.github.scrachx.openfood.features.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient.Companion.getLocaleProductNameField
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import openfoodfacts.github.scrachx.openfood.utils.Utils.picassoBuilder
import openfoodfacts.github.scrachx.openfood.utils.getEcoscoreResource
import openfoodfacts.github.scrachx.openfood.utils.getNovaGroupResource
import openfoodfacts.github.scrachx.openfood.utils.getNutriScoreResource
import openfoodfacts.github.scrachx.openfood.utils.getProductBrandsQuantityDetails

/**
 * @author herau & itchix
 */
class ProductsRecyclerViewAdapter(
        val products: MutableList<Product?>,
        private val isLowBatteryMode: Boolean,
        private val context: Context
) : RecyclerView.Adapter<ProductsRecyclerViewAdapter.ProductsListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsListViewHolder {
        val layoutResourceId = if (viewType == VIEW_ITEM) R.layout.products_list_item else R.layout.progressbar_endless_list
        val view = LayoutInflater.from(parent.context).inflate(layoutResourceId, parent, false)
        return if (viewType == VIEW_ITEM) ProductsListViewHolder.ProductViewHolder(view) else ProductsListViewHolder.ProgressViewHolder(view)
    }

    override fun getItemViewType(position: Int) = if (products[position] == null) VIEW_LOAD else VIEW_ITEM

    override fun onBindViewHolder(holder: ProductsListViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if (holder !is ProductsListViewHolder.ProductViewHolder) return
        holder.imageProgress.visibility = View.VISIBLE
        val imageSmallUrl = products[position]!!.getImageSmallUrl(getLanguage(context))
        if (imageSmallUrl == null) {
            holder.imageProgress.visibility = View.GONE
        }

        // Load Image if isLowBatteryMode is false
        if (!isLowBatteryMode) {
            picassoBuilder(context)
                    .load(imageSmallUrl)
                    .placeholder(R.drawable.placeholder_thumb)
                    .error(R.drawable.error_image)
                    .fit()
                    .centerCrop()
                    .into(holder.productFrontImg, object : Callback {
                        override fun onSuccess() {
                            holder.imageProgress.visibility = View.GONE
                        }

                        override fun onError(ex: Exception) {
                            holder.imageProgress.visibility = View.GONE
                        }
                    })
        } else {
            Picasso.get().load(R.drawable.placeholder_thumb).into(holder.productFrontImg)
            holder.imageProgress.visibility = View.INVISIBLE
        }
        val product = products[position]

        // Set product name
        holder.productName.text = product?.productName ?: context.getString(R.string.productNameNull)
        val productNameInLocale = product?.additionalProperties?.get(getLocaleProductNameField()) as String?
        if (!productNameInLocale.isNullOrBlank()) {
            holder.productName.text = productNameInLocale
        }

        // Set product description
        holder.productDetails.text = product?.getProductBrandsQuantityDetails()

        if (AppFlavors.isFlavors(AppFlavors.OFF)) {
            // Set nutriscore icon
            val nutriRes = product.getNutriScoreResource()
            holder.productNutriscore.setImageResource(nutriRes)
            holder.productNutriscore.visibility = View.VISIBLE

            // Set nova icon
            val novaRes = product.getNovaGroupResource()
            holder.productNova.setImageResource(novaRes)
            holder.productNova.visibility = View.VISIBLE

            // Set ecoscore icon
            val ecoRes = product.getEcoscoreResource()
            holder.productEcoscore.setImageResource(ecoRes)
            holder.productEcoscore.visibility = View.VISIBLE
        }
    }

    fun getProduct(position: Int) = products[position]

    override fun getItemCount() = products.size

    companion object {
        private const val VIEW_ITEM = 1
        private const val VIEW_LOAD = 0
    }

    sealed class ProductsListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal class ProgressViewHolder(view: View) : ProductsListViewHolder(view)

        /**
         * Provide a reference to the views for each data item
         * Complex data items may need more than one view per item, and
         * you provide access to all the views for a data item in a view holder
         */
        internal class ProductViewHolder(view: View) : ProductsListViewHolder(view) {
            val productName: TextView = view.findViewById(R.id.nameProduct)
            val productDetails: TextView = view.findViewById(R.id.productDetails)
            val productNutriscore: ImageView = view.findViewById(R.id.imgNutriscore)
            val productFrontImg: ImageView = view.findViewById(R.id.imgProduct)
            val imageProgress: ProgressBar = view.findViewById(R.id.searchImgProgressbar)
            val productEcoscore: ImageView = view.findViewById(R.id.imgEcoscore)
            val productNova: ImageView = view.findViewById(R.id.imgNova)
        }
    }
}



