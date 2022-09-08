package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import openfoodfacts.github.scrachx.openfood.R
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
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val picasso: Picasso,
    private val productRepository: ProductRepository,
    private val product: Product,
    private val imageNames: List<String>,
    private val snackView: View? = null,
    private val onImageTap: (Int) -> Unit,
) : RecyclerView.Adapter<ProductPhotoViewHolder>() {
    private val isLoggedIn = context.isUserSet()

    override fun getItemCount() = imageNames.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductPhotoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = ImagesItemBinding.inflate(inflater, parent, false)
        return ProductPhotoViewHolder(itemView, picasso)
    }


    override fun onBindViewHolder(holder: ProductPhotoViewHolder, position: Int) {
        holder.run {
            setImage(product.code, imageNames[position])
            setOnClickListener(onImageTap)
            setOnEditClickListener {
                if (!isLoggedIn) {
                    // FIXME: After login the user needs to refresh the fragment to edit images
                    MaterialAlertDialogBuilder(context).apply {
                        setMessage(R.string.sign_in_to_edit)
                        setPositiveButton(R.string.txtSignIn) { d, _ ->
                            context.startActivity(Intent<LoginActivity>(context))
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
    }


    fun displaySetImageName(response: ObjectNode) {
        // TODO: 06/06/2021 i18n
        val imageName = try {
            response["imagefield"].asText()
        } catch (e: JSONException) {
            logcat(LogPriority.ERROR) { "Error while setting image from response $response: ${e.asLog()}" }
            Toast.makeText(context, "Error while setting image from response $response", Toast.LENGTH_LONG).show()
            null
        } catch (e: NullPointerException) {
            logcat(LogPriority.ERROR) { "Error while setting image from response $response: ${e.asLog()}" }
            Toast.makeText(context, "Error while setting image from response $response", Toast.LENGTH_LONG).show()
            null
        } ?: return

        notify(context.getString(R.string.set_image_name, imageName))
    }

    private fun notify(txt: String) {
        if (snackView == null) {
            Toast.makeText(context, txt, Toast.LENGTH_LONG).show()
        } else {
            Snackbar.make(snackView, txt, Snackbar.LENGTH_LONG).show()
        }
    }


    private inner class PopupItemClickListener(private val position: Int) : PopupMenu.OnMenuItemClickListener {
        override fun onMenuItemClick(item: MenuItem): Boolean {
            val imgMap = mutableMapOf(
                IMG_ID to imageNames[position],
                PRODUCT_BARCODE to product.code
            )
            imgMap[IMAGE_STRING_ID] = when (item.itemId) {
                R.id.report_image -> {
                    val intent = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        data = Uri.parse("mailto:")
                        type = "text/plain"
                        putExtra(Intent.EXTRA_EMAIL, "Open Food Facts <contact@openfoodfacts.org>")
                        putExtra(Intent.EXTRA_SUBJECT, "Photo report for product ${product.code}")
                        putExtra(Intent.EXTRA_TEXT, "I've spotted a problematic photo for product ${product.code}")
                    }, context.getString(R.string.report_email_chooser_title))
                    context.startActivity(intent)
                    return true
                }

                R.id.set_ingredient_image -> product.getImageStringKey(ProductImageField.INGREDIENTS)
                R.id.set_recycling_image -> product.getImageStringKey(ProductImageField.PACKAGING)
                R.id.set_nutrition_image -> product.getImageStringKey(ProductImageField.NUTRITION)
                R.id.set_front_image -> product.getImageStringKey(ProductImageField.FRONT)
                else -> product.getImageStringKey(ProductImageField.OTHER)
            }

            notify(context.getString(R.string.changes_saved))

            // Edit photo async
            lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val response = productRepository.editImage(product.code, imgMap)
                withContext(Dispatchers.Main) { displaySetImageName(response) }
            }

            return true
        }
    }


}
