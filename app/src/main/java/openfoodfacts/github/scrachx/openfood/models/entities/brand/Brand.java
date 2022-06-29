package openfoodfacts.github.scrachx.openfood.models.entities.brand;

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

import openfoodfacts.github.scrachx.openfood.models.DaoSession;


@Entity(indexes = {
    @Index(value = "tag", unique = true)
})
public class Brand {

    @Id(autoincrement = true)
    private Long id;

    @Unique
    private String tag;

    @Unique
    private String wikiDataId;

    private Boolean isWikiDataIdPresent;

    @ToMany(joinProperties = {
        @JoinProperty(name = "tag", referencedName = "brandTag")
    })
    private List<BrandName> names;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1427825859)
    private transient BrandDao myDao;

    public Brand(){
    }

    @Keep
    public Brand(String tag, List<BrandName> names, String wikiDataId) {
        this.tag = tag;
        this.names = names;
        this.wikiDataId = wikiDataId;
        this.isWikiDataIdPresent = true;
    }

    @Keep
    public Brand(String tag, List<BrandName> names) {
        this.tag = tag;
        this.names = names;
        this.isWikiDataIdPresent = false;
    }

    @Generated(hash = 435873667)
    public Brand(Long id, String tag, String wikiDataId,
            Boolean isWikiDataIdPresent) {
        this.id = id;
        this.tag = tag;
        this.wikiDataId = wikiDataId;
        this.isWikiDataIdPresent = isWikiDataIdPresent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getWikiDataId() {
        return wikiDataId;
    }

    public void setWikiDataId(String wikiDataId) {
        this.wikiDataId = wikiDataId;
    }

    public Boolean getWikiDataIdPresent() {
        return isWikiDataIdPresent;
    }

    public void setWikiDataIdPresent(Boolean wikiDataIdPresent) {
        isWikiDataIdPresent = wikiDataIdPresent;
    }

    public Boolean getIsWikiDataIdPresent() {
        return this.isWikiDataIdPresent;
    }

    public void setIsWikiDataIdPresent(Boolean isWikiDataIdPresent) {
        this.isWikiDataIdPresent = isWikiDataIdPresent;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1714612506)
    public List<BrandName> getNames() {
        if (names == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            BrandNameDao targetDao = daoSession.getBrandNameDao();
            List<BrandName> namesNew = targetDao._queryBrand_Names(tag);
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
    @Generated(hash = 538692092)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getBrandDao() : null;
    }

}
