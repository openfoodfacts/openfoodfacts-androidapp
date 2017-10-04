package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Kind of Product Image
 */
public enum ProductImageField {
    FRONT, INGREDIENTS, NUTRITION, OTHER;

    @Override
    @JsonValue
    public String toString() {
        return this.name().toLowerCase();
    }
}
