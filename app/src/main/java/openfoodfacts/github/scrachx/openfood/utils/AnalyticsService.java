package openfoodfacts.github.scrachx.openfood.utils;

import io.sentry.core.Sentry;
import openfoodfacts.github.scrachx.openfood.BuildConfig;

public class AnalyticsService {
    private AnalyticsService() {
    }

    public static void init() {
        Sentry.configureScope(scope -> scope.setTag("flavor", BuildConfig.FLAVOR));
    }

    public static void log(String key, String value) {
        Sentry.setTag(key, value);
    }

    public static void record(Exception exception) {
        Sentry.captureException(exception);
    }
}
