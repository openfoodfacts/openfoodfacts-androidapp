package openfoodfacts.github.scrachx.openfood.models.entities.additive

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests for [AdditiveResponse]
 */
class AdditiveResponseTest {
    private lateinit var mStringMap: Map<String, String>

    @BeforeEach
    fun setup() {
        mStringMap = mapOf(
                LANGUAGE_CODE_ENGLISH to AdditiveResponseTestData.VINEGAR_EN,
                LANGUAGE_CODE_FRENCH to AdditiveResponseTestData.VINEGAR_FR
        )
    }

    @Test
    fun mapWithoutWikiDataId_returnsAdditiveWithNamesWithoutWikiDataId() {
        val mAdditiveResponse = AdditiveResponse(AdditiveResponseTestData.ADDITIVE_TAG, mStringMap, null)
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
        val mAdditiveResponse = AdditiveResponse(
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