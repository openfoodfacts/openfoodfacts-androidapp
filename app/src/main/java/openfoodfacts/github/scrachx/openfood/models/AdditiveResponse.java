package openfoodfacts.github.scrachx.openfood.models;

import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Lobster on 04.03.18.
 */

public class AdditiveResponse {

    private String tag;

    private Map<String, String> names;

    private String wikiDataCode;
    private Boolean isWikiDataIdPresent = false;
    private String overexposureRisk;

    public AdditiveResponse(String tag, Map<String, String> names, String overexposureRisk, String wikiDataCode) {
        this.tag = tag;
        this.names = names;
        this.wikiDataCode = wikiDataCode;
        this.overexposureRisk = overexposureRisk;
        this.isWikiDataIdPresent = true;
    }

    public AdditiveResponse(String tag, Map<String, String> names, String overexposureRisk) {
        this.tag = tag;
        this.names = names;
        this.overexposureRisk = overexposureRisk;
        this.isWikiDataIdPresent = false;
    }

    public Additive map() {
        Additive additive;
        if (isWikiDataIdPresent) {
            additive = new Additive(tag, new ArrayList<>(), wikiDataCode, overexposureRisk);
            for (Map.Entry<String, String> name : names.entrySet()) {
                additive.getNames().add(new AdditiveName(additive.getTag(), name.getKey(), name.getValue(), overexposureRisk, wikiDataCode));
            }

        } else {
            additive = new Additive(tag, new ArrayList<>(), overexposureRisk);
            for (Map.Entry<String, String> name : names.entrySet()) {
                additive.getNames().add(new AdditiveName(additive.getTag(), name.getKey(), overexposureRisk, name.getValue()));
            }

        }

        return additive;
    }

}
