package openfoodfacts.github.scrachx.openfood.features.additives

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.core.content.getSystemService
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityAdditivesExplorerBinding
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.utils.Intent
import openfoodfacts.github.scrachx.openfood.utils.SearchType
import java.util.*

@AndroidEntryPoint
// TODO: Use a ViewModel
class AdditiveListActivity : BaseActivity() {

    private val viewModel: AdditiveListViewModel by viewModels()

    private var _binding: ActivityAdditivesExplorerBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AdditivesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAdditivesExplorerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarInclude.toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.additives)
        binding.navigationBottomInclude.bottomNavigation.selectNavigationItem(0)
        binding.navigationBottomInclude.bottomNavigation.installBottomNavigation(this)

        viewModel.additives
            .flowWithLifecycle(lifecycle)
            .filterNot(List<AdditiveName>::isEmpty)
            .onEach(::updateAdditives)
            .launchIn(lifecycleScope)

        // Setup recyclerview
        binding.additiveRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.additiveRecyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL),
        )
        binding.additiveRecyclerView.adapter = AdditivesAdapter { onAdditiveClick(it) }
            .also { adapter = it }
    }

    private fun updateAdditives(additives: List<AdditiveName>) {
        adapter.additives = additives
    }

    private fun onAdditiveClick(name: String) {
        ProductSearchActivity.start(this, SearchType.ADDITIVE, name)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        val additives = viewModel.additives.value

        searchView.queryHint = getString(R.string.addtive_search)
        val searchManager = this.getSystemService<SearchManager>()!!

        if (searchManager.getSearchableInfo(this.componentName) != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(this.componentName))
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String) = false

                override fun onQueryTextChange(query: String): Boolean {
                    val suggestedAdditives = additives.filter { additive ->
                        if (additive.name.lowercase(Locale.getDefault()).split(" - ").size <= 1) {
                            return@filter false
                        }

                        val additiveContent = additive.name
                            .lowercase(Locale.getDefault())
                            .split(" - ")

                        val trimmedQuery = query
                            .trim { it <= ' ' }
                            .lowercase(Locale.getDefault())

                        return@filter trimmedQuery in additiveContent[0].trim { it <= ' ' }
                                || trimmedQuery in additiveContent[1].trim { it <= ' ' }
                                || trimmedQuery in "${additiveContent[0]}-${additiveContent[1]}"
                    }

                    updateAdditives(suggestedAdditives)
                    return false
                }
            })
        }
        return super.onCreateOptionsMenu(menu)
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent<AdditiveListActivity>(context))
        }
    }
}
