package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import logcat.LogPriority
import logcat.logcat
import openfoodfacts.github.scrachx.openfood.databinding.ImagesItemBinding
import openfoodfacts.github.scrachx.openfood.images.IMAGE_EDIT_SIZE
import openfoodfacts.github.scrachx.openfood.images.getImageUrl
import openfoodfacts.github.scrachx.openfood.models.Barcode
import javax.inject.Inject

class ProductPhotoViewHolder(
    private val binding: ImagesItemBinding,
) : RecyclerView.ViewHolder(binding.root) {

    @Inject
    lateinit var picasso: Picasso

    fun setImage(barcode: Barcode, imageName: String) {
        val imageUrl = getImageUrl(barcode, imageName, IMAGE_EDIT_SIZE)
        logcat(LogPriority.DEBUG) { "Loading image $imageUrl..." }
        picasso.load(imageUrl)
            .resize(IMAGE_SIZE, IMAGE_SIZE)
            .centerInside()
            .into(binding.imageView)
    }

    fun setOnClickListener(listener: (View) -> Unit) {
        binding.imageView.setOnClickListener(listener)
    }

    fun setOnEditClickListener(listener: (View) -> Unit) {
        binding.editBtn.setOnClickListener(listener)
    }

    companion object {
        private const val IMAGE_SIZE = 400
    }
}
