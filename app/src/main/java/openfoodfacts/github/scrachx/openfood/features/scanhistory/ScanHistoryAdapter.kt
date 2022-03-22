package openfoodfacts.github.scrachx.openfood.features.scanhistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.HistoryListItemBinding
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct
import openfoodfacts.github.scrachx.openfood.models.getProductBrandsQuantityDetails
import openfoodfacts.github.scrachx.openfood.utils.*
import kotlin.properties.Delegates

/**
 * @param isLowBatteryMode determine if image should be loaded or not
 */
class ScanHistoryAdapter(
    private val isLowBatteryMode: Boolean,
    private val picasso: Picasso,
    private val onItemClicked: (HistoryProduct) -> Unit
) : RecyclerView.Adapter<ScanHistoryAdapter.ViewHolder>(), AutoUpdatableAdapter {

    var products: List<HistoryProduct> by Delegates.observable(emptyList()) { _, oldList, newList ->
        autoNotify(oldList, newList) { o, n -> o.barcode == n.barcode }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = HistoryListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view, isLowBatteryMode, picasso)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val historyProduct = products[position]
        viewHolder.bind(historyProduct) {
            onItemClicked(it)
        }
    }

    override fun getItemCount() = products.size

    override fun onViewRecycled(viewHolder: ViewHolder) {
        viewHolder.unbind()
        super.onViewRecycled(viewHolder)
    }

    class ViewHolder(
        val binding: HistoryListItemBinding,
        val isLowBatteryMode: Boolean,
        private val picasso: Picasso
    ) : RecyclerView.ViewHolder(binding.root) {

        private val context = binding.root.context

        fun bind(product: HistoryProduct, onClicked: (HistoryProduct) -> Unit) {
            binding.productName.text = product.title
            binding.barcode.text = product.barcode
            binding.productDetails.text = product.getProductBrandsQuantityDetails()

            // Load Image if isBatteryLoad is false
            if (!isLowBatteryMode && product.url != null) {
                binding.imgProgress.isVisible = true
                picasso
                    .load(product.url)
                    .placeholder(R.drawable.placeholder_thumb)
                    .error(R.drawable.ic_no_red_24dp)
                    .fit()
                    .centerCrop()
                    .into(binding.productImage, object : Callback {
                        override fun onSuccess() {
                            binding.imgProgress.isVisible = false
                        }

                        override fun onError(ex: Exception) {
                            binding.imgProgress.isVisible = false
                        }
                    })
            } else {
                binding.productImage.setImageResource(R.drawable.placeholder_thumb)
                binding.imgProgress.isVisible = false
            }
            binding.lastScan.text = product.lastSeen.durationToNowFormatted(context)

            if (isFlavors(OFF)) {
                binding.nutriscore.setImageResource(product.getNutriScoreResource())
                binding.ecoscore.setImageResource(product.getEcoscoreResource())
                binding.novaGroup.setImageResource(product.getNovaGroupResource())
            }

            binding.root.setOnClickListener { onClicked(product) }
        }

        fun unbind() {
            binding.root.setOnClickListener(null)
        }

    }
}
