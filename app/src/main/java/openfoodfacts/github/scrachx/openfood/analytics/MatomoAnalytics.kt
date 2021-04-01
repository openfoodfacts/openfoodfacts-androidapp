package openfoodfacts.github.scrachx.openfood.analytics

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.analyticsusage.AnalyticsUsageDialogFragment
import org.matomo.sdk.Matomo
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.TrackHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatomoAnalytics @Inject constructor(
        @ApplicationContext private val context: Context,
) {

    //TODO: change matomo url and id from properties
    private val tracker = TrackerBuilder
            .createDefault(BuildConfig.MATOMO_URL, 1)
            .build(Matomo.getInstance(context))
            .apply {
                isOptOut = PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(context.getString(R.string.pref_analytics_reporting_key), false)
            }
            .also {
                TrackHelper.track().download().with(it)
            }

    fun trackView(view: AnalyticsView) {
        TrackHelper.track()
                .screen(view.path)
                .with(tracker)
    }

    fun trackEvent(event: AnalyticsEvent) {
        TrackHelper.track()
                .event(event.category, event.action)
                .name(event.name)
                .value(event.value)
                .with(tracker)
    }

    fun showAnalyticsBottomSheetIfNeeded(childFragmentManager: FragmentManager) {
        if (PreferenceManager.getDefaultSharedPreferences(context).contains(context.getString(R.string.pref_analytics_reporting_key))) {
            //key already exists, do not show
            return
        }
        val bottomSheet = AnalyticsUsageDialogFragment()
        bottomSheet.show(childFragmentManager, AnalyticsUsageDialogFragment.TAG)
    }

    fun onAnalyticsEnabledToggled(enabled: Boolean) {
        tracker.isOptOut = !enabled
    }
}
