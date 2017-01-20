package openfoodfacts.github.scrachx.openfood.models;

public class NutrimentItem {

    private final String title;
    private final String value;
    private final String servingValue;
    private final String unit;

    public NutrimentItem(String title, String value, String servingValue, String unit){
        this.title = title;
        this.value = value;
        this.servingValue = servingValue;
        this.unit = unit;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public String getServingValue() {
        return servingValue;
    }
}
