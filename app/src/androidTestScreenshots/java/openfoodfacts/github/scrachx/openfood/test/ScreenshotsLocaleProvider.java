package openfoodfacts.github.scrachx.openfood.test;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

import java.util.List;

public class ScreenshotsLocaleProvider {
    private static List<ScreenshotParameter> parameters;

    private List<ScreenshotParameter> getAllParameters() {
        if (parameters == null) {
            parameters = ScreenshotParametersProvider.createDefault();
        }
        return parameters;
    }

    private Predicate predicate = PredicateUtils.truePredicate();

    /**
     * @param predicate could be used to filter to use for a tests.
     */
    public void setPredicate(Predicate predicate) {
        this.predicate = predicate;
    }

    public List<ScreenshotParameter> getParameters() {
        final List<ScreenshotParameter> allLocales = getAllParameters();
        CollectionUtils.filter(allLocales, predicate);
        return allLocales;
    }
}
