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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.login.LoginActivity
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
    private val lifecycleOwner: LifecycleOwner,
    private val picasso: Picasso,
    private val client: OpenFoodAPIClient,
    private val product: Product,
    private val images: List<String>,
    private val snackView: View? = null,
    private val onImageClick: (Int) -> Unit
) : RecyclerView.Adapter<ProductPhotoViewHolder>() {
    private val isLoggedIn = context.isUserSet()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductPhotoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.images_item, parent, false)
        return ProductPhotoViewHolder(itemView, picasso)
    }


    override fun onBindViewHolder(holder: ProductPhotoViewHolder, position: Int) = holder.run {
        setImage(product.code, this@ProductPhotosAdapter.images[position])
        setOnImageClickListener(onImageClick)
        setOnEditClickListener {
            if (!isLoggedIn) {
                // FIXME: After login the user needs to refresh the fragment to edit images
                MaterialAlertDialogBuilder(context).apply {
                    setMessage(R.string.sign_in_to_edit)
                    setPositiveButton(R.string.txtSignIn) { d, _ ->
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        d.dismiss()
                    }
                }.show()

            } else {
                PopupMenu(context, holder.itemView).also {
                    it.inflate(R.menu.menu_image_edit)
                    it.setOnMenuItemClickListener(PopupItemClickListener(position))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) it.setForceShowIcon(true)
                }.show()
            }
        }
    }


    fun displaySetImageName(response: ObjectNode) {
        // TODO: 06/06/2021 i18n
        val imageName = try {
            response["imagefield"].asText()
        } catch (e: JSONException) {
            Log.e(LOG_TAG, "displaySetImageName", e)
            Toast.makeText(context, "Error while setting image from response $response", Toast.LENGTH_LONG).show()
            null
        } catch (e: NullPointerException) {
            Log.e(LOG_TAG, "displaySetImageName", e)
            Toast.makeText(context, "Error while setting image from response $response", Toast.LENGTH_LONG).show()
            null
        } ?: return

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
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        data = Uri.parse("mailto:")
                        type = "text/plain"
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

            // Edit photo async
            lifecycleOwner.lifecycle.coroutineScope.launch(Dispatchers.IO) {
                val response = client.editImage(product.code, imgMap)
                withContext(Dispatchers.Main) { displaySetImageName(response) }
            }

            return true
        }
    }

    companion object {
        private val LOG_TAG = this::class.simpleName!!
    }

    override fun getItemCount() = images.count()
}
