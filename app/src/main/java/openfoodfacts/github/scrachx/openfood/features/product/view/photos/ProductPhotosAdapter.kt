package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.databinding.ImagesItemBinding
import openfoodfacts.github.scrachx.openfood.features.login.LoginActivity
import openfoodfacts.github.scrachx.openfood.images.IMAGE_STRING_ID
import openfoodfacts.github.scrachx.openfood.images.IMG_ID
import openfoodfacts.github.scrachx.openfood.images.PRODUCT_BARCODE
import openfoodfacts.github.scrachx.openfood.images.getImageStringKey
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.Intent
import openfoodfacts.github.scrachx.openfood.utils.isUserSet
import org.json.JSONException

/**
 * Created by prajwalm on 10/09/18.
 */
class ProductPhotosAdapter(
    private val product: Product,
    private val imageNames: List<String>,
    private val onImageTap: (Int) -> Unit,
    private val onLoginNeeded: (View, Int) -> Unit,
) : RecyclerView.Adapter<ProductPhotoViewHolder>() {

    override fun getItemCount() = imageNames.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductPhotoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = ImagesItemBinding.inflate(inflater, parent, false)
        return ProductPhotoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProductPhotoViewHolder, position: Int) {
        holder.run {
            setImage(product.code, imageNames[position])
            setOnClickListener { onImageTap(position) }
            setOnEditClickListener { onLoginNeeded(it, position) }
        }
    }

}
