package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Questions implements Serializable {

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

    public void setInsightType(String insightType) {
        this.insightType = insightType;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setInsightId(String insightId) {
        this.insightId = insightId;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }
}