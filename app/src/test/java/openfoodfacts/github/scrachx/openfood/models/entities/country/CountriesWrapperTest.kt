package openfoodfacts.github.scrachx.openfood.models.entities.country

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.GERMANY_EN
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.GERMANY_FR
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.USA_EN
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.USA_FR
import org.junit.Before
import org.junit.Test

/**
 * Tests for [CountriesWrapper]
 */
class CountriesWrapperTest {
    private var mCountriesWrapper: CountriesWrapper? = null
    lateinit var countries: List<Country>
    lateinit var country1: Country
    lateinit var country2: Country

    @Before
    fun setup() {
        val usaNamesMap = hashMapOf(
                LANGUAGE_CODE_ENGLISH to USA_EN,
                LANGUAGE_CODE_FRENCH to USA_FR
        )
        val usaCC2Map = hashMapOf(LANGUAGE_CODE_ENGLISH to "US")
        val usaCC3Map = hashMapOf(LANGUAGE_CODE_ENGLISH to "USA")
        val countryResponse1 = CountryResponse(USA_COUNTRY_TAG, usaNamesMap, usaCC2Map, usaCC3Map)
        val germanyNamesMap: MutableMap<String, String> = hashMapOf(
                LANGUAGE_CODE_ENGLISH to GERMANY_EN,
                LANGUAGE_CODE_FRENCH to GERMANY_FR
        )

        val gerCC2Map = hashMapOf(LANGUAGE_CODE_ENGLISH to "DE")
        val gerCC3Map = hashMapOf(LANGUAGE_CODE_ENGLISH to "DEU")
        val countryResponse2 = CountryResponse(GERMANY_COUNTRY_TAG, germanyNamesMap, gerCC2Map, gerCC3Map)
        mCountriesWrapper = CountriesWrapper(listOf(countryResponse1, countryResponse2))

        countries = mCountriesWrapper!!.map().also {
            country1 = it[0]
            country2 = it[1]
        }
    }

    @Test
    fun map_returnsListOfCountries_ListHasCorrectSize() {
        assertThat(countries).hasSize(2)
    }

    @Test
    fun map_returnsListOfCountries_CountryTagsAreCorrect() {
        assertThat(country1.tag).isEqualTo(USA_COUNTRY_TAG)
        assertThat(country1.names).hasSize(2)
        assertThat(countries[1].tag).isEqualTo(GERMANY_COUNTRY_TAG)
        assertThat(countries[1].names).hasSize(2)
    }

    @Test
    fun map_returnsListOfCountries_SubCountryTagsAreCorrect() {
        assertThat(country1.names[0].countyTag).isEqualTo(USA_COUNTRY_TAG)
        assertThat(country1.names[1].countyTag).isEqualTo(USA_COUNTRY_TAG)
        assertThat(country2.names[0].countyTag).isEqualTo(GERMANY_COUNTRY_TAG)
        assertThat(country2.names[1].countyTag).isEqualTo(GERMANY_COUNTRY_TAG)
    }

    @Test
    fun map_returnsListOfCountries_SubLanguageCodesAreCorrect() {
        assertThat(country1.names[0].languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(country1.names[1].languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
        assertThat(country2.names[0].languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(country2.names[1].languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
    }

    @Test
    fun map_returnsListOfCountries_SubNamesAreCorrect() {
        assertThat(country1.names[0].name).isEqualTo(USA_EN)
        assertThat(country1.names[1].name).isEqualTo(USA_FR)
        assertThat(country2.names[0].name).isEqualTo(GERMANY_EN)
        assertThat(country2.names[1].name).isEqualTo(GERMANY_FR)
    }

    companion object {
        private const val USA_COUNTRY_TAG = "usa"
        private const val GERMANY_COUNTRY_TAG = "germany"
    }
}