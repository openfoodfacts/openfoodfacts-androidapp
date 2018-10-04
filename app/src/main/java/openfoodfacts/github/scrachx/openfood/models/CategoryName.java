package openfoodfacts.github.scrachx.openfood.models;


import android.support.annotation.Nullable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;

/**
 * Created by Lobster on 04.03.18.
 */

@Entity(indexes = {
        @Index(value = "languageCode, categoryTag", unique = true)
})
public class CategoryName {

    @Id(autoincrement = true)
    Long id;

    private String categoryTag;

    private String languageCode;

    private String name;

    private String wikiDataId;

    private Boolean isWikiDataIdPresent;

    @Generated(hash = 1992623652)
    public CategoryName(Long id, String categoryTag, String languageCode,
                        String name, String wikiDataId, Boolean isWikiDataIdPresent) {
        this.id = id;
        this.categoryTag = categoryTag;
        this.languageCode = languageCode;
        this.name = name;
        this.wikiDataId = wikiDataId;
        this.isWikiDataIdPresent = isWikiDataIdPresent;
    }

    @Keep
    public CategoryName(String categoryTag, String languageCode,
                        String name, String wikiDataId) {
        this.categoryTag = categoryTag;
        this.languageCode = languageCode;
        this.name = name;
        this.wikiDataId = wikiDataId;
        this.isWikiDataIdPresent = true;
    }

    @Keep
    public CategoryName(String categoryTag, String languageCode,
                        String name) {
        this.categoryTag = categoryTag;
        this.languageCode = languageCode;
        this.name = name;
        this.isWikiDataIdPresent = false;
    }

    @Generated(hash = 2002473108)
    public CategoryName() {
    }

    @Nullable
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Nullable
    public String getCategoryTag() {
        return this.categoryTag;
    }

    public void setCategoryTag(String categoryTag) {
        this.categoryTag = categoryTag;
    }

    @Nullable
    public String getLanguageCode() {
        return this.languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    @Nullable
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getWikiDataId() {
        if(this.wikiDataId==null){
            return "null";
        }
        String res = this.wikiDataId;
        int startIndex = res.indexOf("en");
        startIndex= startIndex + 5;
        int lastIndex = res.lastIndexOf("\"");
        if(startIndex<3 || lastIndex < 3 ){
            return res;
        }
        res = res.substring(startIndex,lastIndex);
        return res;
    }

    public void setWikiDataId(String wikiDataId) {
        this.wikiDataId = wikiDataId;
    }

    @Nullable
    public Boolean getIsWikiDataIdPresent() {
        return this.isWikiDataIdPresent;
    }

    public void setIsWikiDataIdPresent(Boolean isWikiDataIdPresent) {
        this.isWikiDataIdPresent = isWikiDataIdPresent;
    }

    public Boolean isNull() {
        return id == null && categoryTag == null && languageCode == null && name == null;
    }

    public Boolean isNotNull() {
        return id != null && categoryTag != null && languageCode != null && name != null;
    }
}
