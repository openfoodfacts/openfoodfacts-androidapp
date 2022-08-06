package openfoodfacts.github.scrachx.openfood.features

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.annotation.CheckResult
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.images.manage.ImagesManageActivity
import openfoodfacts.github.scrachx.openfood.features.images.zoom.ImageZoomActivity
import openfoodfacts.github.scrachx.openfood.images.IMAGE_URL
import openfoodfacts.github.scrachx.openfood.images.ImageSize
import openfoodfacts.github.scrachx.openfood.images.createImageBundle
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.isAbsoluteUrl
import org.jetbrains.annotations.Contract

/**
 * Used to open fullscreen activity
 */
object FullScreenActivityOpener {

    suspend fun openForUrl(
        fragment: Fragment,
        client: ProductRepository,
        product: Product,
        imageType: ProductImageField,
        mUrlImage: String,
        mImageFront: View,
        language: String
    ) = openForUrl(fragment.requireActivity(), client, product, imageType, mUrlImage, mImageFront, language)

    suspend fun openForUrl(
        activity: Activity,
        client: ProductRepository,
        product: Product,
        imageType: ProductImageField,
        mUrlImage: String,
        mImageFront: View,
        language: String
    ) {
        // A new file added just now
        if (isAbsoluteUrl(mUrlImage)) {
            loadImageServerUrl(activity, client, product, imageType, mImageFront, language)
            return
        }
        startActivity(activity, mImageFront, createIntent(activity, product, imageType, mUrlImage, language))
    }

    private fun startActivity(activity: Activity, mImageFront: View?, intent: Intent) {
        val bundle = mImageFront?.let {
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                mImageFront,
                activity.getString(R.string.product_transition)
            ).toBundle()
        }
        activity.startActivityForResult(intent, ImagesManageActivity.REQUEST_EDIT_IMAGE, bundle)
    }

    fun openZoom(
        activity: Activity,
        imageUrl: String,
        imageView: View?
    ) = startActivity(activity, imageView, Intent(activity, ImageZoomActivity::class.java).apply {
        putExtra(IMAGE_URL, imageUrl)
    })

    @CheckResult
    @Contract(pure = true)
    private fun createIntent(
        context: Context,
        product: Product,
        imageType: ProductImageField,
        mUrlImage: String,
        language: String
    ): Intent {
        var productLanguage = language
        if (!product.isLanguageSupported(language) && product.lang.isNotBlank()) {
            productLanguage = product.lang
        }
        return Intent(context, ImagesManageActivity::class.java).apply {
            putExtras(createImageBundle(imageType, product, productLanguage, mUrlImage))
        }
    }

    private suspend fun loadImageServerUrl(
        activity: Activity,
        client: ProductRepository,
        product: Product,
        imageType: ProductImageField,
        mImageFront: View,
        language: String
    ) {
        client.getProductImages(product.code).product?.let { newProduct ->
            val imageUrl = newProduct.getSelectedImage(language, imageType, ImageSize.DISPLAY)
            if (!imageUrl.isNullOrBlank()) {
                openForUrl(activity, client, newProduct, imageType, imageUrl, mImageFront, language)
            } else {
                Toast.makeText(activity, R.string.cant_edit_image_not_yet_uploaded, Toast.LENGTH_LONG).show()
            }
        }
    }
}
