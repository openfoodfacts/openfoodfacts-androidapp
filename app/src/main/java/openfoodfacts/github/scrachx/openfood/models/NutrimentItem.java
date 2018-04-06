package openfoodfacts.github.scrachx.openfood.models;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber;

public class NutrimentItem {

    private final CharSequence title;
    private final CharSequence value;
    private final CharSequence servingValue;
    private final CharSequence preparedValue;
    private final CharSequence preparedServingValue;
    private final CharSequence unit;

    public NutrimentItem(CharSequence title, CharSequence for100g, CharSequence servingValue, CharSequence unit, CharSequence forPrepared100g,
                         CharSequence forPreparedServing) {
        this.title = title;
        this.value = for100g;
        this.servingValue = servingValue;
        this.preparedValue = forPrepared100g;
        this.preparedServingValue = forPreparedServing;
        this.unit = unit;
    }

    /**
     * Use a round value for value and servingValue parameters
     *
     * @param title
     * @param for100g
     * @param servingValue
     * @param unit
     */
    public NutrimentItem(String title, String for100g, String servingValue, String unit, String forPrepared100g, String forPreparedServing) {
        this.title = title;
        this.value = getRoundNumber(for100g);
        this.servingValue = getRoundNumber(servingValue);
        this.preparedValue = getRoundNumber(forPrepared100g);
        this.preparedServingValue = getRoundNumber(forPreparedServing);
        this.unit = unit;
    }

    public CharSequence getTitle() {
        return title;
    }

    public CharSequence getValue() {
        return value;
    }

    public CharSequence getUnit() {
        return unit;
    }

    public CharSequence getServingValue() {
        return servingValue;
    }

    public CharSequence getPreparedValue() {
        return preparedValue;
    }

    public CharSequence getPreparedServingValue() {
        return preparedServingValue;
    }
}
