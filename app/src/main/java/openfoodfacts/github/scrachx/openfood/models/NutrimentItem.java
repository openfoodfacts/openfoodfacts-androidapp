package openfoodfacts.github.scrachx.openfood.models;

public class NutrimentItem {

    private final String title;
    private final String value;
    private final int color;

    public NutrimentItem(String title, String value, int color ){
        this.title = title;
        this.value = value;
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public int getColor() {
        return color;
    }
}
