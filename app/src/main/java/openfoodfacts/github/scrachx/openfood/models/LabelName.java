package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;

/**
 * Created by Lobster on 03.03.18.
 */

@Entity(indexes = {
        @Index(value = "languageCode, labelTag", unique = true)
})
public class LabelName {

    @Id(autoincrement = true)
    Long id;

    private String labelTag;

    private String languageCode;

    private String name;

    public LabelName() {
    }

    @Generated(hash = 274274142)
    public LabelName(Long id, String labelTag, String languageCode, String name) {
        this.id = id;
        this.labelTag = labelTag;
        this.languageCode = languageCode;
        this.name = name;
    }

    @Keep
    public LabelName(String labelTag, String languageCode, String name) {
        this.labelTag = labelTag;
        this.languageCode = languageCode;
        this.name = name;
    }

    @Keep
    public LabelName(String name) {
        this.name = name;
    }

    public String getLabelTag() {
        return this.labelTag;
    }

    public void setLabelTag(String labelTag) {
        this.labelTag = labelTag;
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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
