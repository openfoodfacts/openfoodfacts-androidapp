package openfoodfacts.github.scrachx.openfood.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;

import net.steamcrafted.loadtoast.LoadToast;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Additive;
import openfoodfacts.github.scrachx.openfood.models.AdditiveDao;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.JsonUtils;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import pl.aprilapps.easyphotopicker.EasyImage;

public class SplashActivity extends BaseActivity {

    private SharedPreferences settings;
    private AdditiveDao mAdditiveDao;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        settings = getSharedPreferences("prefs", 0);
        mAdditiveDao = Utils.getAppDaoSession(this).getAdditiveDao();
        OpenFoodAPIClient client = new OpenFoodAPIClient(this);
        client.getPackagerCodes();
        if ((BuildConfig.FLAVOR.equals("off"))) {
            boolean firstRun = settings.getBoolean("firstRun", true);

            boolean errorAdditives = settings.getBoolean("errorAdditives", true);
            boolean errorAllergens = settings.getBoolean("errorAllergens", true);

            if (!errorAdditives && !errorAllergens) {
                settings.edit()
                        .putBoolean("firstRun", false)
                        .apply();
                firstRun = false;
            }
            if (!firstRun) {
                launchMainActivity();
            } else {
                new GetJson().execute();
            }
        } else {
            launchMainActivity();
        }

    }

    private void launchMainActivity() {
        EasyImage.configuration(this)
                .setImagesFolderName("OFF_Images")
                .saveInAppExternalFilesDir()
                .setCopyExistingPicturesToPublicLocation(true);
        Intent mainIntent = new Intent(SplashActivity.this, WelcomeActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private class GetJson extends AsyncTask<Void, Integer, Boolean> {

        private static final String ADDITIVE_IMPORT = "ADDITIVE_IMPORT";
        private LoadToast lt = new LoadToast(SplashActivity.this);


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lt.setText(SplashActivity.this.getString(R.string.toast_retrieving));
            lt.setBackgroundColor(ContextCompat.getColor(SplashActivity.this, R.color.blue));
            lt.setTextColor(ContextCompat.getColor(SplashActivity.this, R.color.white));
            lt.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            boolean result = true;
            boolean errorAdditives = settings.getBoolean("errorAdditives", true);

            if (!errorAdditives) {
                return result;
            }

            String additivesFile = "additives_" + LocaleHelper.getLanguage(SplashActivity.this) + ".json";

            InputStream is = null;
            try {
                is = getAssets().open(additivesFile);
                List<Additive> frenchAdditives = JsonUtils.readFor(new TypeReference<List<Additive>>() {
                })
                        .readValue(is);
                mAdditiveDao.insertOrReplaceInTx(frenchAdditives);
            } catch (IOException e) {
                result = false;
                Log.e(ADDITIVE_IMPORT, "Unable to import additives from " + additivesFile);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e1) {
                        Log.e(ADDITIVE_IMPORT, "Unable to close the inputstream of " + additivesFile);
                    }
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            super.onPostExecute(result);
            final SharedPreferences.Editor editor = settings.edit();
            boolean errorAllergens = settings.getBoolean("errorAllergens", true);
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if (isConnected) {
                if (errorAllergens) {
                    OpenFoodAPIClient api = new OpenFoodAPIClient(SplashActivity.this);
                    api.getAllergens(value -> {
                        if (result && value) {
                            lt.success();
                            editor.putBoolean("firstRun", false);
                        }
                        if (!value) {
                            lt.error();
                            editor.putBoolean("errorAllergens", true);
                        } else {
                            editor.putBoolean("errorAllergens", false);
                        }

                        editor.apply();

                        Intent mainIntent = new Intent(SplashActivity.this, WelcomeActivity.class);
                        startActivity(mainIntent);
                        finish();
                    });
                }
            } else {
                if (!result) {
                    lt.error();
                    editor.putBoolean("errorAdditives", true);
                    editor.apply();
                } else {
                    editor.putBoolean("errorAdditives", false);
                    editor.apply();
                }
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }
    }
}