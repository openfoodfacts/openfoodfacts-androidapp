package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaglineLanguageModel {

    @JsonProperty("language")
    private String language;
    @JsonProperty("data")
    private TaglineModel data;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public TaglineModel getTaglineModel() {
        return data;
    }

    public void setTaglineModel(TaglineModel data) {
        this.data = data;
    }
}
