package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;

/**
 * Country code and translated name of an {@link openfoodfacts.github.scrachx.openfood.models.Allergen}
 *
 * @author Lobster 2018-03-04
 * @author ross-holloway94 2018-03-14
 */

@Entity(indexes = {
        @Index(value = "languageCode, allergenTag", unique = true)
})
public class AllergenName {

    @Id(autoincrement = true)
    Long id;
    private String allergenTag;
    private String languageCode;
    private String name;
    private String wikiDataId;
    private Boolean isWikiDataIdPresent = false;

    @Generated(hash = 1566725667)
    public AllergenName(Long id, String allergenTag, String languageCode, String name,
            String wikiDataId, Boolean isWikiDataIdPresent) {
        this.id = id;
        this.allergenTag = allergenTag;
        this.languageCode = languageCode;
        this.name = name;
        this.wikiDataId = wikiDataId;
        this.isWikiDataIdPresent = isWikiDataIdPresent;
    }

    @Keep
    public AllergenName(String allergenTag, String languageCode,
                        String name, String wikiDataId) {
        this.allergenTag = allergenTag;
        this.languageCode = languageCode;
        this.name = name;
        this.wikiDataId = wikiDataId;
        this.isWikiDataIdPresent = true;
    }

    @Keep
    public AllergenName(String allergenTag, String languageCode,
                        String name) {
        this.allergenTag = allergenTag;
        this.languageCode = languageCode;
        this.name = name;
        this.isWikiDataIdPresent = false;
    }

    @Generated(hash = 287009235)
    public AllergenName() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAllergenTag() {
        return this.allergenTag;
    }

    public void setAllergenTag(String allergenTag) {
        this.allergenTag = allergenTag;
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
        return id == null && allergenTag == null && languageCode == null && name == null;
    }

    public Boolean isNotNull() {
        return id != null && allergenTag != null && languageCode != null && name != null;
    }

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

    public Boolean getIsWikiDataIdPresent() {
        return this.isWikiDataIdPresent;
    }

    public void setIsWikiDataIdPresent(Boolean isWikiDataIdPresent) {
        this.isWikiDataIdPresent = isWikiDataIdPresent;
    }
}
