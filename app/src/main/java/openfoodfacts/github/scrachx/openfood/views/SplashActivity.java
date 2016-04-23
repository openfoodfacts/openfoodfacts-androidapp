package openfoodfacts.github.scrachx.openfood.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.steamcrafted.loadtoast.LoadToast;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Additive;
import openfoodfacts.github.scrachx.openfood.models.FoodAPIRestClientUsage;

public class SplashActivity extends BaseActivity {

    private SharedPreferences settings;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        settings = getSharedPreferences("prefs", 0);
        boolean firstRun = settings.getBoolean("firstRun", true);
        if (!firstRun) {
            Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
            SplashActivity.this.startActivity(mainIntent);
            SplashActivity.this.finish();
        } else {
            new GetJson(this).execute();
        }
    }

    private class GetJson extends AsyncTask<Void, Boolean, Boolean> {

        private Context context;
        private LoadToast lt;

        public GetJson(Context ctx) {
            context = ctx;
            lt = new LoadToast(ctx);
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lt.setText(context.getString(R.string.toast_retrieving));
            lt.setBackgroundColor(context.getResources().getColor(R.color.indigo_600));
            lt.setTextColor(context.getResources().getColor(R.color.white));
            lt.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {

            String json = null;
            String json1 = null;
            try {
                InputStream is = getAssets().open("additives_fr.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, "UTF-8");
                ObjectMapper objectMapper = new ObjectMapper();
                List<Additive> la = objectMapper.readValue(json, new TypeReference<List<Additive>>() {});
                for (Additive a : la) {
                    Additive ta = new Additive(a.getCode(), a.getName(), a.getRisk());
                    ta.save();
                }

                InputStream is1 = getAssets().open("additives_en.json");
                int size1 = is1.available();
                byte[] buffer1 = new byte[size1];
                is1.read(buffer1);
                is1.close();
                json1 = new String(buffer1, "UTF-8");
                ObjectMapper objectMapper1 = new ObjectMapper();
                List<Additive> la1 = objectMapper1.readValue(json1, new TypeReference<List<Additive>>() {});
                for (Additive a : la1) {
                    Additive ta = new Additive(a.getCode(), a.getName(), a.getRisk());
                    ta.save();
                }
                FoodAPIRestClientUsage api = new FoodAPIRestClientUsage();
                api.getAllergens();
                return true;
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) lt.success();
            else lt.error();
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("firstRun", false);
            editor.apply();
            Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        }
    }
}