package openfoodfacts.github.scrachx.openfood.test;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

import java.util.List;

public class ScreenshotsLocaleProvider {
    private List<ScreenshotParameter> parameters;

    private List<ScreenshotParameter> getParameters() {
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

    public List<ScreenshotParameter> getFilteredParameters() {
        final List<ScreenshotParameter> allLocales = getParameters();
        CollectionUtils.filter(allLocales, predicate);
        return allLocales;
    }
}
