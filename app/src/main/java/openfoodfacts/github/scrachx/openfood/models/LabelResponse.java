package openfoodfacts.github.scrachx.openfood.models;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Lobster on 03.03.18.
 */

public class LabelResponse {

    private String code;

    private Map<String, String> names;

    public LabelResponse(String code, Map<String, String> names) {
        this.code = code;
        this.names = names;
    }

    public Label map() {
        Label label = new Label(code, new ArrayList<>());
        for (Map.Entry<String, String> name : names.entrySet()) {
            label.getNames().add(new LabelName(label.getTag(), name.getKey(), name.getValue()));
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

    public void setNames(Map<String, String> names) {
        this.names = names;
    }
}
