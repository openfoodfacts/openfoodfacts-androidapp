package openfoodfacts.github.scrachx.openfood.models;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Lobster on 04.03.18.
 */

public class CountryResponse {

    private String tag;

    private Map<String, String> names;

    public CountryResponse(String code, Map<String, String> names) {
        this.tag = code;
        this.names = names;
    }

    public Country map() {
        Country country = new Country(tag, new ArrayList<>());
        for (Map.Entry<String, String> name : names.entrySet()) {
            country.getNames().add(new CountryName(country.getTag(), name.getKey(), name.getValue()));
        }

        return country;
    }

}
