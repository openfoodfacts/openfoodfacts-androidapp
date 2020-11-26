package openfoodfacts.github.scrachx.openfood.features

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.images.ImageKeyHelper
import openfoodfacts.github.scrachx.openfood.images.ImageSize
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.FileUtils.isAbsoluteUrl
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import org.apache.commons.lang.StringUtils

/**
 * Used to open fullscreen activity
 */
object FullScreenActivityOpener {
    fun openForUrl(
            fragment: Fragment,
            product: Product,
            imageType: ProductImageField,
            mUrlImage: String?,
            mImageFront: View
    ) {
        // A new file added just now
        if (isAbsoluteUrl(mUrlImage)) {
            loadImageServerUrl(fragment, product, imageType, mImageFront)
            return
        }
        startActivity(fragment, mImageFront, createIntent(fragment.context, product, imageType, mUrlImage))
    }

    private fun startActivity(fragment: Fragment, mImageFront: View?, intent: Intent) {
        if (mImageFront != null && fragment.activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(fragment.requireActivity(), mImageFront, fragment.requireActivity().getString(R.string.product_transition))
            fragment.startActivityForResult(intent, ImagesManageActivity.REQUEST_EDIT_IMAGE, options.toBundle())
        } else {
            fragment.startActivityForResult(intent, ImagesManageActivity.REQUEST_EDIT_IMAGE)
        }
    }

    fun openForUrl(activity: Activity?, product: Product, imageType: ProductImageField, mUrlImage: String?, mImageFront: View?) {
        if (activity == null) return
        startActivity(activity, mImageFront, createIntent(activity.baseContext, product, imageType, mUrlImage))
    }

    private fun startActivity(activity: Activity?, mImageFront: View?, intent: Intent) {
        if (mImageFront != null && activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, mImageFront,
                    activity.getString(R.string.product_transition))
            activity.startActivityForResult(intent, ImagesManageActivity.REQUEST_EDIT_IMAGE, options.toBundle())
        } else activity?.startActivityForResult(intent, ImagesManageActivity.REQUEST_EDIT_IMAGE)
    }

    fun openZoom(activity: Activity?, mUrlImage: String?, mImageFront: View?) {
        if (activity == null) {
            return
        }
        val intent = Intent(activity, ImageZoomActivity::class.java)
        intent.putExtra(ImageKeyHelper.IMAGE_URL, mUrlImage)
        startActivity(activity, mImageFront, intent)
    }

    private fun createIntent(context: Context?, product: Product, imageType: ProductImageField, mUrlImage: String?): Intent {
        val intent = Intent(context, ImagesManageActivity::class.java)
        var language = getLanguage(context)
        if (!product.isLanguageSupported(language) && StringUtils.isNotBlank(product.lang)) {
            language = product.lang
        }
        intent.putExtras(ImageKeyHelper.createImageBundle(imageType, product, language, mUrlImage))
        return intent
    }

    private fun loadImageServerUrl(fragment: Fragment, product: Product, imageType: ProductImageField, mImageFront: View) {
        val client = OpenFoodAPIClient(fragment.requireContext())
        client.getProductImages(product.code) { newState: ProductState ->
            val newStateProduct = newState.product
            if (newStateProduct != null) {
                val language = getLanguage(fragment.context)
                val imageUrl = newStateProduct.getSelectedImage(language, imageType, ImageSize.DISPLAY)
                if (StringUtils.isNotBlank(imageUrl)) {
                    openForUrl(fragment, newStateProduct, imageType, imageUrl, mImageFront)
                } else {
                    Toast.makeText(fragment.context, R.string.cant_edit_image_not_yet_uploaded, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}