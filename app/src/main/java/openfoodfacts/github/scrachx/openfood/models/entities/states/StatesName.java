package openfoodfacts.github.scrachx.openfood.models.entities.states;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;

@Entity(indexes = {@Index(value = "languageCode, statesTag", unique = true)})
public class StatesName {
    @Id(autoincrement = true)
    Long id;
    private String statesTag;
    private String languageCode;
    private String name;

    @Keep
    public StatesName(String statesTag, String languageCode,
                       String name) {
        this.statesTag = statesTag;
        this.languageCode = languageCode;
        this.name = name;
    }

    @Keep
    public StatesName(String name) {
        this.name = name;
    }

    @Generated(hash = 668579508)
    public StatesName(Long id, String statesTag, String languageCode, String name) {
        this.id = id;
        this.statesTag = statesTag;
        this.languageCode = languageCode;
        this.name = name;
    }

    @Generated(hash = 1699750489)
    public StatesName() {
    }


    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatesTag() {
        return this.statesTag;
    }

    public void setStatesTag(String statesTag) {
        this.statesTag = statesTag;
    }

    public String getLanguageCode() {
        return this.languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isNull() {
        return id == null && statesTag == null && languageCode == null && name == null;
    }

    public Boolean isNotNull() {
        return id != null && statesTag != null && languageCode != null && name != null;
    }
}
