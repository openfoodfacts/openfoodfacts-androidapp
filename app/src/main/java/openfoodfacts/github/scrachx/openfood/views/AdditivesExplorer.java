package openfoodfacts.github.scrachx.openfood.views;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.*;
import android.view.Menu;
import android.view.MenuItem;
import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.AdditiveNameDao;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.AdditivesAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.BottomNavigationListenerInstaller;
import org.greenrobot.greendao.async.AsyncSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdditivesExplorer extends BaseActivity implements AdditivesAdapter.ClickListener {
    private RecyclerView recyclerView;
    private List<AdditiveName> additives;
    private Toolbar toolbar;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additives_explorer);

        recyclerView = findViewById(R.id.additiveRecyclerView);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.additives);

        DaoSession daoSession = Utils.getAppDaoSession(this);
        AsyncSession asyncSessionAdditives = daoSession.startAsyncSession();
        AdditiveNameDao additiveNameDao = daoSession.getAdditiveNameDao();

        String languageCode = LocaleHelper.getLanguage(this);
        asyncSessionAdditives.queryList(additiveNameDao.queryBuilder()
            .where(AdditiveNameDao.Properties.LanguageCode.eq(languageCode))
            .where(AdditiveNameDao.Properties.Name.like("E%")).build());

        additives = new ArrayList<>();
        asyncSessionAdditives.setListenerMainThread(operation -> {
            additives = (List<AdditiveName>) operation.getResult();

            Collections.sort(additives, (additiveName, t1) -> {
                String s1 = additiveName.getName().toLowerCase().replace('x', '0').split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[1];
                String s2 = t1.getName().toLowerCase().replace('x', '0').split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[1];
                return Integer.valueOf(s1).compareTo(Integer.valueOf(s2));
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(AdditivesExplorer.this));
            recyclerView.setAdapter(new AdditivesAdapter(additives, AdditivesExplorer.this));
            recyclerView.addItemDecoration(new DividerItemDecoration(AdditivesExplorer.this, DividerItemDecoration.VERTICAL));
        });

        BottomNavigationListenerInstaller.install(bottomNavigationView, this, getBaseContext());
    }

    @Override
    public void onClick(int position, String name) {
        ProductBrowsingListActivity.startActivity(AdditivesExplorer.this, name, SearchType.ADDITIVE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.addtive_search));
        if (searchManager!=null && searchManager.getSearchableInfo(this.getComponentName()) != null) {

            searchView.setSearchableInfo(searchManager.getSearchableInfo(this.getComponentName()));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {

                    List<AdditiveName> additiveNames = new ArrayList<>();

                    for (AdditiveName additive : additives) {
                        if (additive.getName().toLowerCase().split(" - ").length > 1) {
                            String[] additiveContent = additive.getName().toLowerCase().split(" - ");
                            if (additiveContent[0].trim().contains(newText.trim().toLowerCase()) || additiveContent[1].trim().contains(newText.trim().toLowerCase())
                                || (additiveContent[0] + "-" + additiveContent[1]).contains(newText.trim().toLowerCase())) {
                                additiveNames.add(additive);
                            }
                        }
                    }

                    recyclerView.setAdapter(new AdditivesAdapter(additiveNames, AdditivesExplorer.this));
                    recyclerView.getAdapter().notifyDataSetChanged();

                    return false;
                }
            });
        }

        return super.onCreateOptionsMenu(menu);
    }
}


