package openfoodfacts.github.scrachx.openfood.models.entities.label

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_GERMAN
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_NAME_DE
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_NAME_EN
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_NAME_FR
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameTestData.LABEL_TAG
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * Tests for [LabelsWrapper]
 */
class LabelsWrapperTest {
    var labels: List<Label?>? = null
    var labelTag2: String? = null
    var label1: Label? = null
    var label2: Label? = null

    @Before
    fun setUp() {
        val namesMap1: MutableMap<String, String> = HashMap(2)
        namesMap1[LANGUAGE_CODE_ENGLISH] = LABEL_NAME_EN
        namesMap1[LANGUAGE_CODE_FRENCH] = LABEL_NAME_FR
        val labelResponse1 = LabelResponse(LABEL_TAG, namesMap1)
        val namesMap2: MutableMap<String, String> = HashMap(2)
        namesMap2[LANGUAGE_CODE_GERMAN] = LABEL_NAME_DE
        namesMap2[LANGUAGE_CODE_ENGLISH] = LABEL_NAME_EN
        labelTag2 = "Tag2"
        val labelResponse2 = LabelResponse(labelTag2!!, namesMap2)
        val labelsWrapper = LabelsWrapper(listOf(labelResponse1, labelResponse2))
        labels = labelsWrapper.map()
    }

    @Test
    fun map_returnsListOfCorrectlyMappedLabels_correctSize() {
        assertThat(labels).hasSize(2)
    }

    @Test
    fun map_returnsListOfCorrectlyMappedLabels_correctStructure() {
        label1 = labels!![0]
        label2 = labels!![1]
        assertThat(label1!!.tag).isEqualTo(LABEL_TAG)
        assertThat(label1!!.wikiDataId).isNull()
        assertThat(label1!!.isWikiDataIdPresent).isFalse()
        assertThat(label1!!.names).hasSize(2)
        assertThat(label2!!.tag).isEqualTo(labelTag2)
        assertThat(label2!!.wikiDataId).isNull()
        assertThat(label2!!.isWikiDataIdPresent).isFalse()
        assertThat(label2!!.names).hasSize(2)
    }

    @Test
    fun map_returnsListOfCorrectlyMappedLabels_SubElementsAreMappedCorrectly() {
        label1 = labels!![0]
        label2 = labels!![1]
        val label1Fr = label1!!.names[0]
        assertThat(label1Fr.labelTag).isEqualTo(LABEL_TAG)
        assertThat(label1Fr.languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
        assertThat(label1Fr.name).isEqualTo(LABEL_NAME_FR)
        assertThat(label1Fr.isWikiDataIdPresent).isFalse()
        assertThat(label1Fr.wikiDataId).isEqualTo(null)
        val label1En = label1!!.names[1]
        assertThat(label1En.labelTag).isEqualTo(LABEL_TAG)
        assertThat(label1En.languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(label1En.name).isEqualTo(LABEL_NAME_EN)
        assertThat(label1En.isWikiDataIdPresent).isFalse()
        assertThat(label1En.wikiDataId).isEqualTo(null)
        val label2De = label2!!.names[0]
        assertThat(label2De.labelTag).isEqualTo(labelTag2)
        assertThat(label2De.languageCode).isEqualTo(LANGUAGE_CODE_GERMAN)
        assertThat(label2De.name).isEqualTo(LABEL_NAME_DE)
        assertThat(label2De.isWikiDataIdPresent).isFalse()
        assertThat(label2De.wikiDataId).isEqualTo(null)
        val label2En = label2!!.names[1]
        assertThat(label2En.labelTag).isEqualTo(labelTag2)
        assertThat(label2En.languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(label2En.name).isEqualTo(LABEL_NAME_EN)
        assertThat(label2En.isWikiDataIdPresent).isFalse()
        assertThat(label2En.wikiDataId).isEqualTo(null)
    }
}