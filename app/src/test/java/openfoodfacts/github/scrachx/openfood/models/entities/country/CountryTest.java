package openfoodfacts.github.scrachx.openfood.models.entities.country;

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
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;
import static openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.GERMANY_EN;
import static openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.GERMANY_FR;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Country}
 */
@RunWith(MockitoJUnitRunner.class)
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

        assertThat(names).hasSize(2);
        assertThat(names.get(0).getCountyTag()).isEqualTo(COUNTRY_TAG);
        assertThat(names.get(0).getLanguageCode()).isEqualTo(LANGUAGE_CODE_ENGLISH);
        assertThat(names.get(0).getName()).isEqualTo(GERMANY_EN);

        assertThat(names.get(1).getCountyTag()).isEqualTo(COUNTRY_TAG);
        assertThat(names.get(1).getLanguageCode()).isEqualTo(LANGUAGE_CODE_FRENCH);
        assertThat(names.get(1).getName()).isEqualTo(GERMANY_FR);
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
