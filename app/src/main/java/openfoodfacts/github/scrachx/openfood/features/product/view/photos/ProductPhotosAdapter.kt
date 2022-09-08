package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.databinding.ImagesItemBinding
import openfoodfacts.github.scrachx.openfood.models.Product

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
