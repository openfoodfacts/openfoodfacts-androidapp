package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.LoginActivity
import openfoodfacts.github.scrachx.openfood.images.*
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.isUserSet
import org.json.JSONException

/**
 * Created by prajwalm on 10/09/18.
 */
class ProductPhotosAdapter(
        private val context: Context,
        private val product: Product,
        private val images: List<String>,
        private val snackView: View? = null,
        private val onImageClick: (Int) -> Unit,

        ) : RecyclerView.Adapter<ProductPhotoViewHolder>(), Disposable {
    private val isLoggedIn = context.isUserSet()
    private val openFoodAPIClient = OpenFoodAPIClient(context)
    private val disp = CompositeDisposable()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductPhotoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.images_item, parent, false)
        return ProductPhotoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProductPhotoViewHolder, position: Int) = holder.run {
        setImage(this@ProductPhotosAdapter.images[position], product.code, context)
        setOnImageListener { onImageClick(it) }
        setOnEditListener {
            if (!isLoggedIn) {
                context.startActivity(Intent(context, LoginActivity::class.java))
            } else {
                PopupMenu(context, holder.itemView).let {
                    it.inflate(R.menu.menu_image_edit)
                    it.setOnMenuItemClickListener(PopupItemClickListener(position))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) it.setForceShowIcon(true)
                    it.show()
                }
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
        val txt = "${context.getString(R.string.set_image_name)} $imageName"
        if (snackView == null) Toast.makeText(context, txt, Toast.LENGTH_LONG).show()
        else Snackbar.make(snackView, txt, Snackbar.LENGTH_LONG).show()
    }


    private inner class PopupItemClickListener(private val position: Int) : PopupMenu.OnMenuItemClickListener {
        override fun onMenuItemClick(item: MenuItem): Boolean {
            val imgMap = mutableMapOf(
                    IMG_ID to images[position],
                    PRODUCT_BARCODE to product.code
            )
            imgMap[IMAGE_STRING_ID] = when (item.itemId) {
                R.id.report_image -> {
                    context.startActivity(Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                data = Uri.parse("mailto:")
                                type = OpenFoodAPIClient.MIME_TEXT
                                putExtra(Intent.EXTRA_EMAIL, "Open Food Facts <contact@openfoodfacts.org>")
                                putExtra(Intent.EXTRA_SUBJECT, "Photo report for product ${product.code}")
                                putExtra(Intent.EXTRA_TEXT, "I've spotted a problematic photo for product ${product.code}")
                            }, context.getString(R.string.report_email_chooser_title)))
                    return true
                }

                R.id.set_ingredient_image -> product.getImageStringKey(ProductImageField.INGREDIENTS)
                R.id.set_recycling_image -> product.getImageStringKey(ProductImageField.PACKAGING)
                R.id.set_nutrition_image -> product.getImageStringKey(ProductImageField.NUTRITION)
                R.id.set_front_image -> product.getImageStringKey(ProductImageField.FRONT)
                else -> product.getImageStringKey(ProductImageField.OTHER)
            }
            if (snackView == null) Toast.makeText(context, context.getString(R.string.changes_saved), Toast.LENGTH_SHORT).show()
            else Snackbar.make(snackView, R.string.changes_saved, Snackbar.LENGTH_SHORT).show()

            openFoodAPIClient.editImage(product.code, imgMap)
                    .subscribe { response -> displaySetImageName(response) }
                    .addTo(disp)
            return true
        }
    }

    companion object {
        private val LOG_TAG = this::class.simpleName!!
    }

    override fun getItemCount() = images.count()
    override fun dispose() = disp.dispose()
    override fun isDisposed() = disp.isDisposed
}