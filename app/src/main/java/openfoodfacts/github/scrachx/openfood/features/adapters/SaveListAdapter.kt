package openfoodfacts.github.scrachx.openfood.features.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.adapters.SaveListAdapter.SaveViewHolder
import openfoodfacts.github.scrachx.openfood.models.SaveItem
import openfoodfacts.github.scrachx.openfood.utils.LOCALE_FILE_SCHEME

class SaveListAdapter(private val context: Context, private val saveItems: List<SaveItem>, private val mSaveClickInterface: SaveClickInterface) : RecyclerView.Adapter<SaveViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaveViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.save_list_item, parent, false)
        return SaveViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SaveViewHolder, position: Int) {
        val (title, fieldsCompleted, url, barcode, weight, brand) = saveItems[position]
        when {
            fieldsCompleted < 10 -> {
                holder.percentageCompleted.progressDrawable.setColorFilter(
                        Color.RED, PorterDuff.Mode.MULTIPLY)
            }
            fieldsCompleted in 10..18 -> {
                holder.percentageCompleted.progressDrawable.setColorFilter(
                        Color.YELLOW, PorterDuff.Mode.MULTIPLY)
            }
            else -> {
                holder.percentageCompleted.progressDrawable.setColorFilter(
                        Color.GREEN, PorterDuff.Mode.MULTIPLY)
            }
        }
        holder.percentageCompleted.progress = fieldsCompleted
        var percentageValue = fieldsCompleted * 100 / 25
        if (percentageValue > 100) {
            percentageValue = 100
        }
        holder.txtPercentage.text = context.getString(R.string.percent, percentageValue)
        if (isUploading) {
            holder.percentageCompleted.visibility = View.GONE
            holder.progressBar.visibility = View.VISIBLE
        }
        holder.txtTitle.text = title
        Picasso.get().load(LOCALE_FILE_SCHEME + url).config(Bitmap.Config.RGB_565).into(holder.imgProduct)
        holder.txtBarcode.text = barcode
        holder.txtWeight.text = weight
        holder.txtBrand.text = brand
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount() = saveItems.size

    interface SaveClickInterface {
        fun onClick(position: Int)
        fun onLongClick(position: Int)
    }

    inner class SaveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, OnLongClickListener {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgSaveProduct)
        val percentageCompleted: ProgressBar = itemView.findViewById(R.id.percentage_completed)
        val progressBar: ProgressBar = itemView.findViewById(R.id.offlineUploadProgressBar)
        val txtBarcode: TextView = itemView.findViewById(R.id.barcodeSave)
        val txtBrand: TextView = itemView.findViewById(R.id.offlineBrand)
        val txtPercentage: TextView = itemView.findViewById(R.id.txt_percentage)
        val txtTitle: TextView = itemView.findViewById(R.id.titleSave)
        val txtWeight: TextView = itemView.findViewById(R.id.offlineWeight)

        override fun onClick(view: View) = mSaveClickInterface.onClick(adapterPosition)

        override fun onLongClick(view: View): Boolean {
            mSaveClickInterface.onLongClick(adapterPosition)
            return true
        }

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }
    }

    companion object {
        private var isUploading = false
        fun showProgressDialog() {
            isUploading = true
        }
    }
}