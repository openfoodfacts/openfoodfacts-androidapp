package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class InsightAnnotationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("status")
    private String status;
    @JsonProperty("description")
    private String description;

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

}