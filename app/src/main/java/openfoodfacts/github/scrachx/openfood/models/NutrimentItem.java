package openfoodfacts.github.scrachx.openfood.models;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber;

public class NutrimentItem {

    private final CharSequence title;
    private final CharSequence value;
    private final CharSequence servingValue;
    private final CharSequence unit;
    private final CharSequence modifier;

    /**
     * @see #NutrimentItem(String, String, String, String, String)
     */
    public NutrimentItem(CharSequence title, CharSequence value, CharSequence servingValue,
                         CharSequence unit, CharSequence modifier) {
        this.title = title;
        this.value = value;
        this.servingValue = servingValue;
        this.unit = unit;
        this.modifier = modifier;
    }

    /**
     * Use a round value for value and servingValue parameters
     * @param title name of nutriment
     * @param value value of nutriment per 100g
     * @param servingValue value of nutriment per serving
     * @param unit unit of nutriment
     * @param modifier one of the following: "<", ">", or "~"
     */
    public NutrimentItem( String title, String value, String servingValue, String unit,
                          String modifier){
        this.title = title;
        this.value = getRoundNumber(value);
        this.servingValue = getRoundNumber(servingValue);
        this.unit = unit;
        this.modifier = modifier;
    }

    public CharSequence getTitle() {
        return title;
    }

    public CharSequence getValue() {
        return value;
    }

    /**
     * Get the modifier for this nutriment.
     *
     * @return one of the following: "<", ">", "~", ""
     */
    public CharSequence getModifier() {
        return modifier == null ? "" : modifier;
    }

    public CharSequence getUnit() {
        return unit;
    }

    public CharSequence getServingValue() {
        return servingValue;
    }
}
