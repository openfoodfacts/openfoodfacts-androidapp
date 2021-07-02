package openfoodfacts.github.scrachx.openfood.features.splash

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivitySplashBinding
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.features.welcome.WelcomeActivity
import pl.aprilapps.easyphotopicker.EasyImage
import kotlin.time.ExperimentalTime

class SplashActivity : BaseActivity() {
    private val binding by lazy { ActivitySplashBinding.inflate(layoutInflater) }
    private val viewModel: SplashViewModel by viewModels()


    private val controller by lazy {
        SplashController(getSharedPreferences("prefs", 0), this, this)
    }

    @ExperimentalTime
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        viewModel.tagLines.postValue(resources.getStringArray(R.array.taglines_array))

        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

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
        binding.loading = View.VISIBLE
    }

    suspend fun hideLoading(isError: Boolean) = withContext(Dispatchers.Main) {
        binding.loading = View.GONE
        if (isError) {
            Snackbar.make(
                binding.root,
                R.string.errorWeb,
                BaseTransientBottomBar.LENGTH_LONG
            ).show()
        }
    }
}