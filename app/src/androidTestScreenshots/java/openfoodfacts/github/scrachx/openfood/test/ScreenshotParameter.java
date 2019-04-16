package openfoodfacts.github.scrachx.openfood.test;

import java.util.List;
import java.util.Locale;

public class ScreenshotParameter {
    private String countryTag;
    private Locale locale;
    private String mainProductCode;
    private List<String> otherProductCodes;

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

    public String getMainProductCode() {
        return mainProductCode;
    }

    public void setMainProductCode(String mainProductCode) {
        this.mainProductCode = mainProductCode;
    }

    public List<String> getOtherProductCodes() {
        return otherProductCodes;
    }

    public void setOtherProductCodes(List<String> otherProductCodes) {
        this.otherProductCodes = otherProductCodes;
    }
}
