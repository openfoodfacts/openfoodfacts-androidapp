package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Tests for {@link LabelName}
 */
public class LabelNameTest {

    private LabelName mLabelName;

    @Before
    public void setup() {
        mLabelName = new LabelName();
    }

    @Test
    public void getWikiDataIdWithNullWikiDataId_returnsNullAsString() {
        assertEquals("null", mLabelName.getWikiDataId());
    }

    @Test
    public void getWikiDataIdWithoutEnInWikiDataId_returnsWholeWikiDataId() {
        String fakeWikiDataId = "aFakeWikiDataId";
        mLabelName.setWikiDataId(fakeWikiDataId);
        assertEquals(fakeWikiDataId, mLabelName.getWikiDataId());
    }

    @Test
    public void getWikiDataIdWithoutQuote_returnsWholeWikiDataId() {
        String wikiDataId = "somethingenmoreofit\"otherstuff";
        mLabelName.setWikiDataId(wikiDataId);
        assertEquals("eofit", mLabelName.getWikiDataId());
    }

    @Test
    public void constructorWithWikiDataId_setsIsWikiDataIdPresentTrue() {
        mLabelName = new LabelName("Tag", "en", "Food", "wikiid");
        assertTrue(mLabelName.getIsWikiDataIdPresent());
    }

    @Test
    public void constructorWithoutWikiDataId_setsIsWikiDataIdPresentFalse() {
        mLabelName = new LabelName("Tag", "en", "name");
        assertFalse(mLabelName.getIsWikiDataIdPresent());
        mLabelName = new LabelName("name");
        assertFalse(mLabelName.getIsWikiDataIdPresent());
    }
}
