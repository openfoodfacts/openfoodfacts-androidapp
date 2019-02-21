package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;
import org.greenrobot.greendao.DaoException;

@Entity(indexes = {@Index(value = "listName")})

public class ProductLists {
    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = "listName")
    private String listName;
    @Property(nameInDb = "numOfProducts")
    private long numOfProducts;

    @ToMany(referencedJoinProperty = "listId")
    private List<YourListedProduct> products;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 55943614)
    private transient ProductListsDao myDao;

    @Generated(hash = 935986243)
    public ProductLists(Long id, String listName, long numOfProducts) {
        this.id = id;
        this.listName = listName;
        this.numOfProducts = numOfProducts;
    }
    @Generated(hash = 968212054)
    public ProductLists() {
    }

    public ProductLists(String listName,long numOfProducts){
        this.listName=listName;
        this.numOfProducts=numOfProducts;
    }

    public Long getId() {
        return this.id;
    }
    public String getListName() {
        return this.listName;
    }
    public void setListName(String listName) {
        this.listName = listName;
    }
    public long getNumOfProducts() {
        return this.numOfProducts;
    }
    public void setNumOfProducts(long numOfProducts) {
        this.numOfProducts = numOfProducts;
    }
    public void setId(Long id) {
        this.id = id;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 372698848)
    public List<YourListedProduct> getProducts() {
        if (products == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            YourListedProductDao targetDao = daoSession.getYourListedProductDao();
            List<YourListedProduct> productsNew = targetDao
                    ._queryProductLists_Products(id);
            synchronized (this) {
                if (products == null) {
                    products = productsNew;
                }
            }
        }
        return products;
    }
    public void setProducts(List<YourListedProduct> yourListedProducts){
        this.products=yourListedProducts;
    }
    public void addProduct(YourListedProduct product){

        List<YourListedProduct> products=getProducts();
        products.add(product);
        setProducts(products);
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 513498032)
    public synchronized void resetProducts() {
        products = null;
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
    @Generated(hash = 674504607)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getProductListsDao() : null;
    }


}
