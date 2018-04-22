package openfoodfacts.github.scrachx.openfood.models;


import android.util.Log;

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

    private String wikiDataId;

    private Boolean isWikiDataIdPresent;


    @Generated(hash = 794887766)
    public LabelName(Long id, String labelTag, String languageCode, String name,
                     String wikiDataId, Boolean isWikiDataIdPresent) {
        this.id = id;
        this.labelTag = labelTag;
        this.languageCode = languageCode;
        this.name = name;
        this.wikiDataId = wikiDataId;
        this.isWikiDataIdPresent = isWikiDataIdPresent;
    }

    @Keep
    public LabelName(String labelTag, String languageCode, String name, String wikiDataId) {
        this.labelTag = labelTag;
        this.languageCode = languageCode;
        this.name = name;
        this.wikiDataId = wikiDataId;
        this.isWikiDataIdPresent = true;
    }


    @Keep
    public LabelName(String labelTag, String languageCode, String name) {
        this.labelTag = labelTag;
        this.languageCode = languageCode;
        this.name = name;
        this.isWikiDataIdPresent = false;
    }

    @Keep
    public LabelName(String name) {
        this.name = name;
        this.isWikiDataIdPresent = false;
    }

    @Generated(hash = 14071323)
    public LabelName() {
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

    public String getWikiDataId() {
        if (this.wikiDataId == null) {
            return "null";
        }
        String res = this.wikiDataId;
        int startIndex = res.indexOf("en");
        startIndex = startIndex + 5;
        int lastIndex = res.lastIndexOf("\"");
        if (startIndex < 3 || lastIndex < 3) {
            return res;
        }
        res = res.substring(startIndex, lastIndex);
        return res;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public void setWikiDataId(String wikiDataId) {
        this.wikiDataId = wikiDataId;
    }

    public Boolean getIsWikiDataIdPresent() {
        return this.isWikiDataIdPresent;
    }

    public void setIsWikiDataIdPresent(Boolean isWikiDataIdPresent) {
        this.isWikiDataIdPresent = isWikiDataIdPresent;
    }

    public Boolean isNull() {
        return id == null && labelTag == null && languageCode == null && name == null;
    }

    public Boolean isNotNull() {
        return id != null && labelTag != null && languageCode != null && name != null;
    }
}
