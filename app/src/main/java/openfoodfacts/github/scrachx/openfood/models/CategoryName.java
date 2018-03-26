package openfoodfacts.github.scrachx.openfood.models;

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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategoryTag() {
        return this.categoryTag;
    }

    public void setCategoryTag(String categoryTag) {
        this.categoryTag = categoryTag;
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

    public String getWikiDataId() {
        if(this.wikiDataId==null){
            return "null";
        }
        return this.wikiDataId;
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

}
