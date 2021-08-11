package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.images.IMAGE_EDIT_SIZE_FILE
import openfoodfacts.github.scrachx.openfood.images.getImageUrl

class ProductPhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val editBtn: Button = itemView.findViewById(R.id.buttonOptions)
    private val imageView: ImageView = itemView.findViewById(R.id.img)

    fun setImage(barcode: String, imageName: String) {
        val finalUrlString = getImageUrl(barcode, imageName, IMAGE_EDIT_SIZE_FILE)
        Log.d(LOG_TAG, "Loading image $finalUrlString...")

        imageView.load(finalUrlString) { size(400) }
    }

    fun setOnImageClickListener(listener: (Int) -> Unit) =
        imageView.setOnClickListener { listener(bindingAdapterPosition) }

    fun setOnEditClickListener(listener: (Int) -> Unit) =
        editBtn.setOnClickListener { listener(bindingAdapterPosition) }

    companion object {
        private val LOG_TAG = ProductPhotoViewHolder::class.simpleName!!
    }
}
