package openfoodfacts.github.scrachx.openfood.models.entities.country;

import java.util.ArrayList;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.network.ApiFields;

/**
 * Created by Lobster on 04.03.18.
 */
// MUST represent the entire JSON object as given by the API
public class CountryResponse {
    private final Map<String, String> names;
    private final Map<String, String> cc2;
    private final Map<String, String> cc3;
    private final String tag;

    public CountryResponse(String code, Map<String, String> names, Map<String, String> cc2, Map<String, String> cc3) {
        this.tag = code;
        this.names = names;
        this.cc2 = cc2;
        this.cc3 = cc3;
    }

    public Country map() {
        Country country = new Country(tag,
            new ArrayList<>(),
            cc2.get(ApiFields.Defaults.DEFAULT_TAXO_PREFIX),
            cc3.get(ApiFields.Defaults.DEFAULT_TAXO_PREFIX));
        for (Map.Entry<String, String> name : names.entrySet()) {
            country.getNames().add(new CountryName(country.getTag(), name.getKey(), name.getValue()));
        }

        return country;
    }

}
