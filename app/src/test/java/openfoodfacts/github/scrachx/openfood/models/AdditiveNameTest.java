package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Tests for {@link AdditiveName}
 */
public class AdditiveNameTest {

    private AdditiveName mAdditiveName;

    @Before
    public void setup() {
        mAdditiveName = new AdditiveName();
    }

    @Test
    public void getWikiDataIdWithNullWikiDataId_returnsStringNull() {
        assertEquals("null", mAdditiveName.getWikiDataId());
    }

    @Test
    public void getWikiDataIdWithoutEnInWikiDataId_returnsWholeWikiDataId() {
        String fakeWikiDataId = "aFakeWikiDataId";
        mAdditiveName.setWikiDataId(fakeWikiDataId);
        assertEquals(fakeWikiDataId, mAdditiveName.getWikiDataId());
    }

    @Test
    public void getWikiDataIdWithoutQuote_returnsWholeWikiDataId() {
        String fakeWikiDataId = "ThisOneIncludesenButNotAQuote";
        mAdditiveName.setWikiDataId(fakeWikiDataId);
        assertEquals(fakeWikiDataId, mAdditiveName.getWikiDataId());
    }

    @Test
    public void getWikiDataIdWithEnAndQuote_returnsPartOfIdInBetweenFivePositionsPastEnAndQuote() {
        String wikiDataId = "somethingenmoreofit\"otherstuff";
        mAdditiveName.setWikiDataId(wikiDataId);
        assertEquals("eofit", mAdditiveName.getWikiDataId());
    }

    @Test
    public void constructorWithWikiDataId_setsIsWikiDataIdPresentTrue() {
        mAdditiveName = new AdditiveName("AdditiveTag", "En", "Name",
                null,"WikiDatId");
        assertTrue(mAdditiveName.getIsWikiDataIdPresent());
    }

    @Test
    public void constructorsWithoutWikiDataId_setIsWikiDataIdPresentFalse() {
        // TODO: update empty constructor to set isWikiDataIdPresent to false
        mAdditiveName = new AdditiveName("AdditiveTag", "Language",
                "Name", "no");
        assertFalse(mAdditiveName.getIsWikiDataIdPresent());
        mAdditiveName = new AdditiveName("Name");
        assertFalse(mAdditiveName.getIsWikiDataIdPresent());
    }
}
