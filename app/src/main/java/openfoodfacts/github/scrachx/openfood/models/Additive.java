package openfoodfacts.github.scrachx.openfood.models;

import org.apache.commons.lang3.BooleanUtils;
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



@Entity(indexes = {
        @Index(value = "tag", unique = true)
})
public class Additive {

    @Id(autoincrement = true)
    private Long id;

    @Unique
    private String tag;

    private String overexposureRisk;

    private String exposureMeanGreaterThanAdi;
    private String exposureMeanGreaterThanNoael;
    private String exposure95ThGreaterThanAdi;
    private String exposure95ThGreaterThanNoael;

    @Unique
    private String wikiDataId;

    private Boolean isWikiDataIdPresent;

    @ToMany(joinProperties = {
            @JoinProperty(name = "tag", referencedName = "additiveTag")
    })
    private List<AdditiveName> names;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1566396314)
    private transient AdditiveDao myDao;

    @Generated(hash = 575160496)
    public Additive(Long id, String tag, String overexposureRisk, String exposureMeanGreaterThanAdi,
            String exposureMeanGreaterThanNoael, String exposure95ThGreaterThanAdi,
            String exposure95ThGreaterThanNoael, String wikiDataId, Boolean isWikiDataIdPresent) {
        this.id = id;
        this.tag = tag;
        this.overexposureRisk = overexposureRisk;
        this.exposureMeanGreaterThanAdi = exposureMeanGreaterThanAdi;
        this.exposureMeanGreaterThanNoael = exposureMeanGreaterThanNoael;
        this.exposure95ThGreaterThanAdi = exposure95ThGreaterThanAdi;
        this.exposure95ThGreaterThanNoael = exposure95ThGreaterThanNoael;
        this.wikiDataId = wikiDataId;
        this.isWikiDataIdPresent = isWikiDataIdPresent;
    }

    @Keep
    public Additive( String tag, List<AdditiveName> names, String overexposureRisk, String wikiDataId) {
        this.names = names;
        this.tag = tag;
        this.overexposureRisk = overexposureRisk;
        this.wikiDataId = wikiDataId;
        this.isWikiDataIdPresent = true;
    }

    @Keep
    public Additive( String tag, List<AdditiveName> names, String overexposureRisk ) {
        this.tag = tag;
        this.names = names;
        this.overexposureRisk = overexposureRisk;
    }

    @Generated(hash = 55580656)
    public Additive() {
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

    public String getOverexposureRisk()
    {
        return overexposureRisk;
    }

    public void setOverexposureRisk( String overexposureRisk )
    {
        this.overexposureRisk = overexposureRisk;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 2093045715)
    public List<AdditiveName> getNames() {
        if (names == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            AdditiveNameDao targetDao = daoSession.getAdditiveNameDao();
            List<AdditiveName> namesNew = targetDao._queryAdditive_Names(tag);
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

    public String getWikiDataId() {
        return this.wikiDataId;
    }

    public void setWikiDataId(String wikiDataId) {
        this.wikiDataId = wikiDataId;
    }

    public Boolean getIsWikiDataIdPresent() {
        return BooleanUtils.toBooleanDefaultIfNull(this.isWikiDataIdPresent,false);
    }

    public void setIsWikiDataIdPresent(Boolean isWikiDataIdPresent) {
        this.isWikiDataIdPresent = isWikiDataIdPresent;
    }

    public String getExposureMeanGreaterThanAdi() {
        return this.exposureMeanGreaterThanAdi;
    }

    public void setExposureMeanGreaterThanAdi(String exposureMeanGreaterThanAdi) {
        this.exposureMeanGreaterThanAdi = exposureMeanGreaterThanAdi;
    }

    public String getExposureMeanGreaterThanNoael() {
        return this.exposureMeanGreaterThanNoael;
    }

    public void setExposureMeanGreaterThanNoael(String exposureMeanGreaterThanNoael) {
        this.exposureMeanGreaterThanNoael = exposureMeanGreaterThanNoael;
    }

    public String getExposure95ThGreaterThanAdi() {
        return this.exposure95ThGreaterThanAdi;
    }

    public void setExposure95ThGreaterThanAdi(String exposure95ThGreaterThanAdi) {
        this.exposure95ThGreaterThanAdi = exposure95ThGreaterThanAdi;
    }

    public String getExposure95ThGreaterThanNoael() {
        return this.exposure95ThGreaterThanNoael;
    }

    public void setExposure95ThGreaterThanNoael(String exposure95ThGreaterThanNoael) {
        this.exposure95ThGreaterThanNoael = exposure95ThGreaterThanNoael;
    }

    public void setExposureEvalMap( String exposure95ThGreaterThanAdi, String exposure95ThGreaterThanNoael, String exposureMeanGreaterThanAdi, String exposureMeanGreaterThanNoael )
    {
        this.exposure95ThGreaterThanAdi = exposure95ThGreaterThanAdi;
        this.exposure95ThGreaterThanNoael = exposure95ThGreaterThanNoael;
        this.exposureMeanGreaterThanAdi = exposureMeanGreaterThanAdi;
        this.exposureMeanGreaterThanNoael = exposureMeanGreaterThanNoael;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1169248577)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getAdditiveDao() : null;
    }
}
