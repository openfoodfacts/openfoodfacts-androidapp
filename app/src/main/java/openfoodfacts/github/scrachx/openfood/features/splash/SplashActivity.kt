package openfoodfacts.github.scrachx.openfood.features.splash

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivitySplashBinding
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.features.welcome.WelcomeActivity
import pl.aprilapps.easyphotopicker.EasyImage

class SplashActivity : BaseActivity(), ISplashActivity.View {
    private val binding by lazy { ActivitySplashBinding.inflate(layoutInflater) }

    /*
    To show different slogans below the logo while content is being downloaded.
     */
    private fun getTagLineRunnable(tagLines: Array<String>) = object : Runnable {
        var i = 0
        override fun run() {
            while (!isFinishing) {
                binding.tagline.text = tagLines[i++ % tagLines.size]
                binding.tagline.postDelayed(this, 1500)
            }
        }
    }

    private val controller by lazy {
        SplashController(getSharedPreferences("prefs", 0), this, this)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        val taglines = resources.getStringArray(R.array.taglines_array)
        binding.tagline.post(getTagLineRunnable(taglines))

        controller.refreshData()
    }

    override fun onDestroy() {
        controller.dispose()
        super.onDestroy()
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

    override fun showLoading() {
        runOnUiThread {
            binding.loading = View.VISIBLE
        }
    }

    override fun hideLoading(isError: Boolean) {
        runOnUiThread {
            binding.loading = View.GONE
            if (isError) {
                Snackbar.make(binding.root, R.string.errorWeb, BaseTransientBottomBar.LENGTH_LONG).show()
            }
        }
    }
}