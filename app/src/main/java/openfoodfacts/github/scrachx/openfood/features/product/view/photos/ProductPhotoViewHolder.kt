package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import logcat.LogPriority
import logcat.logcat
import openfoodfacts.github.scrachx.openfood.databinding.ImagesItemBinding
import openfoodfacts.github.scrachx.openfood.images.IMAGE_EDIT_SIZE
import openfoodfacts.github.scrachx.openfood.images.getImageUrl

class ProductPhotoViewHolder(
    private val binding: ImagesItemBinding,
    private val picasso: Picasso,
) : RecyclerView.ViewHolder(binding.root) {

    fun setImage(barcode: String, imageName: String) {
        val imageUrl = getImageUrl(barcode, imageName, IMAGE_EDIT_SIZE)
        logcat(LogPriority.DEBUG) { "Loading image $imageUrl..." }
        picasso.load(imageUrl)
            .resize(IMAGE_SIZE, IMAGE_SIZE)
            .centerInside()
            .into(binding.imageView)
    }

    fun setOnClickListener(listener: (Int) -> Unit) =
        binding.imageView.setOnClickListener { listener(bindingAdapterPosition) }

    fun setOnEditClickListener(listener: (Int) -> Unit) =
        binding.editBtn.setOnClickListener { listener(bindingAdapterPosition) }

    companion object {
        private const val IMAGE_SIZE = 400
    }
}
