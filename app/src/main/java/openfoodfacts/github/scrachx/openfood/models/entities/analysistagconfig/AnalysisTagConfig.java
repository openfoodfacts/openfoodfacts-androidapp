package openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Transient;

import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTagName;

@Entity
public class AnalysisTagConfig {
    @Id(autoincrement = true)
    private Long id;
    @Index
    private String analysisTag;
    private String type;
    @Transient
    private String typeName;
    private String icon;
    private String color;
    @Transient
    private AnalysisTagName name;

    @Generated(hash = 492427864)
    public AnalysisTagConfig(Long id, String analysisTag, String type, String icon, String color) {
        this.id = id;
        this.analysisTag = analysisTag;
        this.type = type;
        this.icon = icon;
        this.color = color;
    }

    @Keep
    public AnalysisTagConfig(String analysisTag, String type, String icon, String color) {
        this.analysisTag = analysisTag;
        this.type = type;
        this.icon = icon;
        this.color = color;
    }

    @Generated(hash = 104416516)
    public AnalysisTagConfig() {
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

    public String getIcon() {
        return icon;
    }

    public String getIconUrl() {
        return "https://static.openfoodfacts.org/images/icons/" + icon + ".white.96x96.png";
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isNotNull() {
        return analysisTag != null && type != null && icon != null && color != null;
    }

    public AnalysisTagName getName() {
        return name;
    }

    public void setName(AnalysisTagName name) {
        this.name = name;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return this.typeName;
    }
}
