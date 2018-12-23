package openfoodfacts.github.scrachx.openfood.models;

import java.io.Serializable;

public class TaglineModel implements Serializable {

    String tagline;
    String url;

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
