package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.orm.SugarRecord;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "code",
        "name",
        "risk"
})
public class Additive extends SugarRecord {

    private String code;
    private String name;
    private String risk;

    protected Additive(){}

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
    public String getCode() {
        return code;
    }

    /**
     *
     * @param code
     * The code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The risk
     */
    public String getRisk() {
        return risk;
    }

    /**
     *
     * @param risk
     * The risk
     */
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
