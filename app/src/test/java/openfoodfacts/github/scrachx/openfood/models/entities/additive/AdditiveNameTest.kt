package openfoodfacts.github.scrachx.openfood.models.entities.additive

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Tests for [AdditiveName]
 */
class AdditiveNameTest {
    private lateinit var mAdditiveName: AdditiveName

    @BeforeEach
    fun setup() {
        mAdditiveName = AdditiveName()
    }

    @Test
    fun getWikiDataIdWithNullWikiDataId_returnsStringNull() {
        assertThat(mAdditiveName.wikiDataId).isEqualTo("null")
    }

    @Test
    fun getWikiDataIdWithoutEnInWikiDataId_returnsWholeWikiDataId() {
        val fakeWikiDataId = "aFakeWikiDataId"
        mAdditiveName.wikiDataId = fakeWikiDataId
        assertThat(mAdditiveName.wikiDataId).isEqualTo(fakeWikiDataId)
    }

    @Test
    fun getWikiDataIdWithoutQuote_returnsWholeWikiDataId() {
        val fakeWikiDataId = "ThisOneIncludesenButNotAQuote"
        mAdditiveName.wikiDataId = fakeWikiDataId
        assertThat(mAdditiveName.wikiDataId).isEqualTo(fakeWikiDataId)
    }

    @Test
    fun getWikiDataIdWithEnAndQuote_returnsPartOfIdInBetweenFivePositionsPastEnAndQuote() {
        val wikiDataId = "somethingenmoreofit\"otherstuff"
        mAdditiveName.wikiDataId = wikiDataId
        assertThat(mAdditiveName.wikiDataId).isEqualTo("eofit")
    }

    @Test
    fun constructorWithWikiDataId_setsIsWikiDataIdPresentTrue() {
        mAdditiveName = AdditiveName(
                "AdditiveTag",
                "En",
                "Name",
                null,
                "WikiDatId"
        )
        assertThat(mAdditiveName.isWikiDataIdPresent).isTrue()
    }

    @Test
    fun constructorsWithoutWikiDataId_setIsWikiDataIdPresentFalse() {
        // TODO: update empty constructor to set isWikiDataIdPresent to false
        mAdditiveName = AdditiveName(
                "AdditiveTag",
                "Language",
                "Name",
                "no"
        )
        assertThat(mAdditiveName.isWikiDataIdPresent).isFalse()

        mAdditiveName = AdditiveName("Name")
        assertThat(mAdditiveName.isWikiDataIdPresent).isFalse()
    }
}