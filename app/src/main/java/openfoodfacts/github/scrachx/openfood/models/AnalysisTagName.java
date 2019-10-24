package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;

/**
 * Country code and translated name of an {@link AnalysisTag}
 *
 * @author Rares
 */
@Entity(indexes = {
    @Index(value = "languageCode, analysisTag", unique = true)
})
public class AnalysisTagName {
    @Id(autoincrement = true)
    Long id;
    private String analysisTag;
    private String languageCode;
    private String name;
    private String showIngredients;

    @Generated(hash = 848913135)
    public AnalysisTagName(Long id, String analysisTag, String languageCode, String name,
                           String showIngredients) {
        this.id = id;
        this.analysisTag = analysisTag;
        this.languageCode = languageCode;
        this.name = name;
        this.showIngredients = showIngredients;
    }

    @Keep
    public AnalysisTagName(String allergenTag, String languageCode,
                           String name, String showIngredients) {
        this.analysisTag = allergenTag;
        this.languageCode = languageCode;
        this.name = name;
        this.showIngredients = showIngredients;
    }

    @Generated(hash = 1859726503)
    public AnalysisTagName() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAnalysisTag() {
        return this.analysisTag;
    }

    public void setAnalysisTag(String analysisTag) {
        this.analysisTag = analysisTag;
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
        return id == null && analysisTag == null && languageCode == null && name == null;
    }

    public Boolean isNotNull() {
        return id != null && analysisTag != null && languageCode != null && name != null;
    }

    public String getShowIngredients() {
        return this.showIngredients;
    }

    public void setShowIngredients(String showIngredients) {
        this.showIngredients = showIngredients;
    }
}
