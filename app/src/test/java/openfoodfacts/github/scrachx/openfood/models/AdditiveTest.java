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
 * Tests for {@link Additive}
 */
public class AdditiveTest {

    private static final String ADDITIVE_NAME_NAME_1 = "AdditiveName";
    private static final String ADDITIVE_NAME_NAME_2 = "AdditiveName2";
    private static final AdditiveName ADDITIVE_NAME_1 = new AdditiveName(ADDITIVE_NAME_NAME_1);
    private static final AdditiveName ADDITIVE_NAME_2 = new AdditiveName(ADDITIVE_NAME_NAME_2);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private DaoSession mockDaoSession;

    @Mock
    private AdditiveDao mockAdditiveDao;

    @Mock
    private AdditiveNameDao mockAdditiveNameDao;

    private Additive mAdditive;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(mockDaoSession.getAdditiveNameDao()).thenReturn(mockAdditiveNameDao);
        when(mockDaoSession.getAdditiveDao()).thenReturn(mockAdditiveDao);
        when(mockAdditiveNameDao._queryAdditive_Names(any()))
                .thenReturn(Arrays.asList(ADDITIVE_NAME_1, ADDITIVE_NAME_2));

        mAdditive = new Additive();
    }

    @Test
    public void getNamesWithNullNamesAndNullDaoSession_throwsDaoException() {
        thrown.expect(DaoException.class);

        mAdditive.getNames();
    }

    @Test
    public void getNamesWithNullNamesAndNonNullDaoSession_setsNamesFromAdditiveNamesDao() {
        mAdditive.__setDaoSession(mockDaoSession);

        List<AdditiveName> names = mAdditive.getNames();

        assertEquals(2, names.size());
        assertEquals(ADDITIVE_NAME_NAME_1, names.get(0).getName());
        assertEquals(ADDITIVE_NAME_NAME_2, names.get(1).getName());
    }

    @Test
    public void deleteWithNullDao_throwsDaoException() {
        thrown.expect(DaoException.class);

        mAdditive.delete();
    }

    @Test
    public void deleteWithNonNullDao_callsDeleteOnDao() {
        mAdditive.__setDaoSession(mockDaoSession);

        mAdditive.delete();

        verify(mockAdditiveDao).delete(mAdditive);
    }

    @Test
    public void refreshWithNullDao_throwsDaoException() {
        thrown.expect(DaoException.class);

        mAdditive.refresh();
    }

    @Test
    public void refreshWithNonNullDao_callsRefreshOnDao() {
        mAdditive.__setDaoSession(mockDaoSession);

        mAdditive.refresh();

        verify(mockAdditiveDao).refresh(mAdditive);
    }

    @Test
    public void updateWithNullDao_throwsDaoException() {
        thrown.expect(DaoException.class);

        mAdditive.update();
    }

    @Test
    public void updateWithNonNullDao_callsUpdateOnDao() {
        mAdditive.__setDaoSession(mockDaoSession);

        mAdditive.update();

        verify(mockAdditiveDao).update(mAdditive);
    }
}
