package openfoodfacts.github.scrachx.openfood.models.entities.country

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.GERMANY_EN
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.GERMANY_FR
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.USA_EN
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.USA_FR
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

/**
 * Tests for [CountriesWrapper]
 */
class CountriesWrapperTest {

    @Test
    fun map_returnsListOfCountries_ListHasCorrectSize() {
        assertThat(countries).hasSize(2)
    }

    @Test
    fun map_returnsListOfCountries_CountryTagsAreCorrect() {
        assertThat(usaCountry.tag).isEqualTo(USA_COUNTRY_TAG)
        assertThat(usaCountry.names).hasSize(2)

        assertThat(gerCountry.tag).isEqualTo(GERMANY_COUNTRY_TAG)
        assertThat(gerCountry.names).hasSize(2)
    }

    @Test
    fun map_returnsListOfCountries_SubCountryTagsAreCorrect() {
        assertThat(usaCountry.names[0].countyTag).isEqualTo(USA_COUNTRY_TAG)
        assertThat(usaCountry.names[1].countyTag).isEqualTo(USA_COUNTRY_TAG)

        assertThat(gerCountry.names[0].countyTag).isEqualTo(GERMANY_COUNTRY_TAG)
        assertThat(gerCountry.names[1].countyTag).isEqualTo(GERMANY_COUNTRY_TAG)
    }

    @Test
    fun map_returnsListOfCountries_SubLanguageCodesAreCorrect() {
        assertThat(usaCountry.names[0].languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(usaCountry.names[1].languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)

        assertThat(gerCountry.names[0].languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(gerCountry.names[1].languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
    }

    @Test
    fun map_returnsListOfCountries_SubNamesAreCorrect() {
        assertThat(usaCountry.names[0].name).isEqualTo(USA_EN)
        assertThat(usaCountry.names[1].name).isEqualTo(USA_FR)

        assertThat(gerCountry.names[0].name).isEqualTo(GERMANY_EN)
        assertThat(gerCountry.names[1].name).isEqualTo(GERMANY_FR)
    }

    companion object {
        private const val USA_COUNTRY_TAG = "usa"
        private const val GERMANY_COUNTRY_TAG = "germany"
        private lateinit var mCountriesWrapper: CountriesWrapper
        private lateinit var countries: List<Country>
        private lateinit var usaCountry: Country
        private lateinit var gerCountry: Country

        @JvmStatic
        @BeforeAll
        fun setup() {
            val usaNamesMap = mapOf(LANGUAGE_CODE_ENGLISH to USA_EN, LANGUAGE_CODE_FRENCH to USA_FR)
            val usaCC2Map = mapOf(LANGUAGE_CODE_ENGLISH to "US")
            val usaCC3Map = mapOf(LANGUAGE_CODE_ENGLISH to "USA")
            val usaResponse = CountryResponse(USA_COUNTRY_TAG, usaNamesMap, usaCC2Map, usaCC3Map)

            val gerNamesMap = mapOf(LANGUAGE_CODE_ENGLISH to GERMANY_EN, LANGUAGE_CODE_FRENCH to GERMANY_FR)
            val gerCC2Map = mapOf(LANGUAGE_CODE_ENGLISH to "DE")
            val gerCC3Map = mapOf(LANGUAGE_CODE_ENGLISH to "DEU")
            val gerResponse = CountryResponse(GERMANY_COUNTRY_TAG, gerNamesMap, gerCC2Map, gerCC3Map)

            mCountriesWrapper = CountriesWrapper(listOf(usaResponse, gerResponse))
            countries = mCountriesWrapper.map().also {
                usaCountry = it[0]
                gerCountry = it[1]
            }
        }
    }
}