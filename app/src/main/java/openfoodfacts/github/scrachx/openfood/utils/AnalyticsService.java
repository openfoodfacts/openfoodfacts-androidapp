package openfoodfacts.github.scrachx.openfood.utils;

import io.sentry.core.Sentry;
import openfoodfacts.github.scrachx.openfood.BuildConfig;

public class AnalyticsService {
    private static AnalyticsService instance;

    private AnalyticsService() {
    }

    public static synchronized AnalyticsService getInstance() {
        if (instance == null) {
            instance = new AnalyticsService();
        }
        return instance;
    }

    public void init() {
        Sentry.configureScope(scope -> scope.setTag("flavor", BuildConfig.FLAVOR));
    }

    public void log(String key, String value) {
        Sentry.setTag(key, value);
    }

    public void record(Exception exception) {
        Sentry.captureException(exception);
    }
}
