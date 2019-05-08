package openfoodfacts.github.scrachx.openfood.test;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.PredicateUtils;

import java.util.List;

public class ScreenshotsLocaleProvider {
    private static List<ScreenshotParameter> parameters;

    private List<ScreenshotParameter> getAllParameters() {
        if (parameters == null) {
            parameters = ScreenshotParametersProvider.createDefault();
        }
        return parameters;
    }

    private Predicate<ScreenshotParameter> predicate = PredicateUtils.truePredicate();

    /**
     * @param predicate could be used to filter to use for a tests.
     */
    public void setPredicate(Predicate<ScreenshotParameter> predicate) {
        this.predicate = predicate;
    }

    public List<ScreenshotParameter> getParameters() {
        final List<ScreenshotParameter> allLocales = getAllParameters();
        CollectionUtils.filter(allLocales, predicate);
        return allLocales;
    }
}
