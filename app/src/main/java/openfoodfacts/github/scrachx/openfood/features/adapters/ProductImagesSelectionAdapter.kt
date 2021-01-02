package openfoodfacts.github.scrachx.openfood.features.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.images.IMAGE_EDIT_SIZE_FILE
import openfoodfacts.github.scrachx.openfood.images.getImageUrl
import openfoodfacts.github.scrachx.openfood.utils.Utils.picassoBuilder
import java.util.function.Consumer

/**
 * Created by prajwalm on 10/09/18.
 */
class ProductImagesSelectionAdapter(private val context: Context, private val images: List<String>, private val barcode: String, private val onImageClick: Consumer<Int>?) : RecyclerView.Adapter<ProductImagesSelectionAdapter.CustomViewHolder>() {
    var selectedPosition = -1
    fun isSelectionDone() = selectedPosition >= 0

    fun getSelectedImageName() = if (isSelectionDone()) images[selectedPosition] else null

    fun getImageUrl(position: Int): String {
        val imageName = images[position]
        return getImageUrl(barcode, imageName, IMAGE_EDIT_SIZE_FILE)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.image_selectable_item, parent, false))
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val finalUrlString = getImageUrl(position)
        val imageView = holder.productImage
        val viewGroup = holder.parent
        if (position == selectedPosition) {
            viewGroup.setBackgroundColor(ContextCompat.getColor(context, R.color.blue))
        } else {
            viewGroup.setBackgroundColor(0)
        }
        picassoBuilder(context).load(finalUrlString).resize(400, 400).centerInside().into(imageView)
    }

    override fun getItemCount() = images.size

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val parent: ViewGroup = itemView.findViewById(R.id.parentGroup)
        val productImage: ImageView = itemView.findViewById(R.id.img)

        override fun onClick(v: View) {
            if (selectedPosition >= 0) {
                notifyItemChanged(selectedPosition)
            }
            var adapterPosition = adapterPosition
            //if the user reclick on the same image -> deselect
            if (adapterPosition == selectedPosition) {
                adapterPosition = -1
            }
            selectedPosition = adapterPosition
            if (selectedPosition >= 0) {
                notifyItemChanged(selectedPosition)
            }
            onImageClick?.accept(selectedPosition)
        }

        init {
            itemView.setOnClickListener(this)
        }
    }
}