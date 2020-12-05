package openfoodfacts.github.scrachx.openfood.features.splash

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivitySplashBinding
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.features.welcome.WelcomeActivity
import pl.aprilapps.easyphotopicker.EasyImage

class SplashActivity : BaseActivity(), ISplashActivity.View {
    private var _binding: ActivitySplashBinding? = null
    private val binding get() = _binding!!
    private lateinit var taglines: Array<String>

    /*
    To show different slogans below the logo while content is being downloaded.
     */
    private val changeTagline = object : Runnable {
        var i = 0
        override fun run() {
            i++
            if (i > taglines.size - 1) {
                i = 0
            }
            if (!isFinishing) {
                binding.tagline.text = taglines[i]
                binding.tagline.postDelayed(this, 1500)
            }
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        taglines = resources.getStringArray(R.array.taglines_array)
        binding.tagline.post(changeTagline)
        val presenter = SplashController(getSharedPreferences("prefs", 0), this, this)
        presenter.refreshData()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun navigateToMainActivity() {
        EasyImage.configuration(this).apply {
            setImagesFolderName("OFF_Images")
            saveInAppExternalFilesDir()
            setCopyExistingPicturesToPublicLocation(true)
        }
        WelcomeActivity.start(this)
        finish()
    }

    override fun showLoading() {}
    override fun hideLoading(isError: Boolean) {
        if (isError) {
            Handler(Looper.getMainLooper()).post {
                Snackbar.make(binding.root, R.string.errorWeb, BaseTransientBottomBar.LENGTH_LONG).show()
            }
        }
    }
}