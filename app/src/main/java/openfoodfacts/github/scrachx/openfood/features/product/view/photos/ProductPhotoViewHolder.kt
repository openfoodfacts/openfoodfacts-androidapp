package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.images.IMAGE_EDIT_SIZE_FILE
import openfoodfacts.github.scrachx.openfood.images.getImageUrl
import openfoodfacts.github.scrachx.openfood.models.Barcode

class ProductPhotoViewHolder(itemView: View, private val picasso: Picasso) : RecyclerView.ViewHolder(itemView) {
    private val editBtn: Button = itemView.findViewById(R.id.buttonOptions)
    private val imageView: ImageView = itemView.findViewById(R.id.img)

    fun setImage(barcode: Barcode, imageName: String) {
        val finalUrlString = getImageUrl(barcode, imageName, IMAGE_EDIT_SIZE_FILE)
        Log.d(LOG_TAG, "Loading image $finalUrlString...")
        picasso.load(finalUrlString)
            .resize(400, 400)
            .centerInside()
            .into(imageView)
    }

    fun setOnImageClickListener(listener: (Int) -> Unit) =
        imageView.setOnClickListener { listener(bindingAdapterPosition) }

    fun setOnEditClickListener(listener: (Int) -> Unit) =
        editBtn.setOnClickListener { listener(bindingAdapterPosition) }

    companion object {
        private val LOG_TAG = ProductPhotoViewHolder::class.simpleName!!
    }
}
