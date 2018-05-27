package org.openfoodfacts.scanner.models;

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

    public AdditiveResponse(String tag, Map<String, String> names, String wikiDataCode) {
        this.tag = tag;
        this.names = names;
        this.wikiDataCode = wikiDataCode;
        this.isWikiDataIdPresent = true;
    }

    public AdditiveResponse(String tag, Map<String, String> names) {
        this.tag = tag;
        this.names = names;
        this.isWikiDataIdPresent = false;
    }

    public Additive map() {
        Additive additive;
        if (isWikiDataIdPresent) {
            additive = new Additive(tag, new ArrayList<>(), wikiDataCode);
            for (Map.Entry<String, String> name : names.entrySet()) {
                additive.getNames().add(new AdditiveName(additive.getTag(), name.getKey(), name.getValue(), wikiDataCode));
            }

        } else {
            additive = new Additive(tag, new ArrayList<>());
            for (Map.Entry<String, String> name : names.entrySet()) {
                additive.getNames().add(new AdditiveName(additive.getTag(), name.getKey(), name.getValue()));
            }

        }

        return additive;
    }

}
