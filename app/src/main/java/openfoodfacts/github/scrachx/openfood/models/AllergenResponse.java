package openfoodfacts.github.scrachx.openfood.models;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Lobster on 04.03.18.
 */

public class AllergenResponse {

    private String code;

    private Map<String, String> names;

    public AllergenResponse(String code, Map<String, String> names) {
        this.code = code;
        this.names = names;
    }

    public Allergen map() {
        Allergen allergen = new Allergen(code, new ArrayList<>());
        for (Map.Entry<String, String> name : names.entrySet()) {
            allergen.getNames().add(new AllergenName(allergen.getTag(), name.getKey(), name.getValue()));
        }

        return allergen;
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

    public void setNames(Map<String, String> names) {
        this.names = names;
    }

}
