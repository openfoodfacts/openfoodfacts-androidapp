package openfoodfacts.github.scrachx.openfood.models;

public class NutrientLevelItem {

    private final String category;
    private final String value;
    private final String label;
    private final int icon;

    public NutrientLevelItem(String category, String value, String label, int icon){
        this.category = category;
        this.value = value;
        this.label = label;
        this.icon = icon;
    }

    public String getValue() {
        return value;
    }

    public String getCategory(){
        return this.category;
    }

    public int getIcon(){
        return this.icon;
    }

    public String getLabel() {
        return label;
    }
}
