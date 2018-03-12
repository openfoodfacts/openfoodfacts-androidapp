package openfoodfacts.github.scrachx.openfood.models;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Lobster on 04.03.18.
 */

public class AdditiveResponse {

    private String tag;

    private Map<String, String> names;

    public AdditiveResponse(String tag, Map<String, String> names) {
        this.tag = tag;
        this.names = names;
    }

    public Additive map() {
        Additive additive = new Additive(tag, new ArrayList<>());
        for (Map.Entry<String, String> name : names.entrySet()) {
            additive.getNames().add(new AdditiveName(additive.getTag(), name.getKey(), name.getValue()));
        }

        return additive;
    }

}
