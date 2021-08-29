package openfoodfacts.github.scrachx.openfood.models.entities.label

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Tests for [LabelName]
 */
class LabelNameTest {
    private lateinit var mLabelName: LabelName

    @Before
    fun setup() {
        mLabelName = LabelName()
    }

    @Test
    fun getWikiDataIdWithNullWikiDataId_returnsNullAsString() {
        assertThat(mLabelName.wikiDataId).isNull()
    }

    @Test
    fun getWikiDataIdWithoutEnInWikiDataId_returnsWholeWikiDataId() {
        val fakeWikiDataId = "aFakeWikiDataId"
        mLabelName.wikiDataId = fakeWikiDataId
        assertThat(mLabelName.wikiDataId).isEqualTo(fakeWikiDataId)
    }

    @Test
    fun getWikiDataIdWithoutQuote_returnsWholeWikiDataId() {
        val wikiDataId = "somethingenmoreofit\"otherstuff"
        mLabelName.wikiDataId = wikiDataId
        assertThat(mLabelName.wikiDataId).isEqualTo("eofit")
    }

    @Test
    fun constructorWithWikiDataId_setsIsWikiDataIdPresentTrue() {
        mLabelName = LabelName("Tag", "en", "Food", "wikiid")
        assertThat(mLabelName.isWikiDataIdPresent).isTrue()
    }

    @Test
    fun constructorWithoutWikiDataId_setsIsWikiDataIdPresentFalse() {
        mLabelName = LabelName("Tag", "en", "name")
        assertThat(mLabelName.isWikiDataIdPresent).isFalse()
        mLabelName = LabelName("name")
        assertThat(mLabelName.isWikiDataIdPresent).isFalse()
    }
}