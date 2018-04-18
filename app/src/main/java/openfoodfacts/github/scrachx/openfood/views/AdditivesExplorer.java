package openfoodfacts.github.scrachx.openfood.views;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import openfoodfacts.github.scrachx.openfood.FastScroller;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.AdditiveNameDao;
import openfoodfacts.github.scrachx.openfood.repositories.IProductRepository;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.AdditivesAdapter;

public class AdditivesExplorer extends BaseActivity implements AdditivesAdapter.ClickListener {


    RecyclerView recyclerView;
    FastScroller fastScroller;
    AdditiveNameDao additiveNameDao;
    IProductRepository productRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additives_explorer);

        recyclerView = findViewById(R.id.additiveRecyclerView);
        fastScroller = findViewById(R.id.additives_fast_scroller);
        productRepository = ProductRepository.getInstance();

        additiveNameDao = Utils.getAppDaoSession(this).getAdditiveNameDao();
        List<AdditiveName> additivesNames = additiveNameDao.loadAll();
        String languageCode = Locale.getDefault().getLanguage();
        List<AdditiveName> additives = new ArrayList<>();
        Set<AdditiveName> hs = new HashSet<>();
        for (int i = 0; i < additivesNames.size(); i++) {
            String tag = additivesNames.get(i).getAdditiveTag();
            hs.add(productRepository.getAdditiveByTagAndLanguageCode(tag, languageCode));
        }
        additives.addAll(hs);
        //remove Colour - color
        additives.remove(397);

        Collections.sort(additives, new Comparator<AdditiveName>() {
            @Override
            public int compare(AdditiveName additiveName, AdditiveName t1) {
                String s1 = additiveName.getName().toLowerCase().replace('x', '0').split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[1];
                String s2 = t1.getName().toLowerCase().replace('x', '0').split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[1];
                return Integer.valueOf(s1).compareTo(Integer.valueOf(s2));
            }
        });

        // remove Exxx - Exxx food additive
        additives.remove(0);
        // Remove No12-n12
        additives.remove(0);

        recyclerView.setLayoutManager(new LinearLayoutManager(AdditivesExplorer.this));
        recyclerView.setAdapter(new AdditivesAdapter(additives, AdditivesExplorer.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(AdditivesExplorer.this, DividerItemDecoration.VERTICAL));
        fastScroller.setVisibility(View.VISIBLE);
        fastScroller.setRecyclerView(recyclerView);

    }


    @Override
    public void onclick(int position, String name) {
        ProductBrowsingListActivity.startActivity(AdditivesExplorer.this, name, SearchType.ADDITIVE);
    }
}


