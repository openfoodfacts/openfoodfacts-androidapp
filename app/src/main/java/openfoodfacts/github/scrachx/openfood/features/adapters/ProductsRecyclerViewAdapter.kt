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
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.adapters.ProductsRecyclerViewAdapter.ProductsListViewHolder
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity.Companion.getProductBrandsQuantityDetails
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient.Companion.localeProductNameField
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import openfoodfacts.github.scrachx.openfood.utils.Utils.getSmallImageGrade
import openfoodfacts.github.scrachx.openfood.utils.Utils.picassoBuilder
import org.apache.commons.lang.StringUtils

/**
 * @author herau & itchix
 */
class ProductsRecyclerViewAdapter(
        private val products: List<Product?>,
        private val isLowBatteryMode: Boolean,
        private val context: Context
) : RecyclerView.Adapter<ProductsListViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsListViewHolder {
        val layoutResourceId = if (viewType == VIEW_ITEM) R.layout.products_list_item else R.layout.progressbar_endless_list
        val view = LayoutInflater.from(parent.context).inflate(layoutResourceId, parent, false)
        return if (viewType == VIEW_ITEM) {
            ProductViewHolder(view)
        } else {
            ProgressViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int) = if (products[position] == null) VIEW_LOAD else VIEW_ITEM

    override fun onBindViewHolder(holder: ProductsListViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if (holder !is ProductViewHolder) {
            return
        }
        holder.vProductImageProgressbar.visibility = View.VISIBLE
        val imageSmallUrl = products[position]!!.getImageSmallUrl(getLanguage(context))
        if (imageSmallUrl == null) {
            holder.vProductImageProgressbar.visibility = View.GONE
        }

        // Load Image if isLowBatteryMode is false
        if (!isLowBatteryMode) {
            picassoBuilder(context)
                    .load(imageSmallUrl)
                    .placeholder(R.drawable.placeholder_thumb)
                    .error(R.drawable.error_image)
                    .fit()
                    .centerCrop()
                    .into(holder.vProductImage, object : Callback {
                        override fun onSuccess() {
                            holder.vProductImageProgressbar.visibility = View.GONE
                        }

                        override fun onError(ex: Exception) {
                            holder.vProductImageProgressbar.visibility = View.GONE
                        }
                    })
        } else {
            Picasso.get().load(R.drawable.placeholder_thumb).into(holder.vProductImage)
            holder.vProductImageProgressbar.visibility = View.INVISIBLE
        }
        val product = products[position]
        holder.vProductName.text = product!!.productName
        val productNameInLocale = product.additionalProperties[localeProductNameField] as String?
        if (StringUtils.isNotBlank(productNameInLocale)) {
            holder.vProductName.text = productNameInLocale
        }
        val brandsQuantityDetails = getProductBrandsQuantityDetails(product)
        val gradeResource = getSmallImageGrade(product)
        if (gradeResource != 0) {
            holder.vProductGrade.visibility = View.VISIBLE
            holder.vProductGrade.setImageResource(gradeResource)
        } else {
            holder.vProductGrade.visibility = View.INVISIBLE
        }
        holder.vProductDetails.text = brandsQuantityDetails
    }

    fun getProduct(position: Int) = products[position]

    override fun getItemCount() = products.size

    abstract class ProductsListViewHolder(view: View) : RecyclerView.ViewHolder(view)

    /**
     * Provide a reference to the views for each data item
     * Complex data items may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder
     */
    internal class ProductViewHolder(view: View) : ProductsListViewHolder(view) {
        val vProductDetails: TextView = view.findViewById(R.id.productDetails)
        val vProductGrade: ImageView = view.findViewById(R.id.imgGrade)
        val vProductImage: ImageView = view.findViewById(R.id.imgProduct)
        val vProductImageProgressbar: ProgressBar = view.findViewById(R.id.searchImgProgressbar)

        val vProductName: TextView = view.findViewById(R.id.nameProduct)
    }

    internal class ProgressViewHolder(view: View) : ProductsListViewHolder(view)

    companion object {
        private const val VIEW_ITEM = 1
        private const val VIEW_LOAD = 0
    }
}