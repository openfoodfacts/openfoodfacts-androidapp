package openfoodfacts.github.scrachx.openfood.models;

import android.util.Log;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;

/**
 * Created by Lobster on 04.03.18.
 */

@Entity(indexes = {
        @Index(value = "languageCode, additiveTag", unique = true)
})
public class AdditiveName {

    @Id(autoincrement = true)
    Long id;

    private String additiveTag;

    private String languageCode;

    private String name;

    private String wikiDataId;

    private Boolean isWikiDataIdPresent;

    @Generated(hash = 1491450865)
    public AdditiveName(Long id, String additiveTag, String languageCode,
                        String name, String wikiDataId, Boolean isWikiDataIdPresent) {
        this.id = id;
        this.additiveTag = additiveTag;
        this.languageCode = languageCode;
        this.name = name;
        this.wikiDataId = wikiDataId;
        this.isWikiDataIdPresent = isWikiDataIdPresent;
    }

    @Keep
    public AdditiveName(String additiveTag, String languageCode,
                        String name, String wikiDataId) {
        this.additiveTag = additiveTag;
        this.languageCode = languageCode;
        this.name = name;
        this.wikiDataId = wikiDataId;
        this.isWikiDataIdPresent = true;
    }


    @Keep
    public AdditiveName(String additiveTag, String languageCode,
                        String name) {
        this.additiveTag = additiveTag;
        this.languageCode = languageCode;
        this.name = name;
        this.isWikiDataIdPresent = false;
    }

    @Keep
    public AdditiveName(String name) {
        this.name = name;
        this.isWikiDataIdPresent = false;
    }

    @Generated(hash = 1697057291)
    public AdditiveName() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdditiveTag() {
        return this.additiveTag;
    }

    public void setAdditiveTag(String additiveTag) {
        this.additiveTag = additiveTag;
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


    public Boolean getIsWikiDataIdPresent() {
        return this.isWikiDataIdPresent;
    }

    public void setWikiDataId(String wikiDataId) {
        this.wikiDataId = wikiDataId;
    }

    public void setIsWikiDataIdPresent(Boolean isWikiDataIdPresent) {
        this.isWikiDataIdPresent = isWikiDataIdPresent;
    }



}
