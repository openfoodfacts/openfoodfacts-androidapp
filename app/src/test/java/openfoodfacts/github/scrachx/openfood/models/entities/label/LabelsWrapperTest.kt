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

/**
 * Tests for [LabelsWrapper]
 */
class LabelsWrapperTest {
    private var labels: List<Label?>? = null
    private var labelTag2: String? = null

    @Before
    fun setUp() {
        val namesMap1 = hashMapOf(
                LANGUAGE_CODE_ENGLISH to LABEL_NAME_EN,
                LANGUAGE_CODE_FRENCH to LABEL_NAME_FR
        )

        val labelResponse1 = LabelResponse(LABEL_TAG, namesMap1)
        val namesMap2 = hashMapOf(
                LANGUAGE_CODE_GERMAN to LABEL_NAME_DE,
                LANGUAGE_CODE_ENGLISH to LABEL_NAME_EN
        )

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
        val label1 = labels!![0]
        assertThat(label1!!.tag).isEqualTo(LABEL_TAG)
        assertThat(label1.wikiDataId).isNull()
        assertThat(label1.isWikiDataIdPresent).isFalse()
        assertThat(label1.names).hasSize(2)

        val label2 = labels!![1]
        assertThat(label2!!.tag).isEqualTo(labelTag2)
        assertThat(label2.wikiDataId).isNull()
        assertThat(label2.isWikiDataIdPresent).isFalse()
        assertThat(label2.names).hasSize(2)
    }

    @Test
    fun map_returnsListOfCorrectlyMappedLabels_SubElementsAreMappedCorrectly() {
        val label1 = labels!![0]
        val label1Fr = label1!!.names[0]
        assertThat(label1Fr.labelTag).isEqualTo(LABEL_TAG)
        assertThat(label1Fr.languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
        assertThat(label1Fr.name).isEqualTo(LABEL_NAME_FR)
        assertThat(label1Fr.isWikiDataIdPresent).isFalse()
        assertThat(label1Fr.wikiDataId).isNull()

        val label1En = label1.names[1]
        assertThat(label1En.labelTag).isEqualTo(LABEL_TAG)
        assertThat(label1En.languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(label1En.name).isEqualTo(LABEL_NAME_EN)
        assertThat(label1En.isWikiDataIdPresent).isFalse()
        assertThat(label1En.wikiDataId).isNull()

        val label2 = labels!![1]
        val label2De = label2!!.names[0]
        assertThat(label2De.labelTag).isEqualTo(labelTag2)
        assertThat(label2De.languageCode).isEqualTo(LANGUAGE_CODE_GERMAN)
        assertThat(label2De.name).isEqualTo(LABEL_NAME_DE)
        assertThat(label2De.isWikiDataIdPresent).isFalse()
        assertThat(label2De.wikiDataId).isNull()

        val label2En = label2.names[1]
        assertThat(label2En.labelTag).isEqualTo(labelTag2)
        assertThat(label2En.languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(label2En.name).isEqualTo(LABEL_NAME_EN)
        assertThat(label2En.isWikiDataIdPresent).isFalse()
        assertThat(label2En.wikiDataId).isNull()
    }
}