package openfoodfacts.github.scrachx.openfood.features.changelog

import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.PackageInfoCompat
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.AnalyticsService
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLocaleFromContext

class ChangelogDialog : DialogFragment(R.layout.fragment_changelog) {

    companion object {
        private const val TAG = "changelog_dialog"
        private const val FORCE_SHOW_KEY = "force_show"
        private const val LAST_VERSION_CODE = "last_version_code"
        private const val URL_CROWDIN = "https://crowdin.com/project/openfoodfacts"

        fun newInstance(forceShow: Boolean): ChangelogDialog {
            val args = Bundle().apply {
                putBoolean(FORCE_SHOW_KEY, forceShow)
            }
            return ChangelogDialog().apply {
                arguments = args
            }
        }
    }

    private lateinit var translationHelpLabel: TextView
    private lateinit var recyclerView: RecyclerView
    private val compositeDisposable = CompositeDisposable()

    override fun getTheme(): Int = R.style.OFFTheme_NoActionBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        translationHelpLabel = view.findViewById(R.id.changelog_label_help)
        recyclerView = view.findViewById(R.id.changelog_recycler)
        view.findViewById<View>(R.id.changelog_button_close).setOnClickListener { dismiss() }

        applyWindowTweaks()
        setupTranslationHelpLabel()
        setupRecyclerView()
    }

    override fun onDestroyView() {
        compositeDisposable.clear()
        super.onDestroyView()
    }

    @Suppress("DEPRECATION")
    fun presentAutomatically(activity: AppCompatActivity) {
        arguments?.let {
            if (it.getBoolean(FORCE_SHOW_KEY, false)) {
                show(activity.supportFragmentManager, TAG)
            } else {
                try {
                    val lastVersionCode = getVersion(activity)
                    val packageInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
                    val currentVersionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
                    if (currentVersionCode >= 0 && currentVersionCode > lastVersionCode) {
                        show(activity.supportFragmentManager, TAG)
                        saveVersionCode(activity, currentVersionCode)
                    }
                } catch (ex: NameNotFoundException) {
                    AnalyticsService.record(ex)
                    Unit
                }
            }
        }
    }

    private fun setupTranslationHelpLabel() {
        val language = getLocaleFromContext(context).displayLanguage
        translationHelpLabel.text = getString(R.string.changelog_translation_help, language)
        translationHelpLabel.setOnClickListener { openDailyFoodFacts() }
    }

    private fun applyWindowTweaks() {
        dialog?.window?.run {
            decorView.setPadding(0, 0, 0, 0)
            attributes.gravity = Gravity.BOTTOM
            attributes.width = WindowManager.LayoutParams.MATCH_PARENT
            attributes.height = WindowManager.LayoutParams.MATCH_PARENT
            setWindowAnimations(R.style.ChangelogDialogAnimation)
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        val changelogService = ChangelogService(requireContext())
        compositeDisposable.add(
                changelogService
                        .observeChangelog()
                        .map { changelog ->
                            val itemList = mutableListOf<ChangelogListItem>()
                            changelog.versions.forEach { version ->
                                itemList.add(ChangelogListItem.Header(version.name, version.date))
                                version.items.forEach { item ->
                                    itemList.add(ChangelogListItem.Item("- $item"))
                                }
                            }
                            itemList
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { items -> recyclerView.adapter = ChangelogAdapter(items) },
                                { throwable -> AnalyticsService.record(throwable) }
                        )
        )
    }

    private fun openDailyFoodFacts() {
        val dailyFoodFactUri = Uri.parse(URL_CROWDIN)
        val customTabActivityHelper = CustomTabActivityHelper().apply {
            mayLaunchUrl(dailyFoodFactUri, null, null)
        }
        val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(
                requireActivity(),
                customTabActivityHelper.session,
        )
        CustomTabActivityHelper.openCustomTab(
                requireActivity(),
                customTabsIntent,
                dailyFoodFactUri,
                WebViewFallback()
        )
    }

    private fun saveVersionCode(activity: AppCompatActivity, versionCode: Long) {
        PreferenceManager.getDefaultSharedPreferences(activity)
                .edit()
                .putLong(LAST_VERSION_CODE, versionCode)
                .apply()
    }

    private fun getVersion(activity: AppCompatActivity): Long {
        return PreferenceManager.getDefaultSharedPreferences(activity).getLong(LAST_VERSION_CODE, 0)
    }
}
