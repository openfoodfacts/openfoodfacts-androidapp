package openfoodfacts.github.scrachx.openfood.test;

import java.util.List;
import java.util.Locale;

public class ScreenshotParameter {
    private String countryTag;
    private Locale locale;
    private List<String> productCodes;

    public ScreenshotParameter(String countryTag, Locale language) {
        this.countryTag = countryTag;
        this.locale = language;
    }

    public String getCountryTag() {
        return countryTag;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getLanguage() {
        return locale.getLanguage();
    }

    @Override
    public String toString() {
        return "country: " + countryTag + "; language: " + locale;
    }


    public List<String> getProductCodes() {
        return productCodes;
    }

    public void setProductCodes(List<String> productCodes) {
        this.productCodes = productCodes;
    }
}
