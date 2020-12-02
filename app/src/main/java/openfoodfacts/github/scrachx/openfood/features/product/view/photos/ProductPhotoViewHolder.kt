package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.images.IMAGE_EDIT_SIZE_FILE
import openfoodfacts.github.scrachx.openfood.images.getImageUrl
import openfoodfacts.github.scrachx.openfood.utils.Utils

class ProductPhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val menuButton: Button = itemView.findViewById(R.id.buttonOptions)
    private val productImage: ImageView = itemView.findViewById(R.id.img)

    fun setImage(imageName: String, barcode: String, activity: Activity) {
        val finalUrlString = getImageUrl(barcode, imageName, IMAGE_EDIT_SIZE_FILE)
        Log.d(LOG_TAG, "Loading image $finalUrlString...")
        Utils.picassoBuilder(activity)
                .load(finalUrlString)
                .resize(400, 400)
                .centerInside()
                .into(productImage)
    }

    fun setOnImageListener(listener: (Int) -> Unit) {
        productImage.setOnClickListener { listener(adapterPosition) }
    }

    fun setOnEditListener(listener: (Int) -> Unit) {
        menuButton.setOnClickListener { listener(adapterPosition) }
    }

    companion object {
        private val LOG_TAG = this::class.simpleName!!
    }
}