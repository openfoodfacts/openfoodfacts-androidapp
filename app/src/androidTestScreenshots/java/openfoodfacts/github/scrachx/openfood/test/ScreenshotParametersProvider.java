package openfoodfacts.github.scrachx.openfood.test;

import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ScreenshotParametersProvider {
    public static ScreenshotParameter create(String countryTag, String languageCode, String... otherProductsCode) {
        return create(countryTag, LocaleHelper.getLocale(languageCode), otherProductsCode);
    }

    private static ScreenshotParameter create(String countryTag, Locale locale, String... otherProductsCode) {
        ScreenshotParameter parameter = new ScreenshotParameter(countryTag, locale);
        parameter.setProductCodes(Arrays.asList(otherProductsCode));
        return parameter;
    }

    public static List<ScreenshotParameter> createDefault() {
        List<ScreenshotParameter> res = new ArrayList<>();
        String[] productCodes = {"3017760002707", "3387390326574", "3366321051631", "3179142054664", "4001724819400", "3179142054725", "7613034365774",
            "30005501", "5412971096664", "9311627010183"};

        res.add(create("brazil", new Locale("pt", "BR"), productCodes));
//        res.add(create("france", Locale.FRANCE, "3017760002707", "3387390326574", "3179142054664"));
        res.add(create("united-kingdom", Locale.ENGLISH, "3017760002707", "3387390326574", "3179142054664"));

        return res;
    }
}
