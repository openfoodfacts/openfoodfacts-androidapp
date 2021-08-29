package openfoodfacts.github.scrachx.openfood.models.entities.category

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Tests for [CategoryName]
 */
class CategoryNameTest {
    private lateinit var mCategoryName: CategoryName

    @Before
    fun setup() {
        mCategoryName = CategoryName()
    }

    @Test
    fun getWikiDataIdWithNullWikiDataId_returnsStringNull() {
        assertThat(mCategoryName.wikiDataId).isNull()
    }

    @Test
    fun getWikiDataIdWithoutEnInWikiDataId_returnsWholeWikiDataId() {
        val fakeWikiDataId = "aFakeWikiDataId"
        mCategoryName.wikiDataId = fakeWikiDataId
        assertThat(mCategoryName.wikiDataId).isEqualTo(fakeWikiDataId)
    }

    @Test
    fun getWikiDataIdWithoutQuote_returnsWholeWikiDataId() {
        val fakeWikiDataId = "ThisOneIncludesenButNotAQuote"
        mCategoryName.wikiDataId = fakeWikiDataId
        assertThat(mCategoryName.wikiDataId).isEqualTo(fakeWikiDataId)
    }

    @Test
    fun getWikiDataIdWithEnAndQuote_returnsPartOfIdInBetweenFivePositionsPastEnAndQuote() {
        val wikiDataId = "somethingenmoreofit\"otherstuff"
        mCategoryName.wikiDataId = wikiDataId
        assertThat(mCategoryName.wikiDataId).isEqualTo("eofit")
    }

    @Test
    fun constructorWithWikiDataId_setsIsWikiDataIdPresentTrue() {
        mCategoryName = CategoryName("Tag", "en", "Name", "WikiDataId")
        assertThat(mCategoryName.isWikiDataIdPresent).isTrue()
    }

    @Test
    fun constructorsWithoutWikiDataId_setIsWikiDataIdPresentFalse() {
        // TODO: update empty constructor to set isWikiDataIdPresent to false if possible
        mCategoryName = CategoryName("Tag", "en", "Name")
        assertThat(mCategoryName.isWikiDataIdPresent).isFalse()
    }
}