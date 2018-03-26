package openfoodfacts.github.scrachx.openfood.models;

import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Lobster on 03.03.18.
 */

public class LabelResponse {


    private String code;
    private Map<String, String> names;
    private String wikiDataCode;
    private Boolean isWikiDataIdPresent = false;

    public LabelResponse(String code, Map<String, String> names, String wikiDataCode) {
        this.code = code;
        this.wikiDataCode = wikiDataCode;
        this.names = names;
        this.isWikiDataIdPresent = true;
    }

    public LabelResponse(String code, Map<String, String> names) {
        this.code = code;
        this.names = names;
        this.isWikiDataIdPresent = false;
    }

    public Label map() {
        Label label;
        if (isWikiDataIdPresent) {
            label = new Label(code, new ArrayList<>(), wikiDataCode);
            for (Map.Entry<String, String> name : names.entrySet()) {
                label.getNames().add(new LabelName(label.getTag(), name.getKey(), name.getValue(), wikiDataCode));
            }
        } else {
            label = new Label(code, new ArrayList<>());
            for (Map.Entry<String, String> name : names.entrySet()) {
                label.getNames().add(new LabelName(label.getTag(), name.getKey(), name.getValue()));
            }
        }

        return label;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, String> getNames() {
        return names;
    }

    public String getWikiDataCode() {
        return wikiDataCode;
    }

    public Boolean getWikiDataIdPresent() {
        return isWikiDataIdPresent;
    }

    public void setNames(Map<String, String> names) {
        this.names = names;
    }
}
