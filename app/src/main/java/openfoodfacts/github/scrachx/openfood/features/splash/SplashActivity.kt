package openfoodfacts.github.scrachx.openfood.features.splash

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivitySplashBinding
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.features.welcome.WelcomeActivity
import openfoodfacts.github.scrachx.openfood.utils.getAppPreferences
import pl.aprilapps.easyphotopicker.EasyImage
import kotlin.time.ExperimentalTime

@AndroidEntryPoint
class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()

    private val controller by lazy {
        SplashController(getAppPreferences(), this, this)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @ExperimentalTime
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        viewModel.setupTagLines(resources.getStringArray(R.array.taglines_array))
        viewModel.tagLine.observe(this, binding.tagline::setText)

        lifecycleScope.launch { controller.refreshData() }
    }

    suspend fun navigateToMainActivity() = withContext(Dispatchers.Main) {
        EasyImage.configuration(this@SplashActivity).apply {
            setImagesFolderName("OFF_Images")
            saveInAppExternalFilesDir()
            setCopyExistingPicturesToPublicLocation(true)
        }
        WelcomeActivity.start(this@SplashActivity)
        finish()
    }

    suspend fun showLoading() = withContext(Dispatchers.Main) {
        binding.loadingTxt.isVisible = true
        binding.loadingProgress.isVisible = true
    }

    suspend fun hideLoading(isError: Boolean) = withContext(Dispatchers.Main) {
        binding.loadingTxt.isVisible = false
        binding.loadingProgress.isVisible = false
        if (isError) {
            Snackbar.make(
                binding.root,
                R.string.errorWeb,
                BaseTransientBottomBar.LENGTH_LONG
            ).show()
        }
    }
}
