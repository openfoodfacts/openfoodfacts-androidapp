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
package openfoodfacts.github.scrachx.openfood.features

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.edit
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import androidx.preference.*
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.afollestad.materialdialogs.MaterialDialog
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.jobs.LoadTaxonomiesWorker
import openfoodfacts.github.scrachx.openfood.jobs.ProductUploaderWorker.Companion.scheduleProductUpload
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTagNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfigDao
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryName
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameDao
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType
import org.greenrobot.greendao.async.AsyncOperation
import org.greenrobot.greendao.async.AsyncOperationListener
import org.greenrobot.greendao.query.WhereCondition.StringCondition
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PreferencesFragment : PreferenceFragmentCompat(), INavigationItem, OnSharedPreferenceChangeListener {
    private val disp = CompositeDisposable()

    @Inject
    lateinit var daoSession: DaoSession

    @Inject
    lateinit var matomoAnalytics: MatomoAnalytics

    @Inject
    lateinit var localeManager: LocaleManager

    override val navigationDrawerListener: NavigationDrawerListener? by lazy {
        if (activity is NavigationDrawerListener) activity as NavigationDrawerListener
        else null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.findItem(R.id.action_search).isVisible = false
    }

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setHasOptionsMenu(true)

        val settings = requireActivity().getSharedPreferences("prefs", 0)

        initLanguageCell()

        requirePreference<ListPreference>(getString(R.string.pref_app_theme_key)).let {
            it.setEntries(R.array.application_theme_entries)
            it.setEntryValues(R.array.application_theme_entries)
            it.setOnPreferenceChangeListener { _, value ->
                when (value) {
                    getString(R.string.day) -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    getString(R.string.night) -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    getString(R.string.follow_system) -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                true
            }
        }

        requirePreference<Preference>(getString(R.string.pref_delete_history_key)).setOnPreferenceChangeListener { _, _ ->
            MaterialDialog.Builder(requireActivity()).run {
                content(R.string.pref_delete_history_dialog_content)
                positiveText(R.string.delete_txt)
                onPositive { _, _ ->
                    Toast.makeText(requireContext(), getString(R.string.pref_delete_history), Toast.LENGTH_SHORT).show()
                    SearchRecentSuggestions(requireContext(), SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE).clearHistory()
                }
                neutralText(R.string.dialog_cancel)
                onNeutral { dialog, _ -> dialog.dismiss() }
                show()
            }
            true
        }

        requirePreference<SwitchPreference>(getString(R.string.pref_scanner_type_key)).let {
            it.isVisible = BuildConfig.USE_MLKIT
            it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                if (newValue == true) {
                    MaterialDialog.Builder(requireActivity()).run {
                        title(R.string.preference_choose_scanner_dialog_title)
                        content(R.string.preference_choose_scanner_dialog_body)
                        positiveText(R.string.proceed)
                        onPositive { _, _ ->
                            it.isChecked = true
                            settings.edit { putBoolean(getString(R.string.pref_scanner_type_key), newValue as Boolean) }
                            Toast.makeText(requireActivity(), getString(R.string.changes_saved), Toast.LENGTH_SHORT).show()
                        }
                        negativeText(R.string.dialog_cancel)
                        onNegative { dialog, _ ->
                            dialog.dismiss()
                            it.isChecked = false
                            settings.edit { putBoolean(getString(R.string.pref_scanner_type_key), false) }
                        }
                        show()
                    }
                } else {
                    it.isChecked = false
                    settings.edit { putBoolean(getString(R.string.pref_scanner_type_key), newValue as Boolean) }
                    Toast.makeText(requireActivity(), getString(R.string.changes_saved), Toast.LENGTH_SHORT).show()
                }
                true
            }
        }


        val countryPreference = requirePreference<ListPreference>(getString(R.string.pref_country_key))

        val asyncSessionCountries = daoSession.startAsyncSession()
        val countryNameDao = daoSession.countryNameDao

        // Set query finish listener
        asyncSessionCountries.listenerMainThread = AsyncOperationListener { operation: AsyncOperation ->
            val countryNames = operation.result as List<CountryName>
            countryPreference.entries = countryNames.map { it.name }.toTypedArray()
            countryPreference.entryValues = countryNames.map { it.countyTag }.toTypedArray()
        }
        // Execute query
        asyncSessionCountries.queryList(countryNameDao.queryBuilder()
                .where(CountryNameDao.Properties.LanguageCode.eq(localeManager.getLanguage()))
                .orderAsc(CountryNameDao.Properties.Name).build())

        countryPreference.setOnPreferenceChangeListener { preference, newValue ->
            val country = newValue as String?
            settings.edit { putString(preference.key, country) }
            Toast.makeText(context, getString(R.string.changes_saved), Toast.LENGTH_SHORT).show()
            true
        }

        requirePreference<Preference>(getString(R.string.pref_contact_us_key)).setOnPreferenceChangeListener { _, _ ->
            try {
                startActivity(Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse(getString(R.string.off_mail))
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireActivity(), R.string.email_not_found, Toast.LENGTH_SHORT).show()
            }
            true
        }
        requirePreference<Preference>(getString(R.string.pref_rate_us_key)).setOnPreferenceChangeListener { _, _ ->
            try {
                startActivity(Intent(ACTION_VIEW,
                        Uri.parse("market://details?id=${requireActivity().packageName}")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${requireActivity().packageName}")))
            }
            true
        }

        requirePreference<Preference>(getString(R.string.pref_faq_key))
                .setOnPreferenceClickListener { openWebCustomTab(R.string.faq_url) }

        requirePreference<Preference>(getString(R.string.pref_terms_key))
                .setOnPreferenceClickListener { openWebCustomTab(R.string.terms_url) }

        requirePreference<Preference>(getString(R.string.pref_help_translate_key))
                .setOnPreferenceClickListener { openWebCustomTab(R.string.translate_url) }

        requirePreference<ListPreference>(getString(R.string.pref_energy_unit_key)).let {
            it.setEntries(R.array.energy_units)
            it.setEntryValues(R.array.energy_units)
            it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                settings.edit { putString(getString(R.string.pref_energy_unit_key), newValue as String?) }
                Toast.makeText(requireActivity(), getString(R.string.changes_saved), Toast.LENGTH_SHORT).show()
                true
            }
        }

        requirePreference<ListPreference>(getString(R.string.pref_volume_unit_key)).let {
            it.setEntries(R.array.volume_units)
            it.setEntryValues(R.array.volume_units)
            it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                settings.edit { putString(getString(R.string.pref_volume_unit_key), newValue as String?) }
                Toast.makeText(requireActivity(), getString(R.string.changes_saved), Toast.LENGTH_SHORT).show()
                true
            }
        }

        requirePreference<ListPreference>(getString(R.string.pref_resolution_key)).let {
            it.setEntries(R.array.upload_image)
            it.setEntryValues(R.array.upload_image)
            it.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                settings.edit { putString(getString(R.string.pref_resolution_key), newValue as String?) }
                Toast.makeText(requireActivity(), getString(R.string.changes_saved), Toast.LENGTH_SHORT).show()
                true
            }
        }

        // Disable photo mode for OpenProductFacts
        if (isFlavors(AppFlavors.OPF)) {
            requirePreference<Preference>(getString(R.string.pref_show_product_photos_key)).isVisible = false
        }

        // Preference to show version name
        requirePreference<Preference>(getString(R.string.pref_version_key)).let {
            try {
                val pInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
                val version = pInfo.versionName
                val versionCode = PackageInfoCompat.getLongVersionCode(pInfo)
                it.summary = "${getString(R.string.version_string)} $version ($versionCode)"
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(PreferencesFragment::class.simpleName, "onCreatePreferences", e)
            }

            if (isFlavors(OFF, OBF, OPFF)) {
                getAnalysisTagConfigs(daoSession)
            } else {
                preferenceScreen.removePreference(preferenceScreen.requirePreference(getString(R.string.pref_key_display)))
            }
        }

        requirePreference<SwitchPreference>(getString(R.string.pref_analytics_reporting_key)).let {
            it.setOnPreferenceChangeListener { _, newValue ->
                matomoAnalytics.setEnabled(newValue == true)
                true
            }
        }
    }

    private fun buildDisplayCategory(configs: List<AnalysisTagConfig>) {
        if (!isAdded) return

        val displayCategory = preferenceScreen.requirePreference<PreferenceCategory>(getString(R.string.pref_key_display))
        displayCategory.removeAll()
        preferenceScreen.addPreference(displayCategory)

        // If analysis tag is empty show "Load ingredient detection data" option in order to manually reload taxonomies
        if (configs.isNotEmpty()) {
            configs.forEach { config ->
                displayCategory.addPreference(CheckBoxPreference(this.context).apply {
                    key = config.type
                    setDefaultValue(true)
                    summary = null
                    summaryOn = null
                    summaryOff = null
                    title = getString(R.string.display_analysis_tag_status, config.typeName.lowercase(Locale.getDefault()))
                    setOnPreferenceChangeListener { _, newValue ->
                        val event = if (newValue == true) {
                            AnalyticsEvent.IngredientAnalysisEnabled(config.type)
                        } else {
                            AnalyticsEvent.IngredientAnalysisDisabled(config.type)
                        }
                        matomoAnalytics.trackEvent(event)
                        true
                    }
                })
            }
        } else {
            val preference = Preference(preferenceScreen.context).apply {
                setTitle(R.string.load_ingredient_detection_data)
                setSummary(R.string.load_ingredient_detection_data_summary)
                onPreferenceClickListener = OnPreferenceClickListener { pref ->
                    pref.onPreferenceClickListener = null
                    val request = OneTimeWorkRequest.from(LoadTaxonomiesWorker::class.java)

                    // The service will load server resources only if newer than already downloaded...
                    WorkManager.getInstance(requireContext()).let { manager ->
                        manager.enqueue(request)
                        manager.getWorkInfoByIdLiveData(request.id).observe(this@PreferencesFragment, { workInfo: WorkInfo? ->
                            if (workInfo != null) {
                                if (workInfo.state == WorkInfo.State.RUNNING) {
                                    pref.setTitle(R.string.please_wait)
                                    pref.setIcon(R.drawable.ic_cloud_download_black_24dp)
                                    pref.summary = null
                                    pref.widgetLayoutResource = R.layout.loading
                                } else if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                                    getAnalysisTagConfigs(daoSession)
                                }
                            }
                        })
                    }
                    true
                }
            }
            displayCategory.addPreference(preference)
        }
        displayCategory.isVisible = true
    }

    private fun openWebCustomTab(@StringRes resId: Int): Boolean {
        val customTabsIntent = CustomTabsIntent.Builder().build().apply {
            intent.putExtra("android.intent.extra.REFERRER", "android-app://${requireContext().packageName}".toUri())
        }
        CustomTabActivityHelper.openCustomTab(requireActivity(), customTabsIntent, getString(resId).toUri(), WebViewFallback())
        return true
    }

    @NavigationDrawerType
    override fun getNavigationDrawerType() = NavigationDrawerListener.ITEM_PREFERENCES

    override fun onResume() {
        super.onResume()
        try {
            (this.activity as? AppCompatActivity)?.supportActionBar!!.title = getString(R.string.action_preferences)
        } catch (e: NullPointerException) {
            throw IllegalStateException("Preference fragment not attached to AppCompatActivity.")
        }
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onDestroy() {
        disp.dispose()
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            getString(R.string.pref_enable_mobile_data_key) -> scheduleProductUpload(requireContext(), sharedPreferences)
        }
    }

    private fun getAnalysisTagConfigs(daoSession: DaoSession) {
        val language = localeManager.getLanguage()
        Single.fromCallable {
            val analysisTagConfigDao = daoSession.analysisTagConfigDao
            val analysisTagConfigs = analysisTagConfigDao.queryBuilder()
                    .where(StringCondition("1 GROUP BY type"))
                    .orderAsc(AnalysisTagConfigDao.Properties.Type).build().list()
            val analysisTagNameDao = daoSession.analysisTagNameDao
            analysisTagConfigs.forEach { config ->
                val type = "en:${config.type}"
                var analysisTagTypeName = analysisTagNameDao.queryBuilder().where(
                        AnalysisTagNameDao.Properties.AnalysisTag.eq(type),
                        AnalysisTagNameDao.Properties.LanguageCode.eq(language),
                ).unique()
                if (analysisTagTypeName == null) {
                    analysisTagTypeName = analysisTagNameDao.queryBuilder().where(
                            AnalysisTagNameDao.Properties.AnalysisTag.eq(type),
                            AnalysisTagNameDao.Properties.LanguageCode.eq("en")
                    ).unique()
                }
                config.typeName = if (analysisTagTypeName != null) analysisTagTypeName.name else config.type
            }
            analysisTagConfigs
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { configs: List<AnalysisTagConfig> -> buildDisplayCategory(configs) }
                .addTo(disp)
    }

    private fun initLanguageCell() {
        val localesWithNames = SupportedLanguages.codes()
            .map { lc ->
                val locale = LocaleUtils.parseLocale(lc)
                lc to locale.getDisplayName(locale).replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
            }

        requirePreference<ListPreference>(getString(R.string.pref_language_key)).let { preference ->
            preference.entries = localesWithNames.map { it.second }.toTypedArray()
            preference.entryValues = localesWithNames.map { it.first }.toTypedArray()
            preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, locale: Any? ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && locale != null) {
                    val configuration = requireActivity().resources.configuration
                    configuration.setLocale(LocaleUtils.parseLocale(locale as String))
                    Toast.makeText(context, getString(R.string.changes_saved), Toast.LENGTH_SHORT).show()
                    requireActivity().recreate()
                }
                true
            }
        }
    }

    companion object {
        const val LOGIN_PREF = "login"
        fun newInstance() = PreferencesFragment().apply { arguments = Bundle() }
    }
}
