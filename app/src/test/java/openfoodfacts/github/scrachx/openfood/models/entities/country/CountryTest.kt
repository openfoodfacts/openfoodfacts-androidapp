package openfoodfacts.github.scrachx.openfood.models.entities.country

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.GERMANY_EN
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.GERMANY_FR
import org.greenrobot.greendao.DaoException
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.Mockito.`when` as mockitoWhen

/**
 * Tests for [Country]
 */
@RunWith(MockitoJUnitRunner::class)
class CountryTest {

    @Mock
    private val mockDaoSession: DaoSession? = null

    @Mock
    private val mockCountryDao: CountryDao? = null

    @Mock
    private val mockCountryNameDao: CountryNameDao? = null
    private lateinit var mCountry: Country

    @Before
    fun setup() {
        mockitoWhen(mockDaoSession!!.countryDao).thenReturn(mockCountryDao)
        mockitoWhen(mockDaoSession.countryNameDao).thenReturn(mockCountryNameDao)
        mockitoWhen(mockCountryNameDao!!._queryCountry_Names(ArgumentMatchers.any()))
                .thenReturn(listOf(GERMANY_IN_ENGLISH, GERMANY_IN_FRENCH))
        mCountry = Country()
    }

    @Test
    fun getNamesWithNullNamesAndNullDaoSession_throwsDaoException() {
        assertThrows(DaoException::class.java) { mCountry.names }
    }

    @Test
    fun getNamesWithNullNamesAndNonNullDaoSession_getsNamesFromCountryNamesDao() {
        mCountry.__setDaoSession(mockDaoSession)
        val names = mCountry.names
        assertThat(names).hasSize(2)
        assertThat(names[0]!!.countyTag).isEqualTo(COUNTRY_TAG)
        assertThat(names[0]!!.languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(names[0]!!.name).isEqualTo(GERMANY_EN)
        assertThat(names[1]!!.countyTag).isEqualTo(COUNTRY_TAG)
        assertThat(names[1]!!.languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
        assertThat(names[1]!!.name).isEqualTo(GERMANY_FR)
    }

    @Test
    fun deleteWithNullDaoSession_throwsDaoException() {
        assertThrows(DaoException::class.java) { mCountry.delete() }
    }

    @Test
    fun deleteWithNonNullDaoSession_callsDeleteOnCountryDao() {
        mCountry.__setDaoSession(mockDaoSession)
        mCountry.delete()
        verify(mockCountryDao)!!.delete(mCountry)
    }

    @Test
    fun refreshWithNullDaoSession_throwsDaoException() {
        assertThrows(DaoException::class.java) { mCountry.refresh() }
    }

    @Test
    fun refreshWithNonNullDaoSession_callsRefreshOnCountryDao() {
        mCountry.__setDaoSession(mockDaoSession)
        mCountry.refresh()
        verify(mockCountryDao)!!.refresh(mCountry)
    }

    @Test
    fun updateWithNullDaoSession_throwsDaoException() {
        assertThrows(DaoException::class.java) { mCountry.update() }
    }

    @Test
    fun updateWithNonNullDaoSession_callsUpdateOnCountryDao() {
        mCountry.__setDaoSession(mockDaoSession)
        mCountry.update()
        verify(mockCountryDao)!!.update(mCountry)
    }

    companion object {
        private const val COUNTRY_TAG = "code"
        private val GERMANY_IN_ENGLISH = CountryName(COUNTRY_TAG, LANGUAGE_CODE_ENGLISH, GERMANY_EN)
        private val GERMANY_IN_FRENCH = CountryName(COUNTRY_TAG, LANGUAGE_CODE_FRENCH, GERMANY_FR)
    }
}