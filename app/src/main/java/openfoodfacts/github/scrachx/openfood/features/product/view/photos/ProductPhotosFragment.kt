package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.FragmentProductPhotosBinding
import openfoodfacts.github.scrachx.openfood.features.FullScreenActivityOpener
import openfoodfacts.github.scrachx.openfood.features.login.LoginActivity
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_STATE
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.images.IMAGE_STRING_ID
import openfoodfacts.github.scrachx.openfood.images.IMG_ID
import openfoodfacts.github.scrachx.openfood.images.PRODUCT_BARCODE
import openfoodfacts.github.scrachx.openfood.images.getImageStringKey
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.Intent
import openfoodfacts.github.scrachx.openfood.utils.isUserSet
import openfoodfacts.github.scrachx.openfood.utils.requireProductState
import org.json.JSONException
import javax.inject.Inject

/**
 * A fragment to display all the product images in a grid.
 *
 * It allows the user to select the images as front/ingredients/... images
 * of the product.
 *
 * @author prajwalm
 */
@AndroidEntryPoint
class ProductPhotosFragment : BaseFragment() {
    private var _binding: FragmentProductPhotosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductPhotosViewModel by viewModels()

    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var picasso: Picasso

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductPhotosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val product = requireProductState().product!!

        viewModel.imageNames
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { loadImages(product, it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.stateFlow
            .flowWithLifecycle(lifecycle)
            .onEach {
                when (it) {
                    is ProductPhotosViewModel.State.SetImageName -> displaySetImageName(it.response)
                }
            }
            .launchIn(lifecycleScope)

    }


    private fun loadImages(product: Product, imageNames: List<String>) {
        val context = requireContext()

        val adapter = ProductPhotosAdapter(
            product = product,
            imageNames = imageNames,
            onImageTap = { index -> openImage(product, imageNames[index]) },
            onLoginNeeded = { view, position ->
                if (!requireContext().isUserSet()) {
                    showLoginDialog()
                } else {
                    showEditPopup(view, imageNames, position, product)
                }
            }
        )

        binding.progress.hide()
        // Check if user is logged in
        binding.imagesRecycler.adapter = adapter
        binding.imagesRecycler.layoutManager = GridLayoutManager(context, 3)
    }

    private fun showEditPopup(
        view: View,
        imageNames: List<String>,
        position: Int,
        product: Product,
    ) {
        val context = requireContext()

        PopupMenu(context, view).also {
            it.inflate(R.menu.menu_image_edit)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.setForceShowIcon(true)
            }
            it.setOnMenuItemClickListener { item ->
                val imgMap = mutableMapOf(
                    IMG_ID to imageNames[position],
                    PRODUCT_BARCODE to product.code
                )
                when (item.itemId) {
                    R.id.report_image -> {
                        sendEmail(context, product)
                        return@setOnMenuItemClickListener true
                    }

                    R.id.set_ingredient_image -> imgMap[IMAGE_STRING_ID] =
                        product.getImageStringKey(ProductImageField.INGREDIENTS)

                    R.id.set_recycling_image -> imgMap[IMAGE_STRING_ID] =
                        product.getImageStringKey(ProductImageField.PACKAGING)

                    R.id.set_nutrition_image -> imgMap[IMAGE_STRING_ID] =
                        product.getImageStringKey(ProductImageField.NUTRITION)

                    R.id.set_front_image -> imgMap[IMAGE_STRING_ID] =
                        product.getImageStringKey(ProductImageField.FRONT)

                    else -> imgMap[IMAGE_STRING_ID] =
                        product.getImageStringKey(ProductImageField.OTHER)
                }

                notify(context.getString(R.string.changes_saved))

                // Edit photo async
                viewModel.editImage(product, imgMap)
                true
            }
        }.show()
    }

    private fun sendEmail(context: Context, product: Product) {
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND) {
                    data = Uri.parse("mailto:")
                    type = "text/plain"
                    putExtra(Intent.EXTRA_EMAIL,
                        "Open Food Facts <contact@openfoodfacts.org>")
                    putExtra(Intent.EXTRA_SUBJECT,
                        "Photo report for product ${product.code}")
                    putExtra(Intent.EXTRA_TEXT,
                        "I've spotted a problematic photo for product ${product.code}")
                },
                context.getString(R.string.report_email_chooser_title),
            ),
        )
    }


    private val loginContract = registerForActivityResult(LoginActivity.Companion.LoginContract()) {}

    private fun showLoginDialog() {
        val context = requireContext()

        MaterialAlertDialogBuilder(context)
            .setMessage(R.string.sign_in_to_edit)
            .setPositiveButton(R.string.txtSignIn) { d, _ ->
                d.dismiss()
                loginContract.launch(Unit)
            }
            .show()
    }

    private fun openImage(product: Product, image: String) {
        val barcode = getBarcodeUrl(product)
        val imageUrl = "${BuildConfig.STATICURL}/images/products/$barcode/$image.jpg"
        return FullScreenActivityOpener.openZoom(
            requireActivity(),
            imageUrl,
            null
        )
    }

    private fun displaySetImageName(response: ObjectNode) {
        val context = requireContext()

        // TODO: 06/06/2021 i18n
        val imageName = try {
            response["imagefield"].asText()
        } catch (e: JSONException) {
            logcat(LogPriority.ERROR) { "Error while setting image from response $response: ${e.asLog()}" }
            Toast.makeText(context,
                context.getString(R.string.set_image_name_error, response.toString()),
                Toast.LENGTH_LONG).show()
            null
        } catch (e: NullPointerException) {
            logcat(LogPriority.ERROR) { "Error while setting image from response $response: ${e.asLog()}" }
            Toast.makeText(context,
                context.getString(R.string.set_image_name_error, response.toString()),
                Toast.LENGTH_LONG).show()
            null
        } ?: return

        notify(context.getString(R.string.set_image_name, imageName))
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun notify(txt: String) {
        Snackbar.make(binding.root, txt, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        fun newInstance(productState: ProductState) = ProductPhotosFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY_STATE, productState)
            }
        }

        private fun getBarcodeUrl(product: Product): String {
            return if (product.code.length <= 8) {
                product.code
            } else {
                StringBuilder(product.code)
                    .insert(3, "/")
                    .insert(6 + 1, "/")
                    .insert(9 + 2, "/")
                    .toString()
            }
        }
    }
}
