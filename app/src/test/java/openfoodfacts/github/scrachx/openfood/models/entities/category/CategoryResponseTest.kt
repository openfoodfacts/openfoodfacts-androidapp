package openfoodfacts.github.scrachx.openfood.models.entities.category

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH
import openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryResponseTestData.GUMMY_BEARS_EN
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryResponseTestData.GUMMY_BEARS_FR
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * Tests for [CategoryResponse]
 */
class CategoryResponseTest {
    private val mNamesMap: MutableMap<String, String> = HashMap()
    private var mCategoryResponse: CategoryResponse? = null

    @Before
    fun setup() {
        mNamesMap[LANGUAGE_CODE_ENGLISH] = GUMMY_BEARS_EN
        mNamesMap[LANGUAGE_CODE_FRENCH] = GUMMY_BEARS_FR
    }

    @Test
    fun mapWithoutWikiDataId_returnsCategoryWithNamesWithoutWikiDataId() {
        mCategoryResponse = CategoryResponse(CATEGORY_TAG, mNamesMap)
        val category = mCategoryResponse!!.map()
        assertThat(category.tag).isEqualTo(CATEGORY_TAG)
        assertThat(category.names).hasSize(2)
        val categoryName1 = category.names[0]
        assertThat(categoryName1.languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(categoryName1.name).isEqualTo(GUMMY_BEARS_EN)
        assertThat(categoryName1.categoryTag).isEqualTo(CATEGORY_TAG)
        assertThat(categoryName1.isWikiDataIdPresent).isFalse()
        assertThat(categoryName1.wikiDataId).isNull()
        val categoryName2 = category.names[1]
        assertThat(categoryName2.languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
        assertThat(categoryName2.name).isEqualTo(GUMMY_BEARS_FR)
        assertThat(categoryName2.categoryTag).isEqualTo(CATEGORY_TAG)
        assertThat(categoryName2.isWikiDataIdPresent).isFalse()
        assertThat(categoryName2.wikiDataId).isNull()
    }

    @Test
    fun mapWithWikiDataId_returnsCategoryWithNamesWithWikiDataId() {
        mCategoryResponse = CategoryResponse(CATEGORY_TAG, mNamesMap, WIKI_DATA_ID)
        val category = mCategoryResponse!!.map()
        assertThat(category.tag).isEqualTo(CATEGORY_TAG)
        assertThat(category.names).hasSize(2)
        val categoryName1 = category.names[0]
        assertThat(categoryName1.languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(categoryName1.name).isEqualTo(GUMMY_BEARS_EN)
        assertThat(categoryName1.categoryTag).isEqualTo(CATEGORY_TAG)
        assertThat(categoryName1.isWikiDataIdPresent).isTrue()
        assertThat(categoryName1.wikiDataId).isEqualTo(WIKI_DATA_ID)
        val categoryName2 = category.names[1]
        assertThat(categoryName2.languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
        assertThat(categoryName2.name).isEqualTo(GUMMY_BEARS_FR)
        assertThat(categoryName2.categoryTag).isEqualTo(CATEGORY_TAG)
        assertThat(categoryName2.isWikiDataIdPresent).isTrue()
        assertThat(categoryName2.wikiDataId).isEqualTo(WIKI_DATA_ID)
    }

    companion object {
        private const val CATEGORY_TAG = "tag"
        private const val WIKI_DATA_ID = "wiki_id"
    }
}