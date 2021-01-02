package openfoodfacts.github.scrachx.openfood.features.scanhistory

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct
import openfoodfacts.github.scrachx.openfood.utils.Utils.picassoBuilder
import openfoodfacts.github.scrachx.openfood.utils.getNutriScoreSmallDrawable
import java.util.*
import java.util.concurrent.TimeUnit

class ScanHistoryAdapter(
        internal val activity: Activity,
        private val isLowBatteryMode: Boolean,
        val products: MutableList<HistoryProduct>,
) : RecyclerView.Adapter<ScanHistoryHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanHistoryHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_list_item, parent, false)
        return ScanHistoryHolder(view, activity)
    }

    override fun onBindViewHolder(holder: ScanHistoryHolder, position: Int) {

        val item = products[position]
        val productBrandsQuantityDetails = ProductListActivity.getProductBrandsQuantityDetails(item)

        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        holder.txtTitle.text = item.title
        holder.txtBarcode.text = item.barcode
        holder.txtProductDetails.text = productBrandsQuantityDetails
        when {
            isFlavors(OPF, OPFF, OBF) -> {
                holder.imgNutritionGrade.visibility = View.GONE
            }
            getNutriScoreSmallDrawable(item.nutritionGrade) != 0 -> {
                holder.imgNutritionGrade.setImageDrawable(ContextCompat.getDrawable(activity, getNutriScoreSmallDrawable(item.nutritionGrade)))
            }
            else -> {
                holder.imgNutritionGrade.visibility = View.INVISIBLE
            }
        }
        holder.historyImageProgressbar.visibility = if (item.url == null) View.GONE else View.VISIBLE

        // Load Image if isBatteryLoad is false
        if (!isLowBatteryMode) {
            picassoBuilder(activity)
                    .load(item.url)
                    .placeholder(R.drawable.placeholder_thumb)
                    .error(R.drawable.ic_no_red_24dp)
                    .fit()
                    .centerCrop()
                    .into(holder.imgProduct, object : Callback {
                        override fun onSuccess() {
                            holder.historyImageProgressbar.visibility = View.GONE
                        }
                        override fun onError(ex: Exception) {
                            holder.historyImageProgressbar.visibility = View.GONE
                        }
                    })
        } else {
            holder.imgProduct.background = ContextCompat.getDrawable(activity, R.drawable.placeholder_thumb)
            holder.historyImageProgressbar.visibility = View.INVISIBLE
        }
        val date = products[position].lastSeen
        calcTime(date, holder)
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

    private fun calcTime(date: Date, holder: ScanHistoryHolder) {
        val duration = Date().time - date.time
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val days = TimeUnit.MILLISECONDS.toDays(duration)
        holder.txtDate.text = when {
            seconds < 60 -> activity.resources.getQuantityString(R.plurals.seconds, seconds.toInt(), seconds.toInt())
            minutes < 60 -> activity.resources.getQuantityString(R.plurals.minutes, minutes.toInt(), minutes.toInt())
            hours < 24 -> activity.resources.getQuantityString(R.plurals.hours, hours.toInt(), hours.toInt())
            else -> activity.resources.getQuantityString(R.plurals.days, days.toInt(), days.toInt())
        }
    }

}