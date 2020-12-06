package openfoodfacts.github.scrachx.openfood.models.entities.additive

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * Tests for [AdditiveResponse]
 */
class AdditiveResponseTest {
    private val mStringMap = HashMap<String, String>()
    private lateinit var mAdditiveResponse: AdditiveResponse

    @Before
    fun setup() {
        mStringMap[LANGUAGE_CODE_ENGLISH] = AdditiveResponseTestData.VINEGAR_EN
        mStringMap[LANGUAGE_CODE_FRENCH] = AdditiveResponseTestData.VINEGAR_FR
    }

    @Test
    fun mapWithoutWikiDataId_returnsAdditiveWithNamesWithoutWikiDataId() {
        mAdditiveResponse = AdditiveResponse(AdditiveResponseTestData.ADDITIVE_TAG, mStringMap, null)
        val additive = mAdditiveResponse.map()
        assertThat(additive.tag).isEqualTo(AdditiveResponseTestData.ADDITIVE_TAG)
        assertThat(additive.names).hasSize(2)
        assertThat(additive.names[0].languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(additive.names[0].name).isEqualTo(AdditiveResponseTestData.VINEGAR_EN)
        assertThat(additive.names[0].additiveTag).isEqualTo(AdditiveResponseTestData.ADDITIVE_TAG)
        assertThat(additive.names[0].wikiDataId).isEqualTo("null")
        assertThat(additive.names[0].isWikiDataIdPresent).isFalse()
        assertThat(additive.names[1].languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
        assertThat(additive.names[1].name).isEqualTo(AdditiveResponseTestData.VINEGAR_FR)
        assertThat(additive.names[1].additiveTag).isEqualTo(AdditiveResponseTestData.ADDITIVE_TAG)
        assertThat(additive.names[1].wikiDataId).isEqualTo("null")
        assertThat(additive.names[1].isWikiDataIdPresent).isFalse()
    }

    @Test
    fun mapWithWikiDataId_returnsAdditiveWithNamesWithWikiDataId() {
        mAdditiveResponse = AdditiveResponse(
                AdditiveResponseTestData.ADDITIVE_TAG,
                mStringMap,
                null,
                AdditiveResponseTestData.WIKI_DATA_ID
        )
        val additive = mAdditiveResponse.map()
        assertThat(additive.tag).isEqualTo(AdditiveResponseTestData.ADDITIVE_TAG)
        assertThat(additive.names).hasSize(2)
        assertThat(additive.names[0].languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(additive.names[0].name).isEqualTo(AdditiveResponseTestData.VINEGAR_EN)
        assertThat(additive.names[0].additiveTag).isEqualTo(AdditiveResponseTestData.ADDITIVE_TAG)
        assertThat(additive.names[0].wikiDataId).isEqualTo(AdditiveResponseTestData.WIKI_DATA_ID)
        assertThat(additive.names[0].isWikiDataIdPresent).isTrue()
        assertThat(additive.names[1].languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
        assertThat(additive.names[1].name).isEqualTo(AdditiveResponseTestData.VINEGAR_FR)
        assertThat(additive.names[1].additiveTag).isEqualTo(AdditiveResponseTestData.ADDITIVE_TAG)
        assertThat(additive.names[1].wikiDataId).isEqualTo(AdditiveResponseTestData.WIKI_DATA_ID)
        assertThat(additive.names[1].isWikiDataIdPresent).isTrue()
    }
}