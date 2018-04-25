package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.*;
import static openfoodfacts.github.scrachx.openfood.models.CategoryResponseTestData.GUMMY_BEARS_EN;
import static openfoodfacts.github.scrachx.openfood.models.CategoryResponseTestData.GUMMY_BEARS_FR;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;

/**
 * Tests for {@link CategoryResponse}
 */
public class CategoryResponseTest {

    private final String CATEGORY_TAG = "tag";

    private final String WIKI_DATA_ID = "wiki_id";

    private Map<String, String> mNamesMap = new HashMap<>();

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

        assertEquals(CATEGORY_TAG, category.getTag());
        assertEquals(2, category.getNames().size());

        CategoryName categoryName1 = category.getNames().get(0);
        assertEquals(LANGUAGE_CODE_ENGLISH, categoryName1.getLanguageCode());
        assertEquals(GUMMY_BEARS_EN, categoryName1.getName());
        assertEquals(CATEGORY_TAG, categoryName1.getCategoryTag());
        assertFalse(categoryName1.getIsWikiDataIdPresent());
        assertEquals("null", categoryName1.getWikiDataId());

        CategoryName categoryName2 = category.getNames().get(1);
        assertEquals(LANGUAGE_CODE_FRENCH, categoryName2.getLanguageCode());
        assertEquals(GUMMY_BEARS_FR, categoryName2.getName());
        assertEquals(CATEGORY_TAG, categoryName2.getCategoryTag());
        assertFalse(categoryName2.getIsWikiDataIdPresent());
        assertEquals("null", categoryName2.getWikiDataId());
    }

    @Test
    public void mapWithWikiDataId_returnsCategoryWithNamesWithWikiDataId() {
        mCategoryResponse = new CategoryResponse(CATEGORY_TAG, mNamesMap, WIKI_DATA_ID);
        Category category = mCategoryResponse.map();

        assertEquals(CATEGORY_TAG, category.getTag());
        assertEquals(2, category.getNames().size());

        CategoryName categoryName1 = category.getNames().get(0);
        assertEquals(LANGUAGE_CODE_ENGLISH, categoryName1.getLanguageCode());
        assertEquals(GUMMY_BEARS_EN, categoryName1.getName());
        assertEquals(CATEGORY_TAG, categoryName1.getCategoryTag());
        assertTrue(categoryName1.getIsWikiDataIdPresent());
        assertEquals(WIKI_DATA_ID, categoryName1.getWikiDataId());

        CategoryName categoryName2 = category.getNames().get(1);
        assertEquals(LANGUAGE_CODE_FRENCH, categoryName2.getLanguageCode());
        assertEquals(GUMMY_BEARS_FR, categoryName2.getName());
        assertEquals(CATEGORY_TAG, categoryName2.getCategoryTag());
        assertTrue(categoryName2.getIsWikiDataIdPresent());
        assertEquals(WIKI_DATA_ID, categoryName2.getWikiDataId());
    }
}
