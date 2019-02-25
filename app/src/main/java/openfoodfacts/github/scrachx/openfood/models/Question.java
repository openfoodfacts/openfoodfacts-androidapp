package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("barcode")
    private String code;
    @JsonProperty("type")
    private String type;
    @JsonProperty("value")
    private String value;
    @JsonProperty("question")
    private String question;
    @JsonProperty("insight_id")
    private String insightId;
    @JsonProperty("insight_type")
    private String insightType;
    @JsonProperty("source_image_url")
    private String sourceImageUrl;
    @JsonProperty("image_url")
    private String imageUrl;


    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getQuestion() {
        return question;
    }

    public String getInsightId() {
        return insightId;
    }

    public String getInsightType() {
        return insightType;
    }

    public String getSourceImageUrl() {
        return sourceImageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}