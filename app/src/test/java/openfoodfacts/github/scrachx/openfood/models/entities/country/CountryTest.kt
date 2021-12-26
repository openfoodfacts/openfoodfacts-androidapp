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
import org.mockito.kotlin.*

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
    private lateinit var country: Country

    @Before
    fun setup() {
        whenever(mockDaoSession.countryDao) doReturn mockCountryDao
        whenever(mockDaoSession.countryNameDao) doReturn mockCountryNameDao
        whenever(mockCountryNameDao._queryCountry_Names(anyOrNull())) doReturn listOf(GERMANY_IN_ENGLISH, GERMANY_IN_FRENCH)

        country = Country()
    }

    @Test
    fun `getting names with null names and null dao session throws DaoException`() {
        assertThrows(DaoException::class.java) { country.names }
    }

    @Test
    fun `getting names with null names`() {
        country.__setDaoSession(mockDaoSession)

        val names = country.names
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
    fun `delete with null daoSession throws DaoException`() {
        assertThrows(DaoException::class.java) { country.delete() }
    }

    @Test
    fun `delete calls delete on CountryDao`() {
        country.__setDaoSession(mockDaoSession)
        country.delete()

        verify(mockCountryDao).delete(country)
    }

    @Test
    fun `refresh with null daoSession throws DaoException`() {
        assertThrows(DaoException::class.java) { country.refresh() }
    }

    @Test
    fun `refresh calls refresh on CountryDao`() {
        country.__setDaoSession(mockDaoSession)
        country.refresh()
        verify(mockCountryDao).refresh(country)
    }

    @Test
    fun `update with null daoSession should throw DaoException`() {
        assertThrows(DaoException::class.java) { country.update() }
    }

    @Test
    fun `update calls update on CountryDao`() {
        country.__setDaoSession(mockDaoSession)
        country.update()
        verify(mockCountryDao).update(country)
    }

    companion object {
        private const val COUNTRY_TAG = "code"
        private val GERMANY_IN_ENGLISH = CountryName(COUNTRY_TAG, LANGUAGE_CODE_ENGLISH, GERMANY_EN)
        private val GERMANY_IN_FRENCH = CountryName(COUNTRY_TAG, LANGUAGE_CODE_FRENCH, GERMANY_FR)
    }
}