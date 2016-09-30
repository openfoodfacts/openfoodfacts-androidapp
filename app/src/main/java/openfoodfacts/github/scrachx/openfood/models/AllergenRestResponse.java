package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Deserialize nested tags array as List of {@link Allergen}
 */
public class AllergenRestResponse {
    private List<Allergen> allergens;

    public AllergenRestResponse(@JsonProperty("tags") final List<Allergen> allergens) {
        this.allergens = allergens;
    }

    public List<Allergen> getAllergens() {
        return allergens;
    }
}
