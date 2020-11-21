package openfoodfacts.github.scrachx.openfood.features.splash

/**
 * Created by Lobster on 03.03.18.
 */
interface ISplashActivity {
    interface Controller {
        fun refreshData()
        fun onDestroy()
    }

    interface View {
        fun showLoading()
        fun hideLoading(isError: Boolean)
        fun navigateToMainActivity()
    }
}