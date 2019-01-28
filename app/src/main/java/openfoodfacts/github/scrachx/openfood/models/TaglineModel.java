package openfoodfacts.github.scrachx.openfood.models;

import java.io.Serializable;

public class TaglineModel implements Serializable {

    String message;
    String url;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
