/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package openfoodfacts.github.scrachx.openfood.features.allergensalert

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import net.steamcrafted.loadtoast.LoadToast
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAlertAllergensBinding
import openfoodfacts.github.scrachx.openfood.features.adapters.AllergensAdapter
import openfoodfacts.github.scrachx.openfood.features.shared.NavigationBaseFragment
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType
import java.util.*

/**
 * @see R.layout.fragment_alert_allergens
 */
class AllergensAlertFragment : NavigationBaseFragment() {
    private var _binding: FragmentAlertAllergensBinding? = null
    private val binding get() = _binding!!

    private var mAllergensEnabled: MutableList<AllergenName>? = null
    private var mAllergensFromDao: List<AllergenName>? = null
    private var mAdapter: AllergensAdapter? = null
    private lateinit var mSettings: SharedPreferences
    private lateinit var productRepository: ProductRepository
    private val dispCont = CompositeDisposable()
    private lateinit var currentView: View
    private var mDataObserver: DataObserver? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setHasOptionsMenu(true)
        _binding = FragmentAlertAllergensBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        dispCont.dispose()
        binding.allergensRecycle.adapter!!.unregisterAdapterDataObserver(mDataObserver!!)
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Set search as invisible
        menu.findItem(R.id.action_search).isVisible = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // OnClick
        binding.btnAdd.setOnClickListener { addAllergen() }
        productRepository = ProductRepository
        mDataObserver = DataObserver()
        productRepository.getAllergensByEnabledAndLanguageCode(true, Locale.getDefault().language)
        val language = LocaleHelper.getLanguage(requireActivity())
        dispCont.add(productRepository.getAllergensByEnabledAndLanguageCode(true, language)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ allergens ->
                    mAllergensEnabled = allergens.toMutableList()
                    mAdapter = AllergensAdapter(productRepository, mAllergensEnabled)
                    binding.allergensRecycle.adapter = mAdapter
                    binding.allergensRecycle.layoutManager = LinearLayoutManager(view.context)
                    binding.allergensRecycle.setHasFixedSize(true)
                    mAdapter!!.registerAdapterDataObserver(mDataObserver!!)
                    mDataObserver!!.onChanged()
                })
                { e -> Log.e(AllergensAlertFragment::class.java.simpleName, "getAllergensByEnabledAndLanguageCode", e) })

        dispCont.add(productRepository.getAllergensByLanguageCode(language)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ allergens -> mAllergensFromDao = allergens })
                { e: Throwable? -> Log.e(AllergensAlertFragment::class.java.simpleName, "getAllergensByLanguageCode", e) })
        currentView = view
        mSettings = requireActivity().getSharedPreferences("prefs", 0)
    }

    /**
     * Add an allergen to be checked for when browsing products.
     */
    private fun addAllergen() {
        if (mAllergensEnabled != null && mAllergensFromDao?.isNotEmpty() == true) {
            dispCont.add(productRepository.getAllergensByEnabledAndLanguageCode(false, LocaleHelper.getLanguage(requireActivity()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ allergens ->

                        val allergensNames = allergens
                                .sortedWith { a1, a2 -> a1.name.compareTo(a2.name, ignoreCase = true) }
                                .map { it.name }

                        MaterialDialog.Builder(currentView.context).apply {
                            title(R.string.title_dialog_alert)
                            items(allergensNames)
                            itemsCallback { _, _, position, _ ->
                                productRepository.setAllergenEnabled(allergens[position].allergenTag, true)
                                mAllergensEnabled!!.add(allergens[position])
                                mAdapter!!.notifyItemInserted(mAllergensEnabled!!.size - 1)
                                binding.allergensRecycle.scrollToPosition(mAdapter!!.itemCount - 1)
                            }
                        }.show()
                    }) { it.printStackTrace() })
        } else {
            val cm = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
            if (isConnected) {
                val lt = LoadToast(context)
                lt.setText(requireActivity().getString(R.string.toast_retrieving))
                lt.setBackgroundColor(requireActivity().resources.getColor(R.color.blue))
                lt.setTextColor(requireActivity().resources.getColor(R.color.white))
                lt.show()
                dispCont.add(productRepository.getAllergens()
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError {
                            mSettings.edit { putBoolean("errorAllergens", true) }
                            lt.error()
                        }
                        .subscribe { _ ->
                            mSettings.edit { putBoolean("errorAllergens", false) }
                            mAdapter?.allergens = mAllergensEnabled!!
                            mAdapter?.notifyDataSetChanged()
                            updateAllergenDao()
                            addAllergen()
                            lt.success()
                        })
            } else {
                MaterialDialog.Builder(currentView.context).apply {
                    title(R.string.title_dialog_alert)
                    content(R.string.info_download_data_connection)
                    neutralText(R.string.txtOk)
                }.show()
            }
        }
    }

    /**
     * Retrieve modified list of allergens from ProductRepository
     */
    private fun updateAllergenDao() {
        val language = LocaleHelper.getLanguage(context)
        dispCont.add(productRepository.getAllergensByEnabledAndLanguageCode(true, language)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ allergens -> mAllergensEnabled = allergens.toMutableList() }) { e: Throwable? -> Log.e(AllergensAlertFragment::class.java.simpleName, "getAllergensByEnabledAndLanguageCode", e) })
        dispCont.add(productRepository.getAllergensByLanguageCode(language)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ allergens: List<AllergenName>? -> mAllergensFromDao = allergens }) { e: Throwable? -> Log.e(AllergensAlertFragment::class.java.simpleName, "getAllergensByLanguageCode", e) })
    }

    @NavigationDrawerType
    override fun getNavigationDrawerType(): Int {
        return NavigationDrawerListener.ITEM_ALERT
    }

    override fun onResume() {
        super.onResume()
        try {
            (requireActivity() as AppCompatActivity).supportActionBar!!.setTitle(getString(R.string.alert_drawer))
        } catch (e: IllegalStateException) {
            Log.e(AllergensAlertFragment::class.java.simpleName, "onResume", e)
        }
    }

    /**
     * Data observer of the Recycler Views
     */
    internal inner class DataObserver : AdapterDataObserver() {
        private fun setAppropriateView() {
            if (mAdapter != null) {
                val isListEmpty = mAdapter!!.itemCount == 0
                binding.emptyAllergensView.visibility = if (isListEmpty) View.VISIBLE else View.GONE
                binding.allergensRecycle.visibility = if (isListEmpty) View.GONE else View.VISIBLE
            }
        }

        override fun onChanged() {
            super.onChanged()
            setAppropriateView()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            setAppropriateView()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            setAppropriateView()
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = AllergensAlertFragment().apply {
            arguments = Bundle()
        }
    }
}