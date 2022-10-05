package openfoodfacts.github.scrachx.openfood.features.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ImageSelectableItemBinding
import openfoodfacts.github.scrachx.openfood.images.IMAGE_EDIT_SIZE
import openfoodfacts.github.scrachx.openfood.images.getImageUrl

/**
 * Created by prajwalm on 10/09/18.
 */
class ProductImagesSelectionAdapter(
    private val picasso: Picasso,
    private val images: List<String>,
    private val barcode: String,
    private val onImageClick: ((Int) -> Unit)?,
) : RecyclerView.Adapter<ProductImagesSelectionAdapter.ProductImageSelectionAdapter>() {

    var selectedPosition = -1

    fun isSelectionDone() = selectedPosition >= 0
    fun getSelectedImageName() = if (!isSelectionDone()) null else images[selectedPosition]

    fun getImageUrl(position: Int): String {
        val imageName = images[position]
        return getImageUrl(barcode, imageName, IMAGE_EDIT_SIZE)
    }

    override fun getItemCount() = images.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductImageSelectionAdapter {
        val binding = ImageSelectableItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductImageSelectionAdapter(binding)
    }

    override fun onBindViewHolder(holder: ProductImageSelectionAdapter, position: Int) {
        val binding = holder.binding

        val blue = ContextCompat.getColor(binding.root.context, R.color.blue)
        binding.root.setBackgroundColor(if (position == selectedPosition) blue else 0)

        val imageUrl = getImageUrl(position)
        picasso.load(imageUrl)
            .resize(400, 400)
            .centerInside()
            .into(binding.imageView)
    }


    inner class ProductImageSelectionAdapter(
        val binding: ImageSelectableItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun onClick() {
            if (selectedPosition >= 0) {
                notifyItemChanged(selectedPosition)
            }

            // If the user reclick on the same image -> deselect
            selectedPosition = bindingAdapterPosition.takeUnless { it == selectedPosition } ?: -1

            if (selectedPosition >= 0) {
                notifyItemChanged(selectedPosition)
            }
            onImageClick?.invoke(selectedPosition)
        }

        init {
            itemView.setOnClickListener { onClick() }
        }
    }
}
