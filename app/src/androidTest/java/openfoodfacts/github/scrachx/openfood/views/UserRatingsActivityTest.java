package openfoodfacts.github.scrachx.openfood.views;

import android.support.test.rule.ActivityTestRule;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.RatingProduct;
import openfoodfacts.github.scrachx.openfood.models.RatingProductDao;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for the UserRatingsActivity.java
 */
public class UserRatingsActivityTest {
    @Rule
    public ActivityTestRule<UserRatingsActivity> mActivityTestRule =
            new ActivityTestRule<UserRatingsActivity>(UserRatingsActivity.class);

    private UserRatingsActivity mActivity = null;
    private RecyclerView recyclerView;
    private RatingProductDao mRatingDao;

    private List<RatingProduct> mRatings;
    private List<RatingProduct> dbRatings;
    private List<RatingProduct> orderedRatings;

    /**
     * setUp contains all the initializations required by all test cases
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        mActivity = mActivityTestRule.getActivity();
        recyclerView = (RecyclerView) mActivity.findViewById(R.id.ratings_recycler_view);

        mRatingDao = Utils.getAppDaoSession(mActivity).getRatingProductDao();

        mRatings = new ArrayList<>();
        mRatings.add(new RatingProduct((short)3, "", "123456789", "Crunch", "http://google.gr"));
        mRatings.add(new RatingProduct((short)5, "", "123456790", "Lacta", "http://google.gr"));
        mRatings.add(new RatingProduct((short)1, "", "123456791", "KitKat", "http://google.gr"));

        List<RatingProduct> dbItems = mRatingDao.loadAll();
        for(int i = 0; i < dbItems.size(); i++)
            mRatingDao.delete(dbItems.get(i));

        for(RatingProduct item : mRatings){
            mRatingDao.insertOrReplace(item);
        }

        dbRatings = new ArrayList<>();
        dbRatings = mRatingDao.loadAll();

        orderedRatings = new ArrayList<>();
        orderedRatings = mRatingDao.queryBuilder().orderDesc(RatingProductDao.Properties.Stars).list();
    }

    /**
     * Test case: ensure the activity launches with no errors
     */
    @Test
    public void UserRatingsActivity_LaunchTest(){
        assertNotNull(recyclerView);
    }

    /**
     * Test case: ensure the list' items that are created are the same number as
     * the number of the rating' items stored in the database.
     */
    @Test
    public void UserRatingsActivity_RatingSizeItemsTest(){
        assertTrue(orderedRatings.size() == mRatings.size());
    }

    /**
     * Test case: ensure that the info of the list's items that are created are the same identical to
     * the info of the rating' items stored in the database.
     */
    @Test
    public void UserRatingsActivity_RatingsEqualTest(){
        for(int i = 0; i < dbRatings.size(); i++){
            assertTrue("Failed on rating " + i, mRatings.get(i).getBarcode().equals(dbRatings.get(i).getBarcode()));
            assertTrue("Failed on rating " + i, mRatings.get(i).getStars() == dbRatings.get(i).getStars());
            assertTrue("Failed on rating " + i, mRatings.get(i).getComment().equals(dbRatings.get(i).getComment()));
        }
    }

    /**
     * Test case: ensure that the list's items are going to be shown at the right order.
     * Max stars first, min stars last.
     */
    @Test
    public void UserRatingsActivity_RatingOrderTest(){
        assertTrue(orderedRatings.get(0).getStars() == 5 && orderedRatings.get(2).getStars() == 1);
    }

    /**
     * tearDown method is going to be executed aster all test cases has been done.
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        for(RatingProduct item : mRatingDao.loadAll()){
            mRatingDao.delete(item);
        }
        mActivity = null;
    }
}