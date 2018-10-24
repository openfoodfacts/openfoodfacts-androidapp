package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.JoinProperty;
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

    @ToMany(joinProperties = {
        @JoinProperty(name = "tag", referencedName = "ingredientTag")
    })
    private List<IngredientName> names;
/** Used to resolve relations */
@Generated(hash = 2040040024)
private transient DaoSession daoSession;
/** Used for active entity operations. */
@Generated(hash = 942581853)
private transient IngredientDao myDao;

@Generated(hash = 719810036)
public Ingredient(Long id, String tag) {
    this.id = id;
    this.tag = tag;
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

/** called by internal mechanisms, do not call yourself. */
@Generated(hash = 1386056592)
public void __setDaoSession(DaoSession daoSession) {
    this.daoSession = daoSession;
    myDao = daoSession != null ? daoSession.getIngredientDao() : null;
}
}
