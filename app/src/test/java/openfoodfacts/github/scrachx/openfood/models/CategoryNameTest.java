package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

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
        assertEquals("null", mCategoryName.getWikiDataId());
    }

    @Test
    public void getWikiDataIdWithoutEnInWikiDataId_returnsWholeWikiDataId() {
        String fakeWikiDataId = "aFakeWikiDataId";
        mCategoryName.setWikiDataId(fakeWikiDataId);
        assertEquals(fakeWikiDataId, mCategoryName.getWikiDataId());
    }

    @Test
    public void getWikiDataIdWithoutQuote_returnsWholeWikiDataId() {
        String fakeWikiDataId = "ThisOneIncludesenButNotAQuote";
        mCategoryName.setWikiDataId(fakeWikiDataId);
        assertEquals(fakeWikiDataId, mCategoryName.getWikiDataId());
    }

    @Test
    public void getWikiDataIdWithEnAndQuote_returnsPartOfIdInBetweenFivePositionsPastEnAndQuote() {
        String wikiDataId = "somethingenmoreofit\"otherstuff";
        mCategoryName.setWikiDataId(wikiDataId);
        assertEquals("eofit", mCategoryName.getWikiDataId());
    }

    @Test
    public void constructorWithWikiDataId_setsIsWikiDataIdPresentTrue() {
        mCategoryName = new CategoryName("Tag", "en",
                "Name", "WikiDataId");
        assertTrue(mCategoryName.getIsWikiDataIdPresent());
    }

    @Test
    public void constructorsWithoutWikiDataId_setIsWikiDataIdPresentFalse() {
        // TODO: update empty constructor to set isWikiDataIdPresent to false if possible
        mCategoryName = new CategoryName("Tag", "en", "Name");
        assertFalse(mCategoryName.getIsWikiDataIdPresent());
    }
}
