package openbeautyfacts.github.scrachx.openfood.models;

import com.orm.SugarRecord;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "code",
        "name",
        "risk"
})
public class Additive extends SugarRecord {

    @JsonProperty("code")
    private String code;
    @JsonProperty("name")
    private String name;
    @JsonProperty("risk")
    private String risk;

    public Additive() {

    }

    public Additive(String code, String name, String risk) {
        this.code = code;
        this.name = name;
        this.risk = risk;
    }

    /**
     *
     * @return
     * The code
     */
    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    /**
     *
     * @param code
     * The code
     */
    @JsonProperty("code")
    public void setCode(String code) {
        this.code = code;
    }

    /**
     *
     * @return
     * The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The risk
     */
    @JsonProperty("risk")
    public String getRisk() {
        return risk;
    }

    /**
     *
     * @param risk
     * The risk
     */
    @JsonProperty("risk")
    public void setRisk(String risk) {
        this.risk = risk;
    }

    @Override
    public String toString() {
        return "Additive{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", risk='" + risk + '\'' +
                '}';
    }
}
