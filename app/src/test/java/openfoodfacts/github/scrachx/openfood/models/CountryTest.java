package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.DaoException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.models.entities.country.Country;
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryName;

import static junit.framework.Assert.assertEquals;
import static openfoodfacts.github.scrachx.openfood.models.CountryNameTestData.GERMANY_EN;
import static openfoodfacts.github.scrachx.openfood.models.CountryNameTestData.GERMANY_FR;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Country}
 */
public class CountryTest {
    private final String COUNTRY_TAG = "code";
    private final CountryName GERMANY_IN_ENGLISH =
        new CountryName(COUNTRY_TAG, LANGUAGE_CODE_ENGLISH, GERMANY_EN);
    private final CountryName GERMANY_IN_FRENCH =
        new CountryName(COUNTRY_TAG, LANGUAGE_CODE_FRENCH, GERMANY_FR);
    @Mock
    private DaoSession mockDaoSession;
    @Mock
    private CountryDao mockCountryDao;
    @Mock
    private CountryNameDao mockCountryNameDao;
    private Country mCountry;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(mockDaoSession.getCountryDao()).thenReturn(mockCountryDao);
        when(mockDaoSession.getCountryNameDao()).thenReturn(mockCountryNameDao);
        when(mockCountryNameDao._queryCountry_Names(any()))
            .thenReturn(Arrays.asList(GERMANY_IN_ENGLISH, GERMANY_IN_FRENCH));
        mCountry = new Country();
    }

    @Test
    public void getNamesWithNullNamesAndNullDaoSession_throwsDaoException() {
        assertThrows(DaoException.class, () -> mCountry.getNames());
    }

    @Test
    public void getNamesWithNullNamesAndNonNullDaoSession_getsNamesFromCountryNamesDao() {
        mCountry.__setDaoSession(mockDaoSession);

        List<CountryName> names = mCountry.getNames();

        assertEquals(2, names.size());
        assertEquals(COUNTRY_TAG, names.get(0).getCountyTag());
        assertEquals(LANGUAGE_CODE_ENGLISH, names.get(0).getLanguageCode());
        assertEquals(GERMANY_EN, names.get(0).getName());

        assertEquals(COUNTRY_TAG, names.get(1).getCountyTag());
        assertEquals(LANGUAGE_CODE_FRENCH, names.get(1).getLanguageCode());
        assertEquals(GERMANY_FR, names.get(1).getName());
    }

    @Test
    public void deleteWithNullDaoSession_throwsDaoException() {
        assertThrows(DaoException.class, () -> mCountry.delete());
    }

    @Test
    public void deleteWithNonNullDaoSession_callsDeleteOnCountryDao() {
        mCountry.__setDaoSession(mockDaoSession);
        mCountry.delete();
        verify(mockCountryDao).delete(mCountry);
    }

    @Test
    public void refreshWithNullDaoSession_throwsDaoException() {
        assertThrows(DaoException.class, () -> mCountry.refresh());
    }

    @Test
    public void refreshWithNonNullDaoSession_callsRefreshOnCountryDao() {
        mCountry.__setDaoSession(mockDaoSession);
        mCountry.refresh();
        verify(mockCountryDao).refresh(mCountry);
    }

    @Test
    public void updateWithNullDaoSession_throwsDaoException() {
        assertThrows(DaoException.class, () -> mCountry.update());
    }

    @Test
    public void updateWithNonNullDaoSession_callsUpdateOnCountryDao() {
        mCountry.__setDaoSession(mockDaoSession);
        mCountry.update();
        verify(mockCountryDao).update(mCountry);
    }
}
