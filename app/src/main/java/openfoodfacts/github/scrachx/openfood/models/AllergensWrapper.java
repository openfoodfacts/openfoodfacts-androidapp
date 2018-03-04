package openfoodfacts.github.scrachx.openfood.models;

import java.util.ArrayList;
import java.util.List;

public class AllergensWrapper {

    private List<AllergenResponse> allergens;

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
