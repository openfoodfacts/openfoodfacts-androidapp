package openfoodfacts.github.scrachx.openfood.models;

import org.apache.commons.lang3.BooleanUtils;
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

    private String overexposureRisk;

    private String exposureMeanGreaterThanAdi;
    private String exposureMeanGreaterThanNoael;
    private String exposure95ThGreaterThanAdi;
    private String exposure95ThGreaterThanNoael;

    private String wikiDataId;

    private Boolean isWikiDataIdPresent;

    @Generated(hash = 1085820763)
    public AdditiveName(Long id, String additiveTag, String languageCode, String name, String overexposureRisk,
            String exposureMeanGreaterThanAdi, String exposureMeanGreaterThanNoael,
            String exposure95ThGreaterThanAdi, String exposure95ThGreaterThanNoael, String wikiDataId,
            Boolean isWikiDataIdPresent) {
        this.id = id;
        this.additiveTag = additiveTag;
        this.languageCode = languageCode;
        this.name = name;
        this.overexposureRisk = overexposureRisk;
        this.exposureMeanGreaterThanAdi = exposureMeanGreaterThanAdi;
        this.exposureMeanGreaterThanNoael = exposureMeanGreaterThanNoael;
        this.exposure95ThGreaterThanAdi = exposure95ThGreaterThanAdi;
        this.exposure95ThGreaterThanNoael = exposure95ThGreaterThanNoael;
        this.wikiDataId = wikiDataId;
        this.isWikiDataIdPresent = isWikiDataIdPresent;
    }

    @Keep
    public AdditiveName( String additiveTag, String languageCode,
                         String name, String overexposureRisk, String wikiDataId) {
        this.additiveTag = additiveTag;
        this.languageCode = languageCode;
        this.name = name;
        this.overexposureRisk = overexposureRisk;
        this.wikiDataId = wikiDataId;
        this.isWikiDataIdPresent = true;
    }


    @Keep
    public AdditiveName( String additiveTag, String languageCode,
                         String name, String overexposureRisk ) {
        this.additiveTag = additiveTag;
        this.languageCode = languageCode;
        this.name = name;
        this.overexposureRisk = overexposureRisk;
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


    public Boolean getIsWikiDataIdPresent() {
        return BooleanUtils.toBooleanDefaultIfNull(this.isWikiDataIdPresent,false);
    }

    public void setWikiDataId(String wikiDataId) {
        this.wikiDataId = wikiDataId;
    }

    public void setIsWikiDataIdPresent(Boolean isWikiDataIdPresent) {
        this.isWikiDataIdPresent = isWikiDataIdPresent;
    }

    public Boolean isNull() {
        return id == null && additiveTag == null && languageCode == null && name == null;
    }

    public Boolean isNotNull() {
        return id != null && additiveTag != null && languageCode != null && name != null;
    }

    public String getOverexposureRisk() {
        return this.overexposureRisk;
    }

    public void setOverexposureRisk( String overexposureRisk ) {
        this.overexposureRisk = overexposureRisk;
    }

    public String getExposureMeanGreaterThanAdi() {
        return this.exposureMeanGreaterThanAdi;
    }

    public void setExposureMeanGreaterThanAdi(String exposureMeanGreaterThanAdi) {
        this.exposureMeanGreaterThanAdi = exposureMeanGreaterThanAdi;
    }

    public String getExposureMeanGreaterThanNoael() {
        return this.exposureMeanGreaterThanNoael;
    }

    public void setExposureMeanGreaterThanNoael(String exposureMeanGreaterThanNoael) {
        this.exposureMeanGreaterThanNoael = exposureMeanGreaterThanNoael;
    }

    public String getExposure95ThGreaterThanAdi() {
        return this.exposure95ThGreaterThanAdi;
    }

    public void setExposure95ThGreaterThanAdi(String exposure95ThGreaterThanAdi) {
        this.exposure95ThGreaterThanAdi = exposure95ThGreaterThanAdi;
    }

    public String getExposure95ThGreaterThanNoael() {
        return this.exposure95ThGreaterThanNoael;
    }

    public void setExposure95ThGreaterThanNoael(String exposure95ThGreaterThanNoael) {
        this.exposure95ThGreaterThanNoael = exposure95ThGreaterThanNoael;
    }

    public void setExposureEvalMap( String exposure95ThGreaterThanAdi, String exposure95ThGreaterThanNoael, String exposureMeanGreaterThanAdi, String exposureMeanGreaterThanNoael )
    {
        this.exposure95ThGreaterThanAdi = exposure95ThGreaterThanAdi;
        this.exposure95ThGreaterThanNoael = exposure95ThGreaterThanNoael;
        this.exposureMeanGreaterThanAdi = exposureMeanGreaterThanAdi;
        this.exposureMeanGreaterThanNoael = exposureMeanGreaterThanNoael;
    }

    @Keep
    public boolean hasOverexposureData() {
        return overexposureRisk != null && !"no".equalsIgnoreCase(overexposureRisk);
    }
}
