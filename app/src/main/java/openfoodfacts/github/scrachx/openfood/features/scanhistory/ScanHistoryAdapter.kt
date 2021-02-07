package openfoodfacts.github.scrachx.openfood.features.scanhistory

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.HistoryListItemBinding
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity.Companion.getProductBrandsQuantityDetails
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.Utils.picassoBuilder
import openfoodfacts.github.scrachx.openfood.utils.getEcoscoreResource
import openfoodfacts.github.scrachx.openfood.utils.getNovaGroupResource
import openfoodfacts.github.scrachx.openfood.utils.getNutriScoreResource
import java.util.*
import java.util.concurrent.TimeUnit

class ScanHistoryAdapter(
        private val activity: Activity,
        private val isLowBatteryMode: Boolean,
        val products: MutableList<HistoryProduct>,
) : RecyclerView.Adapter<ScanHistoryHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanHistoryHolder {
        val view = HistoryListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScanHistoryHolder(view)
    }

    private fun HistoryProduct.getProductBrandsQuantityDetails() = getProductBrandsQuantityDetails(brands, quantity)

    override fun onBindViewHolder(holder: ScanHistoryHolder, position: Int) {

        val hProduct = products[position]
        val productBrandsQuantityDetails = hProduct.getProductBrandsQuantityDetails()

        // Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        holder.binding.productName.text = hProduct.title
        holder.binding.barcode.text = hProduct.barcode
        holder.binding.productDetails.text = productBrandsQuantityDetails
        holder.binding.imgProgress.visibility = if (hProduct.url == null) View.GONE else View.VISIBLE

        // Load Image if isBatteryLoad is false
        if (!isLowBatteryMode) {
            picassoBuilder(activity)
                    .load(hProduct.url)
                    .placeholder(R.drawable.placeholder_thumb)
                    .error(R.drawable.ic_no_red_24dp)
                    .fit()
                    .centerCrop()
                    .into(holder.binding.productImage, object : Callback {
                        override fun onSuccess() {
                            holder.binding.imgProgress.visibility = View.GONE
                        }

                        override fun onError(ex: Exception) {
                            holder.binding.imgProgress.visibility = View.GONE
                        }
                    })
        } else {
            holder.binding.productImage.background = ContextCompat.getDrawable(activity, R.drawable.placeholder_thumb)
            holder.binding.imgProgress.visibility = View.INVISIBLE
        }
        holder.binding.lastScan.text = calcTime(hProduct.lastSeen)

        if (isFlavors(OFF)) {
            holder.binding.nutriscore.setImageResource(hProduct.getNutriScoreResource())
            holder.binding.ecoscore.setImageResource(hProduct.getEcoscoreResource())
            holder.binding.novaGroup.setImageResource(hProduct.getNovaGroupResource())
        }

        holder.binding.root.setOnClickListener {
            OpenFoodAPIClient(activity).openProduct(hProduct.barcode, activity)
        }
    }

    override fun getItemCount() = products.size

    // Insert a new item to the RecyclerView on a predefined position
    fun insert(position: Int, data: HistoryProduct) {
        products.add(position, data)
        notifyItemInserted(position)
    }

    // Remove a RecyclerView item containing a specified Data object
    fun removeAndNotify(data: HistoryProduct) {
        val position = products.indexOf(data)
        products.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun calcTime(date: Date): String {
        val duration = Date().time - date.time
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val days = TimeUnit.MILLISECONDS.toDays(duration)
        return when {
            seconds < 60 -> activity.resources.getQuantityString(R.plurals.seconds, seconds.toInt(), seconds.toInt())
            minutes < 60 -> activity.resources.getQuantityString(R.plurals.minutes, minutes.toInt(), minutes.toInt())
            hours < 24 -> activity.resources.getQuantityString(R.plurals.hours, hours.toInt(), hours.toInt())
            else -> activity.resources.getQuantityString(R.plurals.days, days.toInt(), days.toInt())
        }
    }

}