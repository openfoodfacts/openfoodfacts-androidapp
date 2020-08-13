package openfoodfacts.github.scrachx.openfood.models.entities.category;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;
import static openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryResponseTestData.GUMMY_BEARS_EN;
import static openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryResponseTestData.GUMMY_BEARS_FR;

/**
 * Tests for {@link CategoryResponse}
 */
public class CategoryResponseTest {
    private final String CATEGORY_TAG = "tag";
    private final String WIKI_DATA_ID = "wiki_id";
    private final Map<String, String> mNamesMap = new HashMap<>();
    private CategoryResponse mCategoryResponse;

    @Before
    public void setup() {
        mNamesMap.put(LANGUAGE_CODE_ENGLISH, GUMMY_BEARS_EN);
        mNamesMap.put(LANGUAGE_CODE_FRENCH, GUMMY_BEARS_FR);
    }

    @Test
    public void mapWithoutWikiDataId_returnsCategoryWithNamesWithoutWikiDataId() {
        mCategoryResponse = new CategoryResponse(CATEGORY_TAG, mNamesMap);
        Category category = mCategoryResponse.map();

        assertThat(category.getTag()).isEqualTo(CATEGORY_TAG);
        assertThat(category.getNames()).hasSize(2);

        CategoryName categoryName1 = category.getNames().get(0);
        assertThat(categoryName1.getLanguageCode()).isEqualTo(LANGUAGE_CODE_ENGLISH);
        assertThat(categoryName1.getName()).isEqualTo(GUMMY_BEARS_EN);
        assertThat(categoryName1.getCategoryTag()).isEqualTo(CATEGORY_TAG);
        assertThat(categoryName1.getIsWikiDataIdPresent()).isFalse();
        assertThat(categoryName1.getWikiDataId()).isEqualTo("null");

        CategoryName categoryName2 = category.getNames().get(1);
        assertThat(categoryName2.getLanguageCode()).isEqualTo(LANGUAGE_CODE_FRENCH);
        assertThat(categoryName2.getName()).isEqualTo(GUMMY_BEARS_FR);
        assertThat(categoryName2.getCategoryTag()).isEqualTo(CATEGORY_TAG);
        assertThat(categoryName2.getIsWikiDataIdPresent()).isFalse();
        assertThat(categoryName2.getWikiDataId()).isEqualTo("null");
    }

    @Test
    public void mapWithWikiDataId_returnsCategoryWithNamesWithWikiDataId() {
        mCategoryResponse = new CategoryResponse(CATEGORY_TAG, mNamesMap, WIKI_DATA_ID);
        Category category = mCategoryResponse.map();

        assertThat(category.getTag()).isEqualTo(CATEGORY_TAG);
        assertThat(category.getNames()).hasSize(2);

        CategoryName categoryName1 = category.getNames().get(0);
        assertThat(categoryName1.getLanguageCode()).isEqualTo(LANGUAGE_CODE_ENGLISH);
        assertThat(categoryName1.getName()).isEqualTo(GUMMY_BEARS_EN);
        assertThat(categoryName1.getCategoryTag()).isEqualTo(CATEGORY_TAG);
        assertThat(categoryName1.getIsWikiDataIdPresent()).isTrue();
        assertThat(categoryName1.getWikiDataId()).isEqualTo(WIKI_DATA_ID);

        CategoryName categoryName2 = category.getNames().get(1);
        assertThat(categoryName2.getLanguageCode()).isEqualTo(LANGUAGE_CODE_FRENCH);
        assertThat(categoryName2.getName()).isEqualTo(GUMMY_BEARS_FR);
        assertThat(categoryName2.getCategoryTag()).isEqualTo(CATEGORY_TAG);
        assertThat(categoryName2.getIsWikiDataIdPresent()).isTrue();
        assertThat(categoryName2.getWikiDataId()).isEqualTo(WIKI_DATA_ID);
    }
}
