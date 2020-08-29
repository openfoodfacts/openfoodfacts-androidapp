package openfoodfacts.github.scrachx.openfood.views;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.greenrobot.greendao.async.AsyncSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.ActivityAdditivesExplorerBinding;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveNameDao;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.AdditivesAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.CommonBottomListenerInstaller;

public class AdditivesExplorer extends BaseActivity implements AdditivesAdapter.ClickListener {
    private ActivityAdditivesExplorerBinding binding;
    private List<AdditiveName> additives;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    public static void start(Context context) {
        Intent starter = new Intent(context, AdditivesExplorer.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdditivesExplorerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarInclude.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.additives);

        DaoSession daoSession = Utils.getDaoSession();
        AsyncSession asyncSessionAdditives = daoSession.startAsyncSession();
        AdditiveNameDao additiveNameDao = daoSession.getAdditiveNameDao();

        String languageCode = LocaleHelper.getLanguage(this);
        asyncSessionAdditives.queryList(additiveNameDao.queryBuilder()
            .where(AdditiveNameDao.Properties.LanguageCode.eq(languageCode))
            .where(AdditiveNameDao.Properties.Name.like("E%")).build());

        additives = new ArrayList<>();
        asyncSessionAdditives.setListenerMainThread(operation -> {
            additives = (List<AdditiveName>) operation.getResult();

            Collections.sort(additives, (additive1, additive2) -> {
                String s1 = additive1.getName().toLowerCase().replace('x', '0').split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[1];
                String s2 = additive2.getName().toLowerCase().replace('x', '0').split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[1];
                return Integer.valueOf(s1).compareTo(Integer.valueOf(s2));
            });

            if (binding == null) {
                return;
            }
            binding.additiveRecyclerView.setLayoutManager(new LinearLayoutManager(AdditivesExplorer.this));
            binding.additiveRecyclerView.setAdapter(new AdditivesAdapter(additives, AdditivesExplorer.this));
            binding.additiveRecyclerView.addItemDecoration(new DividerItemDecoration(AdditivesExplorer.this, DividerItemDecoration.VERTICAL));
        });

        CommonBottomListenerInstaller.selectNavigationItem(binding.navigationBottomInclude.bottomNavigation, 0);
        CommonBottomListenerInstaller.install(this, binding.navigationBottomInclude.bottomNavigation);
    }

    @Override
    public void onClick(int position, String name) {
        ProductBrowsingListActivity.start(AdditivesExplorer.this, name, SearchType.ADDITIVE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) this.getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.addtive_search));
        if (searchManager != null && searchManager.getSearchableInfo(this.getComponentName()) != null) {

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

                    binding.additiveRecyclerView.setAdapter(new AdditivesAdapter(additiveNames, AdditivesExplorer.this));
                    binding.additiveRecyclerView.getAdapter().notifyDataSetChanged();

                    return false;
                }
            });
        }

        return super.onCreateOptionsMenu(menu);
    }
}


