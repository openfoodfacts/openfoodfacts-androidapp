package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.images.IMAGE_EDIT_SIZE_FILE
import openfoodfacts.github.scrachx.openfood.images.getImageUrl
import openfoodfacts.github.scrachx.openfood.utils.Utils.picassoBuilder

class ProductPhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val editBtn: Button = itemView.findViewById(R.id.buttonOptions)
    private val image: ImageView = itemView.findViewById(R.id.img)

    fun setImage(imageName: String, barcode: String, context: Context) {
        val finalUrlString = getImageUrl(barcode, imageName, IMAGE_EDIT_SIZE_FILE)
        Log.d(LOG_TAG, "Loading image $finalUrlString...")
        picassoBuilder(context)
                .load(finalUrlString)
                .resize(400, 400)
                .centerInside()
                .into(image)
    }

    fun setOnImageListener(listener: (Int) -> Unit) = image.setOnClickListener { listener(adapterPosition) }

    fun setOnEditListener(listener: (Int) -> Unit) = editBtn.setOnClickListener { listener(adapterPosition) }

    companion object {
        private val LOG_TAG = this::class.simpleName!!
    }
}