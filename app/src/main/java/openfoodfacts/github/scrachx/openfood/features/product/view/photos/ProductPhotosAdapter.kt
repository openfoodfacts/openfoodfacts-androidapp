package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.LoginActivity
import openfoodfacts.github.scrachx.openfood.images.*
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import org.json.JSONException

/**
 * Created by prajwalm on 10/09/18.
 */
class ProductPhotosAdapter(
        private val context: Context,
        private val product: Product,
        private val isLoggedIn: Boolean,
        private val images: List<String>,
        private val onImageClick: (Int) -> Unit
) : RecyclerView.Adapter<ProductPhotoViewHolder>(), Disposable {
    private val barcode = product.code
    private val imgMap = hashMapOf<String, String>()
    private val openFoodAPIClient = OpenFoodAPIClient(context)
    private val disp = CompositeDisposable()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductPhotoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.images_item, parent, false)
        return ProductPhotoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProductPhotoViewHolder, position: Int) = holder.run {
        setImage(this@ProductPhotosAdapter.images[position], barcode, context)
        setOnImageListener { onImageClick(it) }
        setOnEditListener {
            if (isLoggedIn) {
                PopupMenu(context, holder.itemView).let {
                    it.inflate(R.menu.menu_image_edit)
                    it.setOnMenuItemClickListener(PopupItemClickListener(position))
                    it.show()
                }
            } else {
                context.startActivity(Intent(context, LoginActivity::class.java))
            }
        }
    }


    fun displaySetImageName(response: String?) {
        val imageName = try {
            jacksonObjectMapper().readTree(response)!!["imagefield"].asText()
        } catch (e: JSONException) {
            Log.e(LOG_TAG, "displaySetImageName", e)
            Toast.makeText(context, "Error while setting image from response $response", Toast.LENGTH_LONG).show()
            return
        } catch (e: NullPointerException) {
            Log.e(LOG_TAG, "displaySetImageName", e)
            Toast.makeText(context, "Error while setting image from response $response", Toast.LENGTH_LONG).show()
            return
        }
        Toast.makeText(context, "${context.getString(R.string.set_image_name)} $imageName", Toast.LENGTH_LONG).show()
    }


    private inner class PopupItemClickListener(private val position: Int) : PopupMenu.OnMenuItemClickListener {
        override fun onMenuItemClick(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.set_ingredient_image -> {
                    imgMap[IMG_ID] = images[position]
                    imgMap[PRODUCT_BARCODE] = barcode
                    imgMap[IMAGE_STRING_ID] = product.getImageStringKey(ProductImageField.INGREDIENTS)
                    openFoodAPIClient.editImage(product.code, imgMap)
                            .subscribe { response -> displaySetImageName(response) }
                            .addTo(disp)
                }
                R.id.set_nutrition_image -> {
                    imgMap[IMG_ID] = images[position]
                    imgMap[PRODUCT_BARCODE] = barcode
                    imgMap[IMAGE_STRING_ID] = product.getImageStringKey(ProductImageField.NUTRITION)
                    openFoodAPIClient.editImage(product.code, imgMap)
                            .subscribe { response -> displaySetImageName(response) }
                            .addTo(disp)

                }
                R.id.set_front_image -> {
                    imgMap[IMG_ID] = images[position]
                    imgMap[PRODUCT_BARCODE] = barcode
                    imgMap[IMAGE_STRING_ID] = product.getImageStringKey(ProductImageField.FRONT)
                    openFoodAPIClient.editImage(product.code, imgMap)
                            .subscribe { response -> displaySetImageName(response) }
                            .addTo(disp)
                }
                R.id.report_image -> context.startActivity(Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            data = Uri.parse("mailto:")
                            type = OpenFoodAPIClient.MIME_TEXT
                            putExtra(Intent.EXTRA_EMAIL, "Open Food Facts <contact@openfoodfacts.org>")
                            putExtra(Intent.EXTRA_SUBJECT, "Photo report for product $barcode")
                            putExtra(Intent.EXTRA_TEXT, "I've spotted a problematic photo for product $barcode")
                        }, "Send mail"))
            }
            return true
        }
    }

    companion object {
        private val LOG_TAG = this::class.simpleName!!
    }

    override fun getItemCount() = images.size
    override fun dispose() = disp.dispose()
    override fun isDisposed() = disp.isDisposed
}