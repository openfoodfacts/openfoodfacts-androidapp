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

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.rx2.await
import net.steamcrafted.loadtoast.LoadToast
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAlertAllergensBinding
import openfoodfacts.github.scrachx.openfood.features.shared.NavigationBaseFragment
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType
import openfoodfacts.github.scrachx.openfood.utils.getAppPreferences
import openfoodfacts.github.scrachx.openfood.utils.isNetworkConnected
import javax.inject.Inject

/**
 * @see R.layout.fragment_alert_allergens
 */
@AndroidEntryPoint
class AllergensAlertFragment : NavigationBaseFragment() {

    private var _binding: FragmentAlertAllergensBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var matomoAnalytics: MatomoAnalytics

    @Inject
    lateinit var localeManager: LocaleManager

    private var enabledAllergens: MutableList<AllergenName>? = null
    private var allergensFromDao: List<AllergenName>? = null

    private lateinit var adapter: AllergensAdapter
    private val mSettings by lazy { requireActivity().getAppPreferences() }
    private val dataObserver by lazy { AllergensObserver() }
    private val appLang: String by lazy { localeManager.getLanguage() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setHasOptionsMenu(true)
        _binding = FragmentAlertAllergensBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AllergensAdapter {
            removeAllergen(it)
        }
        binding.allergensRecycle.adapter = adapter
        binding.allergensRecycle.layoutManager = LinearLayoutManager(view.context)
        binding.allergensRecycle.setHasFixedSize(true)
        adapter.registerAdapterDataObserver(dataObserver)
        dataObserver.onChanged()
        binding.btnAdd.setOnClickListener { addAllergen() }

        lifecycleScope.launchWhenStarted {
            awaitAll(
                async { updateEnabledAllergens(appLang) },
                async { updateAllergensFromDao(appLang) }
            )
            binding.btnAdd.isEnabled = true
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            (requireActivity() as? AppCompatActivity)?.supportActionBar?.setTitle(R.string.alert_drawer)
        } catch (e: IllegalStateException) {
            Log.e(AllergensAlertFragment::class.simpleName, "onResume", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (this::adapter.isInitialized) {
            adapter.unregisterAdapterDataObserver(dataObserver)
        }
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Set search as invisible
        menu.findItem(R.id.action_search).isVisible = false
    }

    /**
     * Add an allergen to be checked for when browsing products.
     */
    private fun addAllergen() {
        if (enabledAllergens != null && !allergensFromDao.isNullOrEmpty()) {

            lifecycleScope.launch {
                val allergens = withContext(Dispatchers.IO) {
                    productRepository.getAllergensByEnabledAndLanguageCode(false, appLang).await()
                }.sortedBy { it.name }

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.title_dialog_alert)
                    .setItems(allergens.map { it.name }.toTypedArray()) { _, position ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            productRepository.setAllergenEnabled(allergens[position].allergenTag, true).await()
                        }
                        enabledAllergens?.let {
                            it.add(allergens[position])
                            adapter.updateItems(it)
                        }
                        binding.allergensRecycle.scrollToPosition(adapter.itemCount - 1)
                        matomoAnalytics.trackEvent(AnalyticsEvent.AllergenAlertCreated(allergens[position].allergenTag))
                    }.show()
            }

        } else if (requireContext().isNetworkConnected()) {
            val lt = LoadToast(context)
                .setText(requireActivity().getString(R.string.toast_retrieving))
                .setBackgroundColor(ResourcesCompat.getColor(requireContext().resources, R.color.blue, requireContext().theme))
                .setTextColor(ResourcesCompat.getColor(requireActivity().resources, R.color.white, requireContext().theme))
                .show()

            // Retry to get allergens
            lifecycleScope.launch {
                try {
                    productRepository.getAllergens()
                } catch (err: Exception) {
                    mSettings.edit { putBoolean("errorAllergens", true) }
                    lt.error()
                }

                mSettings.edit { putBoolean("errorAllergens", false) }
                enabledAllergens?.let {
                    adapter.updateItems(it)
                }

                updateAllergens()

                // Retry
                addAllergen()

                // Close modal
                lt.success()
            }
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.title_dialog_alert)
                .setMessage(R.string.info_download_data_connection)
                .setNeutralButton(android.R.string.ok) { d, _ -> d.dismiss() }
                .show()
        }
    }

    private fun removeAllergen(allergen: AllergenName) {
        lifecycleScope.launch {
            enabledAllergens?.let {
                it.remove(allergen)
                adapter.updateItems(it)
            }
            withContext(Dispatchers.IO) {
                productRepository.setAllergenEnabled(allergen.allergenTag, false).await()
            }
        }
    }

    /**
     * Retrieve modified list of allergens from ProductRepository
     */
    private suspend fun updateAllergens() {
        updateEnabledAllergens(appLang)
        updateAllergensFromDao(appLang)
    }

    private suspend fun updateAllergensFromDao(language: String) {
        allergensFromDao = productRepository.getAllergensByLanguageCode(language)
    }

    private suspend fun updateEnabledAllergens(language: String) {
        enabledAllergens = productRepository.getAllergensByEnabledAndLanguageCode(true, language).await().toMutableList()
    }

    @NavigationDrawerType
    override fun getNavigationDrawerType() = NavigationDrawerListener.ITEM_ALERT


    /**
     * Data observer of the Recycler Views
     */
    internal inner class AllergensObserver : AdapterDataObserver() {
        override fun onChanged() = updateView()
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = updateView()
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = updateView()

        private fun updateView() {
            val isListEmpty = adapter.itemCount == 0

            binding.emptyAllergensView.isVisible = isListEmpty
            binding.allergensRecycle.isGone = isListEmpty
        }
    }

    companion object {
        fun newInstance() = AllergensAlertFragment().apply { arguments = Bundle() }
    }
}
