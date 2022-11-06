package openfoodfacts.github.scrachx.openfood.features.productlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.YourListedProductsItemBinding
import openfoodfacts.github.scrachx.openfood.models.entities.ListedProduct
import openfoodfacts.github.scrachx.openfood.utils.into
import openfoodfacts.github.scrachx.openfood.utils.shouldLoadImages

class ProductListAdapter(
    private val context: Context,
    private val picasso: Picasso,
) : RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {

    var products = mutableListOf<ListedProduct>()
    var onItemClickListener: (ListedProduct) -> Unit = { }

    private val shouldLoadImages by lazy { context.shouldLoadImages() }

    override fun getItemCount() = products.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val inflater = LayoutInflater.from(context)
        return ProductViewHolder(YourListedProductsItemBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        val binding = holder.binding

        binding.titleYourListedProduct.text = product.productName
        binding.productDetailsYourListedProduct.text = product.productDetails
        binding.barcodeYourListedProduct.text = product.barcode

        binding.imageProgressbarYourListedProduct.visibility = View.VISIBLE


        if (shouldLoadImages && !product.imageUrl.isNullOrEmpty()) {
            picasso
                .load(product.imageUrl)
                .placeholder(R.drawable.placeholder_thumb)
                .error(R.drawable.ic_no_red_24dp)
                .fit()
                .centerCrop()
                .into(binding.imgProductYourListedProduct,
                    onSuccess = {
                        binding.imageProgressbarYourListedProduct.visibility = View.GONE
                    }, onError = {
                        binding.imageProgressbarYourListedProduct.visibility = View.GONE
                    }
                )
        } else {
            binding.imgProductYourListedProduct.background = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.placeholder_thumb,
                context.theme
            )
            binding.imageProgressbarYourListedProduct.visibility = View.INVISIBLE
        }

        // Set on click listener
        binding.root.setOnClickListener { onItemClickListener(product) }
    }

    fun remove(product: ListedProduct) {
        val position = products.indexOf(product)
        products.remove(product)
        notifyItemRemoved(position)
    }


    class ProductViewHolder(val binding: YourListedProductsItemBinding) : RecyclerView.ViewHolder(binding.root)
}