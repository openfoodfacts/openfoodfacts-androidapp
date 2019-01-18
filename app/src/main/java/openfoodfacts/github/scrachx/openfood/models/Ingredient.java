package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Unique;

import java.util.List;

/**
 * Created by dobriseb on 2018.10.15.
 */
@Entity(
        indexes = {
                @Index(value = "tag DESC", unique = true)
        }
)
public class Ingredient {
    @Id(autoincrement = true)
    private Long id;
    @Unique
    private String tag;

    @Unique
    private String wikiDataId;

    @ToMany(joinProperties = {
        @JoinProperty(name = "tag", referencedName = "ingredientTag")
    })
    private List<IngredientName> names;

    @ToMany(joinProperties = {
        @JoinProperty(name = "tag", referencedName = "parentTag")
    })
    private List<IngredientsRelation> parents;

    @ToMany(joinProperties = {
        @JoinProperty(name = "tag", referencedName = "childTag")
    })
    private List<IngredientsRelation> children;
/** Used to resolve relations */
@Generated(hash = 2040040024)
private transient DaoSession daoSession;
/** Used for active entity operations. */
@Generated(hash = 942581853)
private transient IngredientDao myDao;

@Generated(hash = 332885183)
public Ingredient(Long id, String tag, String wikiDataId) {
    this.id = id;
    this.tag = tag;
    this.wikiDataId = wikiDataId;
}
@Keep
public Ingredient(String tag, List<IngredientName> names, List<IngredientsRelation> parents, List<IngredientsRelation> children, String wikiDataId) {
    this.tag = tag;
    this.names = names;
    this.parents = parents;
    this.children = children;
    this.wikiDataId = wikiDataId;
}
@Keep
public Ingredient(String tag, List<IngredientName> names, List<IngredientsRelation> parents, List<IngredientsRelation> children) {
    this.tag = tag;
    this.names = names;
    this.parents = parents;
    this.children = children;
}

@Generated(hash = 1584798654)
public Ingredient() {
}

public Long getId() {
    return this.id;
}

public void setId(Long id) {
    this.id = id;
}

public String getTag() {
    return this.tag;
}

public void setTag(String tag) {
    this.tag = tag;
}

/**
 * To-many relationship, resolved on first access (and after reset).
 * Changes to to-many relations are not persisted, make changes to the target entity.
 */
@Generated(hash = 208701125)
public List<IngredientName> getNames() {
    if (names == null) {
        final DaoSession daoSession = this.daoSession;
        if (daoSession == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        IngredientNameDao targetDao = daoSession.getIngredientNameDao();
        List<IngredientName> namesNew = targetDao._queryIngredient_Names(tag);
        synchronized (this) {
            if (names == null) {
                names = namesNew;
            }
        }
    }
    return names;
}

/** Resets a to-many relationship, making the next get call to query for a fresh result. */
@Generated(hash = 1832659617)
public synchronized void resetNames() {
    names = null;
}

/**
 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
 * Entity must attached to an entity context.
 */
@Generated(hash = 128553479)
public void delete() {
    if (myDao == null) {
        throw new DaoException("Entity is detached from DAO context");
    }
    myDao.delete(this);
}

/**
 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
 * Entity must attached to an entity context.
 */
@Generated(hash = 1942392019)
public void refresh() {
    if (myDao == null) {
        throw new DaoException("Entity is detached from DAO context");
    }
    myDao.refresh(this);
}

/**
 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
 * Entity must attached to an entity context.
 */
@Generated(hash = 713229351)
public void update() {
    if (myDao == null) {
        throw new DaoException("Entity is detached from DAO context");
    }
    myDao.update(this);
}

public String getWikiDataId() {
    return this.wikiDataId;
}

public void setWikiDataId(String wikiDataId) {
    this.wikiDataId = wikiDataId;
}

/**
 * To-many relationship, resolved on first access (and after reset).
 * Changes to to-many relations are not persisted, make changes to the target entity.
 */
@Generated(hash = 1272953349)
public List<IngredientsRelation> getParents() {
    if (parents == null) {
        final DaoSession daoSession = this.daoSession;
        if (daoSession == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        IngredientsRelationDao targetDao = daoSession.getIngredientsRelationDao();
        List<IngredientsRelation> parentsNew = targetDao._queryIngredient_Parents(tag);
        synchronized (this) {
            if (parents == null) {
                parents = parentsNew;
            }
        }
    }
    return parents;
}
/** Resets a to-many relationship, making the next get call to query for a fresh result. */
@Generated(hash = 51086427)
public synchronized void resetParents() {
    parents = null;
}
/**
 * To-many relationship, resolved on first access (and after reset).
 * Changes to to-many relations are not persisted, make changes to the target entity.
 */
@Generated(hash = 1430180030)
public List<IngredientsRelation> getChildren() {
    if (children == null) {
        final DaoSession daoSession = this.daoSession;
        if (daoSession == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        IngredientsRelationDao targetDao = daoSession.getIngredientsRelationDao();
        List<IngredientsRelation> childrenNew = targetDao._queryIngredient_Children(tag);
        synchronized (this) {
            if (children == null) {
                children = childrenNew;
            }
        }
    }
    return children;
}
/** Resets a to-many relationship, making the next get call to query for a fresh result. */
@Generated(hash = 1590975152)
public synchronized void resetChildren() {
    children = null;
}
/** called by internal mechanisms, do not call yourself. */
@Generated(hash = 1386056592)
public void __setDaoSession(DaoSession daoSession) {
    this.daoSession = daoSession;
    myDao = daoSession != null ? daoSession.getIngredientDao() : null;
}
}
