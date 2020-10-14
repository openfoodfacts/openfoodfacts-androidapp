package openfoodfacts.github.scrachx.openfood.models.entities.category;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * Tests for {@link CategoryName}
 */
public class CategoryNameTest {
    private CategoryName mCategoryName;

    @Before
    public void setup() {
        mCategoryName = new CategoryName();
    }

    @Test
    public void getWikiDataIdWithNullWikiDataId_returnsStringNull() {
        assertThat(mCategoryName.getWikiDataId()).isEqualTo("null");
    }

    @Test
    public void getWikiDataIdWithoutEnInWikiDataId_returnsWholeWikiDataId() {
        String fakeWikiDataId = "aFakeWikiDataId";
        mCategoryName.setWikiDataId(fakeWikiDataId);
        assertThat(mCategoryName.getWikiDataId()).isEqualTo(fakeWikiDataId);
    }

    @Test
    public void getWikiDataIdWithoutQuote_returnsWholeWikiDataId() {
        String fakeWikiDataId = "ThisOneIncludesenButNotAQuote";
        mCategoryName.setWikiDataId(fakeWikiDataId);
        assertThat(mCategoryName.getWikiDataId()).isEqualTo(fakeWikiDataId);
    }

    @Test
    public void getWikiDataIdWithEnAndQuote_returnsPartOfIdInBetweenFivePositionsPastEnAndQuote() {
        String wikiDataId = "somethingenmoreofit\"otherstuff";
        mCategoryName.setWikiDataId(wikiDataId);
        assertThat(mCategoryName.getWikiDataId()).isEqualTo("eofit");
    }

    @Test
    public void constructorWithWikiDataId_setsIsWikiDataIdPresentTrue() {
        mCategoryName = new CategoryName("Tag", "en",
            "Name", "WikiDataId");
        assertThat(mCategoryName.getIsWikiDataIdPresent()).isTrue();
    }

    @Test
    public void constructorsWithoutWikiDataId_setIsWikiDataIdPresentFalse() {
        // TODO: update empty constructor to set isWikiDataIdPresent to false if possible
        mCategoryName = new CategoryName("Tag", "en", "Name");
        assertThat(mCategoryName.getIsWikiDataIdPresent()).isFalse();
    }
}
