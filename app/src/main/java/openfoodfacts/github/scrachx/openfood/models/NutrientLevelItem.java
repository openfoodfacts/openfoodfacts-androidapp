package openfoodfacts.github.scrachx.openfood.models;

public class NutrientLevelItem {

    private final String title;
    private final int icon;

    public NutrientLevelItem(String title, int icon){
        this.title = title;
        this.icon = icon;
    }

    public String getTitle(){
        return this.title;
    }

    public int getIcon(){
        return this.icon;
    }

}
