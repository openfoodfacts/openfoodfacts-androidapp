package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;

/**
 * Created by dobriseb on 2018.10.15.
 */

@Entity(indexes = {
        @Index(value = "languageCode, ingredientTag", unique = true)
})
public class IngredientName {
    @Id(autoincrement = true)
    Long id;
    private String ingredientTag;
    private String languageCode;
    private String name;
@Generated(hash = 302289181)
public IngredientName(Long id, String ingredientTag, String languageCode,
        String name) {
    this.id = id;
    this.ingredientTag = ingredientTag;
    this.languageCode = languageCode;
    this.name = name;
}
@Keep
public IngredientName(String ingredientTag, String languageCode, String name) {
    this.ingredientTag = ingredientTag;
    this.languageCode = languageCode;
    this.name = name;
}
@Generated(hash = 177058686)
public IngredientName() {
}
public Long getId() {
    return this.id;
}
public void setId(Long id) {
    this.id = id;
}
public String getIngredientTag() {
    return this.ingredientTag;
}
public void setIngredientTag(String ingredientTag) {
    this.ingredientTag = ingredientTag;
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
