package openfoodfacts.github.scrachx.openfood.test;

import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ScreenshotParametersProvider {
    /**
     * @param countryTag see https://static.openfoodfacts.org/data/taxonomies/countries.json. the county tag is en:countryTag
     */
    public static ScreenshotParameter create(String countryTag, String languageCode, String mainProduct, String... otherProductsCode) {
        return create(countryTag, LocaleHelper.getLocale(languageCode), mainProduct, otherProductsCode);
    }

    /**
     * @param countryTag see https://static.openfoodfacts.org/data/taxonomies/countries.json. the county tag is en:countryTag
     */
    public static ScreenshotParameter create(String countryTag, Locale locale, String mainProduct, String... otherProductsCode) {
        ScreenshotParameter parameter = new ScreenshotParameter(countryTag, locale);
        parameter.setMainProductCode(mainProduct);
        parameter.setOtherProductCodes(Arrays.asList(otherProductsCode));
        return parameter;
    }

    public static List<ScreenshotParameter> createDefault() {
        List<ScreenshotParameter> res = new ArrayList<>();
        res.add(create("brazil", new Locale("pt", "BR"), "3017760002707", "3387390326574", "3179142054664"));
        res.add(create("france", Locale.FRANCE, "3017760002707", "3387390326574", "3179142054664"));
        res.add(create("united-kingdom", Locale.ENGLISH, "3017760002707", "3387390326574", "3179142054664"));

        return res;
    }
}
