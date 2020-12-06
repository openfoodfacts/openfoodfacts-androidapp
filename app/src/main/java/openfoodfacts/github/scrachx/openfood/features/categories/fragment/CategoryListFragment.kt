package openfoodfacts.github.scrachx.openfood.features.categories.fragment

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.view.*
import android.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.disposables.CompositeDisposable
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.dagger.component.FragmentComponent
import openfoodfacts.github.scrachx.openfood.databinding.FragmentCategoryListBinding
import openfoodfacts.github.scrachx.openfood.features.MvvmFragment
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.features.shared.layouts.FastScroller
import openfoodfacts.github.scrachx.openfood.features.viewmodel.category.CategoryFragmentViewModel
import openfoodfacts.github.scrachx.openfood.utils.SearchSuggestionProvider
import java.util.*
import javax.inject.Inject

class CategoryListFragment : MvvmFragment<CategoryFragmentViewModel, FragmentComponent?>() {
    var fastScroller: FastScroller? = null

    @Inject
    override lateinit var viewModel: CategoryFragmentViewModel

    private var _binding: FragmentCategoryListBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategoryListBinding.inflate(layoutInflater, container, false)
        fastScroller = binding.fastScroller
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.recycler.setHasFixedSize(true)
        binding.recycler.layoutManager = LinearLayoutManager(context)
        binding.recycler.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL))
        binding.viewModel = viewModel
        fastScroller!!.setRecyclerView(binding.recycler)
        binding.recycler.viewTreeObserver.addOnGlobalLayoutListener {
            if ((binding.viewModel as CategoryFragmentViewModel).filteredCategories.get()!!.isEmpty()) {
                fastScroller!!.visibility = View.GONE
            } else {
                fastScroller!!.visibility = View.VISIBLE
                // check for an empty item in the start of the list
                if (viewModel.filteredCategories.get()!![0].name!!.isEmpty()) {
                    viewModel.filteredCategories.get()!!.removeAt(0)
                    binding.recycler.adapter!!.notifyItemRemoved(0)
                    binding.recycler.adapter!!.notifyItemRangeChanged(0, binding.recycler.adapter!!.itemCount)
                }
            }
        }
        binding.offlineView.findViewById<View>(R.id.buttonToRefresh).setOnClickListener { viewModel.refreshCategories() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchMenuItem = menu.findItem(R.id.action_search)
        val searchView = searchMenuItem.actionView as SearchView
        // TODO: 26/07/2020 use resources
        searchView.queryHint = "Search for a food category"
        if (searchManager.getSearchableInfo(requireActivity().componentName) == null) {
            return
        }
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val suggestions = SearchRecentSuggestions(context,
                        SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE)
                suggestions.saveRecentQuery(query, null)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.searchCategories(newText.toLowerCase(Locale.getDefault()))
                return false
            }
        })
    }


    override fun createComponent() =
            (requireActivity() as BaseActivity).activityComponent!!.plusFragmentComponent()!!

    override fun inject() = component!!.inject(this)

    override fun bindProperties(compositeDisposable: CompositeDisposable?) {
        // Not used here
    }
}