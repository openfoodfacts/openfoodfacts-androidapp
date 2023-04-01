package openfoodfacts.github.scrachx.openfood.features.scanhistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import openfoodfacts.github.scrachx.openfood.AppFlavor.Companion.isFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavor.OFF
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.HistoryListItemBinding
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct
import openfoodfacts.github.scrachx.openfood.utils.AutoUpdatableAdapter
import openfoodfacts.github.scrachx.openfood.utils.durationToNowFormatted
import openfoodfacts.github.scrachx.openfood.utils.getEcoscoreResource
import openfoodfacts.github.scrachx.openfood.utils.getNovaGroupResource
import openfoodfacts.github.scrachx.openfood.utils.getNutriScoreResource
import openfoodfacts.github.scrachx.openfood.utils.into
import java.util.Locale

/**
 * @param shouldLoadImages determine if image should be loaded or not
 */
class ScanHistoryAdapter(
    private val shouldLoadImages: Boolean,
    private val picasso: Picasso,
    private val onItemClicked: (HistoryProduct) -> Unit,
) : RecyclerView.Adapter<ScanHistoryAdapter.Companion.ViewHolder>(), AutoUpdatableAdapter {

    var products: List<HistoryProduct> by autoNotifying(comparator = compareBy { it.barcode })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = HistoryListItemBinding.inflate(inflater, parent, false)
        return ViewHolder(view, shouldLoadImages, picasso)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val historyProduct = products[position]
        viewHolder.bind(historyProduct, onItemClicked)
    }

    override fun getItemCount() = products.size

    override fun onViewRecycled(viewHolder: ViewHolder) {
        viewHolder.unbind()
        super.onViewRecycled(viewHolder)
    }

    companion object {
        fun getProductBrandsQuantityDetails(historyProduct: HistoryProduct): String {
            return buildString {
                val brands = historyProduct.brands
                if (!brands.isNullOrEmpty()) {
                    append(brands.split(",").first().trim { it <= ' ' }
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() })
                }
                val quantity = historyProduct.quantity
                if (!quantity.isNullOrEmpty()) {
                    append(" - ")
                    append(quantity)
                }
            }
        }

        class ViewHolder(
            private val binding: HistoryListItemBinding,
            private val shouldLoadImages: Boolean,
            private val picasso: Picasso,
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(product: HistoryProduct, onClicked: (HistoryProduct) -> Unit) {
                binding.productName.text = product.title
                binding.barcode.text = product.barcode
                binding.productDetails.text = getProductBrandsQuantityDetails(product)

                if (shouldLoadImages && product.url != null) {
                    binding.imgProgress.isVisible = true
                    picasso
                        .load(product.url)
                        .placeholder(R.drawable.placeholder_thumb)
                        .error(R.drawable.ic_no_red_24dp)
                        .fit()
                        .centerCrop()
                        .into(
                            binding.productImage,
                            onSuccess = { binding.imgProgress.isVisible = false },
                            onError = { binding.imgProgress.isVisible = false },
                        )
                } else {
                    binding.productImage.setImageResource(R.drawable.placeholder_thumb)
                    binding.imgProgress.isVisible = false
                }
                val context = binding.root.context
                binding.lastScan.text = product.lastSeen.durationToNowFormatted(context)

                if (isFlavors(OFF)) {
                    binding.nutriscore.setImageResource(product.getNutriScoreResource())
                    binding.ecoscore.setImageResource(product.getEcoscoreResource())
                    binding.novaGroup.setImageResource(product.getNovaGroupResource())
                }

                itemView.setOnClickListener { onClicked(product) }
            }

            fun unbind() {
                itemView.setOnClickListener(null)
            }

        }
    }
}
