package openfoodfacts.github.scrachx.openfood.models;

import android.support.annotation.NonNull;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber;

public class NutrimentItem {

    private final CharSequence title;
    private final CharSequence value;
    private final CharSequence servingValue;
    private final CharSequence unit;

    public NutrimentItem(CharSequence title, CharSequence value, CharSequence servingValue, CharSequence unit){
        this.title = title;
        this.value = value;
        this.servingValue = servingValue;
        this.unit = unit;
    }

    /**
     * Use a round value for value and servingValue parameters
     * @param title
     * @param value
     * @param servingValue
     * @param unit
     */
    public NutrimentItem(@NonNull String title, String value, String servingValue, String unit){
        this.title = title;
        this.value = getRoundNumber(value);
        this.servingValue = getRoundNumber(servingValue);
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
}
