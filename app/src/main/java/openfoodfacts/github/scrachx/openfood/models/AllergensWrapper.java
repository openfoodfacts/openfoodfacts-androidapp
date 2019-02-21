package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.network.deserializers.AllergensWrapperDeserializer;

/**
 * JSON from URL https://ssl-api.openfoodfacts.org/data/taxonomies/allergens.json (top 14 allergens and substances)
 *
 * @author Lobster
 * @author ross-holloway94 2018-03-14
 */

@JsonDeserialize(using = AllergensWrapperDeserializer.class)
public class AllergensWrapper {

    private List<AllergenResponse> allergens;

    /**
     * @return A list of Allergen objects
     */
    public List<Allergen> map() {
        List<Allergen> entityAllergens = new ArrayList<>();
        for (AllergenResponse allergen : allergens) {
            entityAllergens.add(allergen.map());
        }

        return entityAllergens;
    }

    public void setAllergens(List<AllergenResponse> allergens) {
        this.allergens = allergens;
    }

    public List<AllergenResponse> getAllergens() {
        return allergens;
    }
}
