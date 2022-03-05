package openfoodfacts.github.scrachx.openfood.models.entities.label

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_NAME_EN
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_NAME_FR
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_TAG
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests for [LabelResponse]
 */
class LabelResponseTest {
    private lateinit var mNamesMap: MutableMap<String, String>
    private var mLabelResponse: LabelResponse? = null

    @BeforeEach
    fun setup() {
        mNamesMap = HashMap(2)
        mNamesMap[LANGUAGE_CODE_ENGLISH] = LABEL_NAME_EN
        mNamesMap[LANGUAGE_CODE_FRENCH] = LABEL_NAME_FR
    }

    @Test
    fun mapWithoutWikiDataId_returnsLabelWithNamesWithoutWikiDataId() {
        mLabelResponse = LabelResponse(LABEL_TAG, mNamesMap)
        val label = mLabelResponse!!.map()
        assertThat(label.tag).isEqualTo(LABEL_TAG)
        assertThat(label.isWikiDataIdPresent).isFalse()
        assertThat(label.wikiDataId).isNull()
        assertThat(label.names).hasSize(2)
        val labelName1 = label.names[0]
        assertThat(labelName1.labelTag).isEqualTo(LABEL_TAG)
        assertThat(labelName1.languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
        assertThat(labelName1.name).isEqualTo(LABEL_NAME_FR)
        assertThat(labelName1.isWikiDataIdPresent).isFalse()
        assertThat(labelName1.wikiDataId).isNull()
        val labelName2 = label.names[1]
        assertThat(labelName2.labelTag).isEqualTo(LABEL_TAG)
        assertThat(labelName2.languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(labelName2.name).isEqualTo(LABEL_NAME_EN)
        assertThat(labelName2.isWikiDataIdPresent).isFalse()
        assertThat(labelName2.wikiDataId).isNull()
    }

    @Test
    fun mapWithWikiDataId_returnsLabelsWithNamesWithWikiDataId() {
        val wikiDataId = "wikiDataId"
        mLabelResponse = LabelResponse(LABEL_TAG, mNamesMap, wikiDataId)
        val label = mLabelResponse!!.map()
        assertThat(label.tag).isEqualTo(LABEL_TAG)
        assertThat(label.isWikiDataIdPresent).isTrue()
        assertThat(label.wikiDataId).isEqualTo(wikiDataId)
        assertThat(label.names).hasSize(2)
        val labelName1 = label.names[0]
        assertThat(labelName1.labelTag).isEqualTo(LABEL_TAG)
        assertThat(labelName1.languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
        assertThat(labelName1.name).isEqualTo(LABEL_NAME_FR)
        assertThat(labelName1.isWikiDataIdPresent).isTrue()
        assertThat(labelName1.wikiDataId).isEqualTo(wikiDataId)
        val labelName2 = label.names[1]
        assertThat(labelName2.labelTag).isEqualTo(LABEL_TAG)
        assertThat(labelName2.languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(labelName2.name).isEqualTo(LABEL_NAME_EN)
        assertThat(labelName2.isWikiDataIdPresent).isTrue()
        assertThat(labelName2.wikiDataId).isEqualTo(wikiDataId)
    }
}