package openfoodfacts.github.scrachx.openfood.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AllergensWrapper {

    @SerializedName("tags")
    private List<Allergen> allergens;

    public void setAllergens(List<Allergen> allergens) {
        this.allergens = allergens;
    }

    public List<Allergen> getAllergens() {
        return allergens;
    }
}
