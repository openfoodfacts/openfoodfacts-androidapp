package openfoodfacts.github.scrachx.openfood.models.entities.label;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

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
        assertThat(mLabelName.getWikiDataId()).isEqualTo("null");
    }

    @Test
    public void getWikiDataIdWithoutEnInWikiDataId_returnsWholeWikiDataId() {
        String fakeWikiDataId = "aFakeWikiDataId";
        mLabelName.setWikiDataId(fakeWikiDataId);
        assertThat(mLabelName.getWikiDataId()).isEqualTo(fakeWikiDataId);
    }

    @Test
    public void getWikiDataIdWithoutQuote_returnsWholeWikiDataId() {
        String wikiDataId = "somethingenmoreofit\"otherstuff";
        mLabelName.setWikiDataId(wikiDataId);
        assertThat(mLabelName.getWikiDataId()).isEqualTo("eofit");
    }

    @Test
    public void constructorWithWikiDataId_setsIsWikiDataIdPresentTrue() {
        mLabelName = new LabelName("Tag", "en", "Food", "wikiid");
        assertThat(mLabelName.getIsWikiDataIdPresent()).isTrue();
    }

    @Test
    public void constructorWithoutWikiDataId_setsIsWikiDataIdPresentFalse() {
        mLabelName = new LabelName("Tag", "en", "name");
        assertThat(mLabelName.getIsWikiDataIdPresent()).isFalse();
        mLabelName = new LabelName("name");
        assertThat(mLabelName.getIsWikiDataIdPresent()).isFalse();
    }
}
