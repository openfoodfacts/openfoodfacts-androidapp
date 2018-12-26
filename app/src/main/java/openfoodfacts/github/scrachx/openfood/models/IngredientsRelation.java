package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Unique;

import java.util.List;

/**
 * Created by dobriseb on 2018.12.22.
 */
@Entity(
        indexes = {
                @Index(value = "parentTag, childTag", unique = true)
        }
)
public class IngredientsRelation {
    @NotNull
    private String parentTag;
    @NotNull
    private String childTag;
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
}
