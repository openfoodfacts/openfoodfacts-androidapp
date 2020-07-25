package openfoodfacts.github.scrachx.openfood.utils;

import androidx.preference.PreferenceManager;

import org.matomo.sdk.Matomo;
import org.matomo.sdk.Tracker;
import org.matomo.sdk.TrackerBuilder;
import org.matomo.sdk.extra.TrackHelper;

import io.sentry.android.core.SentryAndroid;
import io.sentry.core.Sentry;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

public class AnalyticsService {
    private static AnalyticsService instance;
    private Tracker tracker;
    private boolean isCrashReportingEnabled;

    private AnalyticsService() {
        // isCrashReportingEnabled is not dynamic, as sentry can not be enabled / disabled, so it takes the value at startup, and changes will only be taken into account after an app restart
        isCrashReportingEnabled = PreferenceManager.getDefaultSharedPreferences(OFFApplication.getInstance()).getBoolean("privacyCrashReports", true);

        //TODO: pass matomo url and id from properties, with different values depending on flavor
        tracker = TrackerBuilder.createDefault("https://example.com/piwik.php", 1)
            .build(Matomo.getInstance(OFFApplication.getInstance()));
        tracker.setOptOut(!AnalyticsService.isAnalyticsEnabled());
    }

    public static synchronized AnalyticsService getInstance() {
        if (instance == null) {
            instance = new AnalyticsService();
        }
        return instance;
    }

    public void init() {
        if (isCrashReportingEnabled) {
            SentryAndroid.init(OFFApplication.getInstance());
            Sentry.configureScope(scope -> scope.setTag("flavor", BuildConfig.FLAVOR));
        }
        TrackHelper.track().download().with(tracker);
    }

    public void log(String key, String value) {
        if (isCrashReportingEnabled) {
            Sentry.setTag(key, value);
        }
    }

    public void record(Exception exception) {
        if (isCrashReportingEnabled) {
            Sentry.captureException(exception);
        }
    }

    public void trackView(AnalyticsView view) {
        TrackHelper.track()
            .screen(view.path)
            .with(tracker);
    }

    public void trackEvent(AnalyticsEvent event) {
        TrackHelper.track()
            .event(event.category, event.action)
            .name(event.name)
            .value(event.value)
            .with(tracker);
    }

    private static boolean isAnalyticsEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(OFFApplication.getInstance()).getBoolean("privacyAnalyticsReporting", false);
    }

    public void onAnalyticsEnabledToggled(boolean enabled) {
        tracker.setOptOut(!enabled);
    }
}
