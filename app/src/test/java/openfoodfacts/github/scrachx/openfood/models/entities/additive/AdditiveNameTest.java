package openfoodfacts.github.scrachx.openfood.models.entities.additive;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

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
        assertThat(mAdditiveName.getWikiDataId()).isEqualTo("null");
    }

    @Test
    public void getWikiDataIdWithoutEnInWikiDataId_returnsWholeWikiDataId() {
        String fakeWikiDataId = "aFakeWikiDataId";
        mAdditiveName.setWikiDataId(fakeWikiDataId);
        assertThat(mAdditiveName.getWikiDataId()).isEqualTo(fakeWikiDataId);
    }

    @Test
    public void getWikiDataIdWithoutQuote_returnsWholeWikiDataId() {
        String fakeWikiDataId = "ThisOneIncludesenButNotAQuote";
        mAdditiveName.setWikiDataId(fakeWikiDataId);
        assertThat(mAdditiveName.getWikiDataId()).isEqualTo(fakeWikiDataId);
    }

    @Test
    public void getWikiDataIdWithEnAndQuote_returnsPartOfIdInBetweenFivePositionsPastEnAndQuote() {
        String wikiDataId = "somethingenmoreofit\"otherstuff";
        mAdditiveName.setWikiDataId(wikiDataId);
        assertThat(mAdditiveName.getWikiDataId()).isEqualTo("eofit");
    }

    @Test
    public void constructorWithWikiDataId_setsIsWikiDataIdPresentTrue() {
        mAdditiveName = new AdditiveName("AdditiveTag", "En", "Name",
            null, "WikiDatId");
        assertThat(mAdditiveName.getIsWikiDataIdPresent()).isTrue();
    }

    @Test
    public void constructorsWithoutWikiDataId_setIsWikiDataIdPresentFalse() {
        // TODO: update empty constructor to set isWikiDataIdPresent to false
        mAdditiveName = new AdditiveName("AdditiveTag", "Language",
            "Name", "no");
        assertThat(mAdditiveName.getIsWikiDataIdPresent()).isFalse();
        mAdditiveName = new AdditiveName("Name");
        assertThat(mAdditiveName.getIsWikiDataIdPresent()).isFalse();
    }
}
