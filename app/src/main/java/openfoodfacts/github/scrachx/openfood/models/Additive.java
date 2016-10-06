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

    public Additive(){}

    public Additive(String code, String name, String risk) {
        this.code = code.toUpperCase();
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
        this.code = code.toUpperCase();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Additive additive = (Additive) o;

        return code != null ? code.equals(additive.code) : additive.code == null;

    }

    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }
}
