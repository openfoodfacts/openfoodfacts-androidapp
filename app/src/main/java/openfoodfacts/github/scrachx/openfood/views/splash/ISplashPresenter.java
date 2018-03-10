package openfoodfacts.github.scrachx.openfood.views.splash;

import android.content.res.AssetManager;

/**
 * Created by Lobster on 03.03.18.
 */

public interface ISplashPresenter {

    interface Actions {
        void refreshData();
    }

    interface View {
        void showLoading();

        void hideLoading(boolean isError);

        void navigateToMainActivity();

        AssetManager getAssetManager();
    }
}
