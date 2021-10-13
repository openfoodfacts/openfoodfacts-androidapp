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
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for [Country]
 */
@RunWith(MockitoJUnitRunner::class)
class CountryTest {

    @Mock
    private lateinit var mockDaoSession: DaoSession

    @Mock
    private lateinit var mockCountryDao: CountryDao

    @Mock
    private lateinit var mockCountryNameDao: CountryNameDao
    private lateinit var mCountry: Country

    @Before
    fun setup() {
        whenever(mockDaoSession.countryDao) doReturn mockCountryDao
        whenever(mockDaoSession.countryNameDao) doReturn mockCountryNameDao
        whenever(mockCountryNameDao._queryCountry_Names(any())) doReturn listOf(GERMANY_IN_ENGLISH, GERMANY_IN_FRENCH)
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

        val countryName1 = names[0]!!
        assertThat(countryName1.countyTag).isEqualTo(COUNTRY_TAG)
        assertThat(countryName1.languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(countryName1.name).isEqualTo(GERMANY_EN)

        val countryName2 = names[1]!!
        assertThat(countryName2.countyTag).isEqualTo(COUNTRY_TAG)
        assertThat(countryName2.languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
        assertThat(countryName2.name).isEqualTo(GERMANY_FR)
    }

    @Test
    fun deleteWithNullDaoSession_throwsDaoException() {
        assertThrows(DaoException::class.java) { mCountry.delete() }
    }

    @Test
    fun deleteWithNonNullDaoSession_callsDeleteOnCountryDao() {
        mCountry.__setDaoSession(mockDaoSession)
        mCountry.delete()
        verify(mockCountryDao).delete(mCountry)
    }

    @Test
    fun refreshWithNullDaoSession_throwsDaoException() {
        assertThrows(DaoException::class.java) { mCountry.refresh() }
    }

    @Test
    fun refreshWithNonNullDaoSession_callsRefreshOnCountryDao() {
        mCountry.__setDaoSession(mockDaoSession)
        mCountry.refresh()
        verify(mockCountryDao).refresh(mCountry)
    }

    @Test
    fun updateWithNullDaoSession_throwsDaoException() {
        assertThrows(DaoException::class.java) { mCountry.update() }
    }

    @Test
    fun updateWithNonNullDaoSession_callsUpdateOnCountryDao() {
        mCountry.__setDaoSession(mockDaoSession)
        mCountry.update()
        verify(mockCountryDao).update(mCountry)
    }

    companion object {
        private const val COUNTRY_TAG = "code"
        private val GERMANY_IN_ENGLISH = CountryName(COUNTRY_TAG, LANGUAGE_CODE_ENGLISH, GERMANY_EN)
        private val GERMANY_IN_FRENCH = CountryName(COUNTRY_TAG, LANGUAGE_CODE_FRENCH, GERMANY_FR)
    }
}