package openfoodfacts.github.scrachx.openfood.models;

import org.apache.commons.lang.StringUtils;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber;

public class NutrimentListItem {
    private final boolean displayVolumeHeader;
    private final String modifier;
    private final String servingValue;
    private final String title;
    private final String unit;
    private final String value;

    public NutrimentListItem(boolean displayVolumeHeader) {
        this.displayVolumeHeader = displayVolumeHeader;

        this.title = null;
        this.value = null;
        this.servingValue = null;
        this.unit = null;
        this.modifier = null;
    }

    /**
     * Use a round value for value and servingValue parameters
     *
     * @param title name of nutriment
     * @param value value of nutriment per 100g
     * @param servingValue value of nutriment per serving
     * @param unit unit of nutriment
     * @param modifier one of the following: "<", ">", or "~"
     */
    public NutrimentListItem(String title, String value, String servingValue, String unit,
                             String modifier) {
        this.title = title;
        this.value = getRoundNumber(value);
        this.servingValue = StringUtils.isBlank(servingValue) ? StringUtils.EMPTY : getRoundNumber(servingValue);
        this.unit = unit;
        this.modifier = modifier;

        this.displayVolumeHeader = false;
    }

    public boolean shouldDisplayVolumeHeader() {
        return displayVolumeHeader;
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
