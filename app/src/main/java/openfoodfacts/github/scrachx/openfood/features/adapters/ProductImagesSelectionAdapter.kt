package openfoodfacts.github.scrachx.openfood.features.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.images.IMAGE_EDIT_SIZE_FILE
import openfoodfacts.github.scrachx.openfood.images.getImageUrl
import openfoodfacts.github.scrachx.openfood.models.Barcode

/**
 * Created by prajwalm on 10/09/18.
 */
class ProductImagesSelectionAdapter(
    private val context: Context,
    private val picasso: Picasso,
    private val images: List<String>,
    private val barcode: Barcode,
    private val onImageClick: ((Int) -> Unit)?
) : RecyclerView.Adapter<ProductImagesSelectionAdapter.CustomViewHolder>() {
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
        holder.itemView.setBackgroundColor(
            if (position == selectedPosition) ContextCompat.getColor(context, R.color.blue)
            else 0
        )
        val finalUrlString = getImageUrl(position)
        picasso.load(finalUrlString)
            .resize(400, 400)
            .centerInside()
            .into(holder.productImage)
    }

    override fun getItemCount() = images.size

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val productImage: ImageView = itemView.findViewById(R.id.img)

        override fun onClick(v: View) {
            if (selectedPosition >= 0) {
                notifyItemChanged(selectedPosition)
            }

            // If the user clicks on the same image -> deselect
            selectedPosition = if (bindingAdapterPosition != selectedPosition) bindingAdapterPosition else -1

            if (selectedPosition >= 0) {
                notifyItemChanged(selectedPosition)
            }
            onImageClick?.invoke(selectedPosition)
        }

        init {
            itemView.setOnClickListener(this)
        }
    }
}
