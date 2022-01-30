package openfoodfacts.github.scrachx.openfood.models.entities.country

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.GERMANY_EN
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.GERMANY_FR
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests for [CountryResponse]
 */
class CountryResponseTest {
    private var mCountryResponse: CountryResponse? = null
    private var country: Country? = null

    @BeforeEach
    fun setup() {
        NAMES_MAP[LANGUAGE_CODE_ENGLISH] = GERMANY_EN
        NAMES_MAP[LANGUAGE_CODE_FRENCH] = GERMANY_FR
        mCountryResponse = CountryResponse(COUNTRY_TAG, NAMES_MAP, HashMap(), HashMap())
        country = mCountryResponse!!.map()
    }

    @Test
    fun map_returnsCorrectTag() {
        assertThat(country!!.tag).isEqualTo(COUNTRY_TAG)
    }

    @Test
    fun map_returnsCorrectSize() {
        assertThat(country!!.names).hasSize(2)
    }

    @Test
    fun map_returnsFirstCountryWithMappedNames() {
        val countryName1 = country!!.names[0]
        assertThat(countryName1.countyTag).isEqualTo(COUNTRY_TAG)
        assertThat(countryName1.languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(countryName1.name).isEqualTo(GERMANY_EN)
    }

    @Test
    fun map_returnsSecondCountryWithMappedNames() {
        val countryName2 = country!!.names[1]
        assertThat(countryName2.countyTag).isEqualTo(COUNTRY_TAG)
        assertThat(countryName2.languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
        assertThat(countryName2.name).isEqualTo(GERMANY_FR)
    }

    companion object {
        private const val COUNTRY_TAG = "tag"
        private val NAMES_MAP = hashMapOf<String, String>()
    }
}