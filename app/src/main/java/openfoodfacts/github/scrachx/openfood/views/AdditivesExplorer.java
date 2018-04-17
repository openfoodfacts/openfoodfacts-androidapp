package openfoodfacts.github.scrachx.openfood.views;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.mikepenz.materialize.color.Material;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import openfoodfacts.github.scrachx.openfood.FastScroller;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Additives;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.views.adapters.AdditivesAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.RecyclerItemClickListener;

public class AdditivesExplorer extends BaseActivity implements AdditivesAdapter.ClickListener {


    RecyclerView recyclerView;
    ProgressBar progressBar;
    FastScroller fastScroller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additives_explorer);

        recyclerView = findViewById(R.id.additiveRecyclerView);
        progressBar = findViewById(R.id.additives_progress_bar);
        fastScroller = findViewById(R.id.additives_fast_scroller);


        progressBar.setVisibility(View.VISIBLE);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://world.openfoodfacts.org/additives.json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String myResponse = response.body().string();
                List<Additives> names = new ArrayList<>();

                try {
                    names = parseJson(myResponse);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final List<Additives> additives = names;
                AdditivesExplorer.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        progressBar.setVisibility(View.INVISIBLE);
                        recyclerView.setLayoutManager(new LinearLayoutManager(AdditivesExplorer.this));
                        recyclerView.setAdapter(new AdditivesAdapter(additives, AdditivesExplorer.this));
                        recyclerView.addItemDecoration(new DividerItemDecoration(AdditivesExplorer.this, DividerItemDecoration.VERTICAL));
                        fastScroller.setVisibility(View.VISIBLE);
                        fastScroller.setRecyclerView(recyclerView);

                    }
                });


            }

        });

    }

    private List<Additives> parseJson(String response) throws JSONException {


        JSONObject object;
        List<Additives> additives = new ArrayList<>();

        object = new JSONObject(response);

        JSONArray array = object.getJSONArray("tags");

        for (int i = 0; i < array.length(); i++) {


            JSONObject object1 = array.getJSONObject(i);
            Additives additive = new Additives();
            additive.setName(object1.getString("name"));
            additive.setUrl(object1.getString("url"));
            additives.add(additive);

        }

        Collections.sort(additives, new Comparator<Additives>() {
            @Override
            public int compare(Additives additives, Additives t1) {
                String s1 = additives.getName().toLowerCase().replace('x', '0').split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[1];
                String s2 = t1.getName().toLowerCase().replace('x', '0').split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[1];
                return Integer.valueOf(s1).compareTo(Integer.valueOf(s2));
            }
        });
        return additives;


    }

    @Override
    public void onclick(int position, String name) {
        ProductBrowsingListActivity.startActivity(AdditivesExplorer.this, name, SearchType.ADDITIVE);
    }
}


