package openfoodfacts.github.scrachx.openfood.models.entities.additive

import com.google.common.truth.Truth
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH
import org.junit.Test
import java.util.*

/**
 * Tests for [AdditivesWrapper]
 */
class AdditivesWrapperTest {
    @Test
    fun map_returnsListOfCorrectlyMappedAdditives() {
        val stringMap: MutableMap<String, String> = HashMap()
        stringMap[LANGUAGE_CODE_ENGLISH] = AdditiveResponseTestData.VINEGAR_EN
        stringMap[LANGUAGE_CODE_FRENCH] = AdditiveResponseTestData.VINEGAR_FR
        val additiveResponse1 = AdditiveResponse(AdditiveResponseTestData.ADDITIVE_TAG, stringMap, null)
        val additiveResponse2 = AdditiveResponse(AdditiveResponseTestData.ADDITIVE_TAG, stringMap, null, AdditiveResponseTestData.WIKI_DATA_ID)
        val additivesWrapper = AdditivesWrapper(listOf(additiveResponse1, additiveResponse2))
        val mappedAdditives: List<Additive?> = additivesWrapper.map()
        Truth.assertThat(mappedAdditives).hasSize(2)
        val mappedAdditive1 = mappedAdditives[0]
        val mappedAdditive2 = mappedAdditives[1]
        Truth.assertThat(mappedAdditive1!!.tag).isEqualTo(AdditiveResponseTestData.ADDITIVE_TAG)
        Truth.assertThat(mappedAdditive2!!.tag).isEqualTo(AdditiveResponseTestData.ADDITIVE_TAG)

        // TODO: fix so that this test passes. Currently gives a null pointer.
        // The problem is based on using Boolean class rather than boolean primitive
        // Need to set the Boolean to false somewhere
        Truth.assertThat(mappedAdditives[0]!!.isWikiDataIdPresent).isFalse()
        Truth.assertThat(mappedAdditive1.wikiDataId).isNull()
        Truth.assertThat(mappedAdditive2.isWikiDataIdPresent).isTrue()
        Truth.assertThat(mappedAdditive2.wikiDataId).isEqualTo(AdditiveResponseTestData.WIKI_DATA_ID)
        Truth.assertThat(mappedAdditive1.names).hasSize(2)
        Truth.assertThat(mappedAdditive2.names).hasSize(2)
        val mA1Name1 = mappedAdditive1.names[0]
        val mA1Name2 = mappedAdditive1.names[1]
        Truth.assertThat(mA1Name1.additiveTag).isEqualTo(AdditiveResponseTestData.ADDITIVE_TAG)
        Truth.assertThat(mA1Name2.additiveTag).isEqualTo(AdditiveResponseTestData.ADDITIVE_TAG)
        Truth.assertThat(mA1Name1.languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        Truth.assertThat(mA1Name2.languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
        Truth.assertThat(mA1Name1.name).isEqualTo(AdditiveResponseTestData.VINEGAR_EN)
        Truth.assertThat(mA1Name2.name).isEqualTo(AdditiveResponseTestData.VINEGAR_FR)
        Truth.assertThat(mA1Name1.isWikiDataIdPresent).isFalse()
        Truth.assertThat(mA1Name2.isWikiDataIdPresent).isFalse()
        Truth.assertThat(mA1Name1.wikiDataId).isEqualTo("null")
        Truth.assertThat(mA1Name2.wikiDataId).isEqualTo("null")
        val mA2Name1 = mappedAdditive2.names[0]
        val mA2Name2 = mappedAdditive2.names[1]
        Truth.assertThat(mA2Name1.additiveTag).isEqualTo(AdditiveResponseTestData.ADDITIVE_TAG)
        Truth.assertThat(mA2Name2.additiveTag).isEqualTo(AdditiveResponseTestData.ADDITIVE_TAG)
        Truth.assertThat(mA2Name1.languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        Truth.assertThat(mA2Name2.languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
        Truth.assertThat(mA2Name1.name).isEqualTo(AdditiveResponseTestData.VINEGAR_EN)
        Truth.assertThat(mA2Name2.name).isEqualTo(AdditiveResponseTestData.VINEGAR_FR)
        Truth.assertThat(mA2Name1.isWikiDataIdPresent).isTrue()
        Truth.assertThat(mA2Name2.isWikiDataIdPresent).isTrue()
        Truth.assertThat(mA2Name1.wikiDataId).isEqualTo(AdditiveResponseTestData.WIKI_DATA_ID)
        Truth.assertThat(mA2Name2.wikiDataId).isEqualTo(AdditiveResponseTestData.WIKI_DATA_ID)
    }
}