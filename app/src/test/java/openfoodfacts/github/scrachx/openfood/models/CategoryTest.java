package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.DaoException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Category}
 */
public class CategoryTest {

    private static final String CATEGORY_TAG_1 = "Tag1";
    private static final String CATEGORY_TAG_2 = "Tag2";
    private static final String LANGUAGE_CODE_ENGLISH = "en";
    private static final String LANGUAGE_CODE_FRENCH = "fr";
    private static final String CATEGORY_NAME_NAME_1 = "Gummy Bears";
    private static final String CATEGORY_NAME_NAME_2 = "Ours Gommeux";
    private static final CategoryName CATEGORY_NAME_1 =
            new CategoryName(CATEGORY_TAG_1, LANGUAGE_CODE_ENGLISH, CATEGORY_NAME_NAME_1);
    private static final CategoryName CATEGORY_NAME_2 =
            new CategoryName(CATEGORY_TAG_2, LANGUAGE_CODE_FRENCH, CATEGORY_NAME_NAME_2);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private DaoSession mockDaoSession;

    @Mock
    private CategoryDao mockCategoryDao;

    @Mock
    private CategoryNameDao mockCategoryNameDao;

    private Category mCategory;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(mockDaoSession.getCategoryDao()).thenReturn(mockCategoryDao);
        when(mockDaoSession.getCategoryNameDao()).thenReturn(mockCategoryNameDao);
        when(mockCategoryNameDao._queryCategory_Names(any()))
                .thenReturn(Arrays.asList(CATEGORY_NAME_1, CATEGORY_NAME_2));

        mCategory = new Category();
    }

    @Test
    public void getNamesWithNullNamesAndNullDaoSession_throwsDaoException() {
        thrown.expect(DaoException.class);

        mCategory.getNames();
    }

    @Test
    public void getNamesWithNullNamesAndNonNullDaoSession_setsNamesFromCategoryNameDao() {
        mCategory.__setDaoSession(mockDaoSession);

        List<CategoryName> names = mCategory.getNames();

        assertEquals(2, names.size());
        assertEquals(CATEGORY_TAG_1, names.get(0).getCategoryTag());
        assertEquals(LANGUAGE_CODE_ENGLISH, names.get(0).getLanguageCode());
        assertEquals(CATEGORY_NAME_NAME_1, names.get(0).getName());
        assertEquals(CATEGORY_TAG_2, names.get(1).getCategoryTag());
        assertEquals(LANGUAGE_CODE_FRENCH, names.get(1).getLanguageCode());
        assertEquals(CATEGORY_NAME_NAME_2, names.get(1).getName());
    }

    @Test
    public void deleteWithNullDao_throwsDaoException() {
        thrown.expect(DaoException.class);

        mCategory.delete();
    }

    @Test
    public void deleteWithNonNullDao_callsDeleteOnDao() {
        mCategory.__setDaoSession(mockDaoSession);

        mCategory.delete();

        verify(mockCategoryDao).delete(mCategory);
    }

    @Test
    public void refreshWithNullDao_throwsDaoException() {
        thrown.expect(DaoException.class);

        mCategory.refresh();
    }

    @Test
    public void refreshWithNonNullDao_callsRefreshOnDao() {
        mCategory.__setDaoSession(mockDaoSession);

        mCategory.refresh();

        verify(mockCategoryDao).refresh(mCategory);
    }

    @Test
    public void updateWithNullDao_throwsDaoException() {
        thrown.expect(DaoException.class);

        mCategory.update();
    }

    @Test
    public void updateWithNonNullDao_callsUpdateOnDao() {
        mCategory.__setDaoSession(mockDaoSession);

        mCategory.update();

        verify(mockCategoryDao).update(mCategory);
    }
}
