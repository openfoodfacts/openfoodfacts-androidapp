package openfoodfacts.github.scrachx.openfood.features

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.chrisbanes.photoview.PhotoViewAttacher
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityZoomImageBinding
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.images.IMAGE_URL
import java.util.*
import javax.inject.Inject

/**
 * Activity to display/edit product images
 */
@AndroidEntryPoint
class ImageZoomActivity : BaseActivity() {
    private var _binding: ActivityZoomImageBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var picasso: Picasso

    private lateinit var attacher: PhotoViewAttacher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityZoomImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        attacher = PhotoViewAttacher(binding.imageViewFullScreen)

        //delaying the transition until the view has been laid out
        ActivityCompat.postponeEnterTransition(this)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.imageFullscreen)

        loadImage(intent.getStringExtra(IMAGE_URL))
    }


    override fun onResume() {
        binding.toolbar.setTitle(R.string.imageFullscreen)
        super.onResume()
    }


    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadImage(imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            binding.imageViewFullScreen.setImageDrawable(null)
            stopRefresh()
        } else {
            startRefresh(getString(R.string.txtLoading))
            picasso
                    .load(imageUrl)
                    .into(binding.imageViewFullScreen, object : Callback {
                        override fun onSuccess() {
                            // Activity could have been destroyed while we load the image
                            if (isFinishing) return
                            attacher.update()
                            scheduleStartPostponedTransition(binding.imageViewFullScreen)
                            binding.imageViewFullScreen.visibility = View.VISIBLE
                            stopRefresh()
                        }

                        override fun onError(ex: Exception) {
                            // Activity could have been destroyed while we load the image
                            if (isFinishing) return
                            binding.imageViewFullScreen.visibility = View.VISIBLE
                            Toast.makeText(this@ImageZoomActivity, resources.getString(R.string.txtConnectionError), Toast.LENGTH_LONG).show()
                            stopRefresh()
                        }
                    })
        }
    }

    private fun stopRefresh() {
        binding.progressBar.visibility = View.GONE
        binding.textInfo.visibility = View.GONE
    }


    private fun startRefresh(text: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.textInfo.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.textInfo.text = text
    }

    /**
     * For scheduling a postponed transition after the proper measures of the view are done
     * and the view has been properly laid out in the View hierarchy
     */
    private fun scheduleStartPostponedTransition(sharedElement: View) {
        sharedElement.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                sharedElement.viewTreeObserver.removeOnPreDrawListener(this)
                ActivityCompat.startPostponedEnterTransition(this@ImageZoomActivity)
                return true
            }
        })
    }

    companion object {
        fun start(
                context: Context,
                imageUrl: String
        ) = context.startActivity(Intent(context, ImageZoomActivity::class.java).apply {
            putExtra(IMAGE_URL, imageUrl)
        })
    }
}
