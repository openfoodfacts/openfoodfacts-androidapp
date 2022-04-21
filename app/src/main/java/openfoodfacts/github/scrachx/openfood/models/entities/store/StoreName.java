package openfoodfacts.github.scrachx.openfood.models.entities.store;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;

@Entity(indexes = {
    @Index(value = "languageCode, storeTag", unique = true)
})
public class StoreName {

    @Id(autoincrement = true)
    Long id;

    private String storeTag;

    private String languageCode;

    private String name;

    private String wikiDataId;

    private Boolean isWikiDataIdPresent;

    @Keep
    public StoreName(String storeTag, String languageCode, String name, String wikiDataId) {
        this.storeTag = storeTag;
        this.languageCode = languageCode;
        this.name = name;
        this.wikiDataId = wikiDataId;
        this.isWikiDataIdPresent = true;
    }

    @Keep
    public StoreName(String storeTag, String languageCode, String name) {
        this.storeTag = storeTag;
        this.languageCode = languageCode;
        this.name = name;
    }

    @Keep
    public StoreName(String name) {
        this.name = name;
    }

    @Generated(hash = 612140410)
    public StoreName(Long id, String storeTag, String languageCode, String name,
            String wikiDataId, Boolean isWikiDataIdPresent) {
        this.id = id;
        this.storeTag = storeTag;
        this.languageCode = languageCode;
        this.name = name;
        this.wikiDataId = wikiDataId;
        this.isWikiDataIdPresent = isWikiDataIdPresent;
    }

    @Generated(hash = 1609806649)
    public StoreName() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStoreTag() {
        return storeTag;
    }

    public void setStoreTag(String storeTag) {
        this.storeTag = storeTag;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setWikiDataId(String wikiDataId) {
        this.wikiDataId = wikiDataId;
    }

    public Boolean getWikiDataIdPresent() {
        return isWikiDataIdPresent;
    }

    public void setWikiDataIdPresent(Boolean wikiDataIdPresent) {
        isWikiDataIdPresent = wikiDataIdPresent;
    }

    public Boolean isNull() {
        return id == null && storeTag == null && languageCode == null && name == null;
    }

    public Boolean isNotNull() {
        return id != null && storeTag != null && languageCode != null && name != null;
    }

    public Boolean getIsWikiDataIdPresent() {
        return this.isWikiDataIdPresent;
    }

    public void setIsWikiDataIdPresent(Boolean isWikiDataIdPresent) {
        this.isWikiDataIdPresent = isWikiDataIdPresent;
    }
}
