package openfoodfacts.github.scrachx.openfood.models.entities.analysistag;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.entities.TaxonomyEntity;

@Entity
public class AnalysisTag implements TaxonomyEntity {
    @Id(autoincrement = true)
    private Long id;
    @Index
    private String tag;
    /**
     * If the analysis tag is being checked for by the user.
     */
    private Boolean enabled = true;
    @ToMany(joinProperties = {
        @JoinProperty(name = "tag", referencedName = "analysisTag")
    })
    private List<AnalysisTagName> names;
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1638664250)
    private transient AnalysisTagDao myDao;

    @Generated(hash = 1494809953)
    public AnalysisTag(Long id, String tag, Boolean enabled) {
        this.id = id;
        this.tag = tag;
        this.enabled = enabled;
    }

    @Keep
    public AnalysisTag(String tag, List<AnalysisTagName> names) {
        this.enabled = true;
        this.tag = tag;
        this.names = names;
    }

    @Generated(hash = 338711806)
    public AnalysisTag() {
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
    @Generated(hash = 1998124482)
    public List<AnalysisTagName> getNames() {
        if (names == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            AnalysisTagNameDao targetDao = daoSession.getAnalysisTagNameDao();
            List<AnalysisTagName> namesNew = targetDao._queryAnalysisTag_Names(tag);
            synchronized (this) {
                if (names == null) {
                    names = namesNew;
                }
            }
        }
        return names;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
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

    /**
     * @return True if the user is checking for this allergen.
     */
    public Boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 250411501)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getAnalysisTagDao() : null;
    }
}
