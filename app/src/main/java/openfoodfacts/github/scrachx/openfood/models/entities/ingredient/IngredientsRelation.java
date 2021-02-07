package openfoodfacts.github.scrachx.openfood.models.entities.ingredient;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * Created by dobriseb on 2018.12.22.
 */
<<<<<<< HEAD
@Entity(indexes = {@Index(value = "parentTag, childTag", unique = true)})
=======
@Entity(
        indexes = {
                @Index(value = "parentTag, childTag", unique = true)
        }
)
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
public class IngredientsRelation {
    @NotNull
    private String parentTag;
    @NotNull
    private String childTag;
<<<<<<< HEAD

    @Generated(hash = 758005723)
    public IngredientsRelation(@NotNull String parentTag,
                               @NotNull String childTag) {
        this.parentTag = parentTag;
        this.childTag = childTag;
    }

    @Generated(hash = 2077281975)
    public IngredientsRelation() {
    }

    public String getParentTag() {
        return this.parentTag;
    }

    public void setParentTag(String parentTag) {
        this.parentTag = parentTag;
    }

    public String getChildTag() {
        return this.childTag;
    }

    public void setChildTag(String childTag) {
        this.childTag = childTag;
    }
=======
@Generated(hash = 758005723)
public IngredientsRelation(@NotNull String parentTag,
        @NotNull String childTag) {
    this.parentTag = parentTag;
    this.childTag = childTag;
}
@Generated(hash = 2077281975)
public IngredientsRelation() {
}
public String getParentTag() {
    return this.parentTag;
}
public void setParentTag(String parentTag) {
    this.parentTag = parentTag;
}
public String getChildTag() {
    return this.childTag;
}
public void setChildTag(String childTag) {
    this.childTag = childTag;
}
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
}
