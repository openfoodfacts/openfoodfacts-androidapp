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

    @Generated(hash = 1400007293)
    public AllergenName(Long id, String allergenTag, String languageCode,
                        String name) {
        this.id = id;
        this.allergenTag = allergenTag;
        this.languageCode = languageCode;
        this.name = name;
    }

    @Keep
    public AllergenName(String allergenTag, String languageCode,
                        String name) {
        this.allergenTag = allergenTag;
        this.languageCode = languageCode;
        this.name = name;
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

}
