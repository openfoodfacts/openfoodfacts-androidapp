package openfoodfacts.github.scrachx.openfood.views;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Additives;

/**
 * Created by scotscriven on 08/05/15.
 */
public class SplashActivity extends Activity {

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 1000;
    private SharedPreferences settings;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash);

        settings = getSharedPreferences("prefs",0);
        boolean firstRun = settings.getBoolean("firstRun",true);
        if(!firstRun) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                    SplashActivity.this.startActivity(mainIntent);
                    SplashActivity.this.finish();
                }
            }, SPLASH_DISPLAY_LENGTH);

        } else {
            new AdditivesJson().execute();
        }
    }

    private class AdditivesJson extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

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
                List<Additives> la = objectMapper.readValue(json, new TypeReference<List<Additives>>(){});
                for (Additives a: la) {
                    Additives ta = new Additives(a.getCode(),a.getName(),a.getRisk());
                    ta.save();
                }

                InputStream is1 = getAssets().open("additives_en.json");
                int size1 = is1.available();
                byte[] buffer1 = new byte[size1];
                is1.read(buffer1);
                is1.close();
                json1 = new String(buffer1, "UTF-8");
                ObjectMapper objectMapper1 = new ObjectMapper();
                List<Additives> la1 = objectMapper1.readValue(json1, new TypeReference<List<Additives>>(){});
                for (Additives a: la1) {
                    Additives ta = new Additives(a.getCode(),a.getName(),a.getRisk());
                    ta.save();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            SharedPreferences.Editor editor=settings.edit();
            editor.putBoolean("firstRun",false);
            editor.apply();
            Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        }

    }
}