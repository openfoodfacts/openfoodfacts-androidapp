package openfoodfacts.github.scrachx.openfood.models.entities.additive;

import org.greenrobot.greendao.DaoException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.models.DaoSession;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Additive}
 */
@RunWith(MockitoJUnitRunner.class)
public class AdditiveTest {
    private static final String ADDITIVE_NAME_NAME_1 = "AdditiveName";
    private static final String ADDITIVE_NAME_NAME_2 = "AdditiveName2";
    private static final AdditiveName ADDITIVE_NAME_1 = new AdditiveName(ADDITIVE_NAME_NAME_1);
    private static final AdditiveName ADDITIVE_NAME_2 = new AdditiveName(ADDITIVE_NAME_NAME_2);
    @Mock
    private DaoSession mockDaoSession;
    @Mock
    private AdditiveDao mockAdditiveDao;
    @Mock
    private AdditiveNameDao mockAdditiveNameDao;
    private Additive mAdditive;

    @Before
    public void setup() {
        when(mockDaoSession.getAdditiveNameDao()).thenReturn(mockAdditiveNameDao);
        when(mockDaoSession.getAdditiveDao()).thenReturn(mockAdditiveDao);
        when(mockAdditiveNameDao._queryAdditive_Names(any()))
            .thenReturn(Arrays.asList(ADDITIVE_NAME_1, ADDITIVE_NAME_2));

        mAdditive = new Additive();
    }

    @Test
    public void getNamesWithNullNamesAndNullDaoSession_throwsDaoException() {
        assertThrows(DaoException.class, () -> mAdditive.getNames());
    }

    @Test
    public void getNamesWithNullNamesAndNonNullDaoSession_setsNamesFromAdditiveNamesDao() {
        mAdditive.__setDaoSession(mockDaoSession);

        List<AdditiveName> names = mAdditive.getNames();

        assertThat(names).hasSize(2);
        assertThat(names.get(0).getName()).isEqualTo(ADDITIVE_NAME_NAME_1);
        assertThat(names.get(1).getName()).isEqualTo(ADDITIVE_NAME_NAME_2);
    }

    @Test
    public void deleteWithNullDao_throwsDaoException() {
        assertThrows(DaoException.class, () -> mAdditive.delete());
    }

    @Test
    public void deleteWithNonNullDao_callsDeleteOnDao() {
        mAdditive.__setDaoSession(mockDaoSession);

        mAdditive.delete();

        verify(mockAdditiveDao).delete(mAdditive);
    }

    @Test
    public void refreshWithNullDao_throwsDaoException() {
        assertThrows(DaoException.class, () -> mAdditive.refresh());
    }

    @Test
    public void refreshWithNonNullDao_callsRefreshOnDao() {
        mAdditive.__setDaoSession(mockDaoSession);

        mAdditive.refresh();

        verify(mockAdditiveDao).refresh(mAdditive);
    }

    @Test
    public void updateWithNullDao_throwsDaoException() {
        assertThrows(DaoException.class, () -> mAdditive.update());
    }

    @Test
    public void updateWithNonNullDao_callsUpdateOnDao() {
        mAdditive.__setDaoSession(mockDaoSession);

        mAdditive.update();

        verify(mockAdditiveDao).update(mAdditive);
    }
}
