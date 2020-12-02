package openfoodfacts.github.scrachx.openfood.features.scanhistory

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity
import openfoodfacts.github.scrachx.openfood.models.HistoryItem
import openfoodfacts.github.scrachx.openfood.utils.Utils.getSmallImageGrade
import openfoodfacts.github.scrachx.openfood.utils.Utils.picassoBuilder
import java.util.*
import java.util.concurrent.TimeUnit

class ScanHistoryAdapter(
        private val context: Context,
        private val isLowBatteryMode: Boolean,
        private val list: MutableList<HistoryItem> = mutableListOf(),
) : RecyclerView.Adapter<ScanHistoryHolder>() {
    private val res: Resources = context.resources
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanHistoryHolder {
        //Inflate the layout, initialize the View Holder
        val v = LayoutInflater.from(parent.context).inflate(R.layout.history_list_item, parent, false)
        return ScanHistoryHolder(v, context)
    }

    override fun onBindViewHolder(holder: ScanHistoryHolder, position: Int) {

        val item = list[position]
        val productBrandsQuantityDetails = ProductListActivity.getProductBrandsQuantityDetails(item)

        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        holder.txtTitle.text = item.title
        holder.txtBarcode.text = item.barcode
        holder.txtProductDetails.text = productBrandsQuantityDetails
        when {
            AppFlavors.isFlavors(OPF, OPFF, OBF) -> {
                holder.imgNutritionGrade.visibility = View.GONE
            }
            getSmallImageGrade(item.nutritionGrade) != 0 -> {
                holder.imgNutritionGrade.setImageDrawable(ContextCompat.getDrawable(context, getSmallImageGrade(item.nutritionGrade)))
            }
            else -> {
                holder.imgNutritionGrade.visibility = View.INVISIBLE
            }
        }
        holder.historyImageProgressbar.visibility = if (item.url == null) View.GONE else View.VISIBLE

        // Load Image if isBatteryLoad is false
        if (!isLowBatteryMode) {
            picassoBuilder(context)
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
            holder.imgProduct.background = context.resources.getDrawable(R.drawable.placeholder_thumb)
            holder.historyImageProgressbar.visibility = View.INVISIBLE
        }
        val date = list[position].time
        calcTime(date, holder)
    }

    override fun getItemCount(): Int {
        //returns the number of elements the RecyclerView will display
        return list.size
    }

    // Insert a new item to the RecyclerView on a predefined position
    fun insert(position: Int, data: HistoryItem) {
        list.add(position, data)
        notifyItemInserted(position)
    }

    // Remove a RecyclerView item containing a specified Data object
    fun remove(data: HistoryItem) {
        val position = list.indexOf(data)
        list.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun calcTime(date: Date?, holder: ScanHistoryHolder) {
        val duration = Date().time - date!!.time
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val days = TimeUnit.MILLISECONDS.toDays(duration)
        when {
            seconds < 60 -> {
                holder.txtDate.text = res.getQuantityString(R.plurals.seconds, seconds.toInt(), seconds.toInt())
            }
            minutes < 60 -> {
                holder.txtDate.text = res.getQuantityString(R.plurals.minutes, minutes.toInt(), minutes.toInt())
            }
            hours < 24 -> {
                holder.txtDate.text = res.getQuantityString(R.plurals.hours, hours.toInt(), hours.toInt())
            }
            else -> {
                holder.txtDate.text = res.getQuantityString(R.plurals.days, days.toInt(), days.toInt())
            }
        }
    }

}