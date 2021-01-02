package openfoodfacts.github.scrachx.openfood.features.splash

import io.reactivex.disposables.Disposable

/**
 * Created by Lobster on 03.03.18.
 */
interface ISplashActivity {
    interface Controller : Disposable {
        fun refreshData()
    }

    interface View {
        fun showLoading()
        fun hideLoading(isError: Boolean)
        fun navigateToMainActivity()
    }
}