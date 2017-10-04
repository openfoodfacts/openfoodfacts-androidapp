package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "code",
        "name",
        "risk"
})
@Entity(indexes = {
        @Index(value = "code", unique = true)
})
public class Additive {

    @Id
    private Long id;
    private String code;
    private String name;
    private String risk;

    public Additive(){}

    @Generated(hash = 1600864133)
    public Additive(Long id, String code, String name, String risk) {
        this.id = id;
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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
