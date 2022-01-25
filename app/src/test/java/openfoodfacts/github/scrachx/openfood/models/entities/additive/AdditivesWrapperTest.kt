package openfoodfacts.github.scrachx.openfood.models.entities.additive

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveResponseTestData.ADDITIVE_TAG
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveResponseTestData.WIKI_DATA_ID
import org.junit.jupiter.api.Test

/**
 * Tests for [AdditivesWrapper]
 */
class AdditivesWrapperTest {
    @Test
    fun map_returnsListOfCorrectlyMappedAdditives() {
        val stringMap = mapOf(
                LANGUAGE_CODE_ENGLISH to AdditiveResponseTestData.VINEGAR_EN,
                LANGUAGE_CODE_FRENCH to AdditiveResponseTestData.VINEGAR_FR
        )
        val additiveResponse1 = AdditiveResponse(ADDITIVE_TAG, stringMap, null)
        val additiveResponse2 = AdditiveResponse(ADDITIVE_TAG, stringMap, null, WIKI_DATA_ID)
        val additivesWrapper = AdditivesWrapper(listOf(additiveResponse1, additiveResponse2))
        val mappedAdditives = additivesWrapper.map()
        assertThat(mappedAdditives).hasSize(2)

        val mappedAdditive1 = mappedAdditives[0]
        val mappedAdditive2 = mappedAdditives[1]

        assertThat(mappedAdditive1.tag).isEqualTo(ADDITIVE_TAG)
        assertThat(mappedAdditive2.tag).isEqualTo(ADDITIVE_TAG)

        assertThat(mappedAdditive1.isWikiDataIdPresent).isFalse()
        assertThat(mappedAdditive1.wikiDataId).isNull()
        assertThat(mappedAdditive2.isWikiDataIdPresent).isTrue()
        assertThat(mappedAdditive2.wikiDataId).isEqualTo(WIKI_DATA_ID)

        assertThat(mappedAdditive1.names).hasSize(2)
        assertThat(mappedAdditive2.names).hasSize(2)

        val mA1Name1 = mappedAdditive1.names[0]
        val mA1Name2 = mappedAdditive1.names[1]

        assertThat(mA1Name1.additiveTag).isEqualTo(ADDITIVE_TAG)
        assertThat(mA1Name2.additiveTag).isEqualTo(ADDITIVE_TAG)

        assertThat(mA1Name1.languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(mA1Name2.languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)

        assertThat(mA1Name1.name).isEqualTo(AdditiveResponseTestData.VINEGAR_EN)
        assertThat(mA1Name2.name).isEqualTo(AdditiveResponseTestData.VINEGAR_FR)

        assertThat(mA1Name1.isWikiDataIdPresent).isFalse()
        assertThat(mA1Name2.isWikiDataIdPresent).isFalse()

        assertThat(mA1Name1.wikiDataId).isEqualTo("null")
        assertThat(mA1Name2.wikiDataId).isEqualTo("null")

        val mA2Name1 = mappedAdditive2.names[0]
        val mA2Name2 = mappedAdditive2.names[1]

        assertThat(mA2Name1.additiveTag).isEqualTo(ADDITIVE_TAG)
        assertThat(mA2Name2.additiveTag).isEqualTo(ADDITIVE_TAG)

        assertThat(mA2Name1.languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(mA2Name2.languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)

        assertThat(mA2Name1.name).isEqualTo(AdditiveResponseTestData.VINEGAR_EN)
        assertThat(mA2Name2.name).isEqualTo(AdditiveResponseTestData.VINEGAR_FR)

        assertThat(mA2Name1.isWikiDataIdPresent).isTrue()
        assertThat(mA2Name2.isWikiDataIdPresent).isTrue()

        assertThat(mA2Name1.wikiDataId).isEqualTo(WIKI_DATA_ID)
        assertThat(mA2Name2.wikiDataId).isEqualTo(WIKI_DATA_ID)
    }
}