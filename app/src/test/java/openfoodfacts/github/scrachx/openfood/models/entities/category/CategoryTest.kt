package openfoodfacts.github.scrachx.openfood.models.entities.category

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import org.greenrobot.greendao.DaoException
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*

/**
 * Tests for [Category]
 */
@RunWith(MockitoJUnitRunner.StrictStubs::class)
class CategoryTest {
    @Mock
    private lateinit var mockDaoSession: DaoSession

    @Mock
    private lateinit var mockCategoryDao: CategoryDao

    @Mock
    private lateinit var mockCategoryNameDao: CategoryNameDao
    private lateinit var mCategory: Category

    @Before
    fun setup() {
        whenever(mockDaoSession.categoryDao) doReturn mockCategoryDao
        whenever(mockDaoSession.categoryNameDao) doReturn mockCategoryNameDao
        whenever(mockCategoryNameDao._queryCategory_Names(anyOrNull())) doReturn listOf(CATEGORY_NAME_1, CATEGORY_NAME_2)

        mCategory = Category()
    }

    @Test
    fun getNamesWithNullNamesAndNullDaoSession_throwsDaoException() {
        assertThrows(DaoException::class.java) { mCategory.names }
    }

    @Test
    fun getNamesWithNullNamesAndNonNullDaoSession_setsNamesFromCategoryNameDao() {
        mCategory.__setDaoSession(mockDaoSession)
        val names = mCategory.names

        assertThat(names).hasSize(2)

        val name1 = names[0]!!
        assertThat(name1.categoryTag).isEqualTo(CATEGORY_TAG_1)
        assertThat(name1.languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        assertThat(name1.name).isEqualTo(CATEGORY_NAME_NAME_1)

        val name2 = names[1]!!
        assertThat(name2.categoryTag).isEqualTo(CATEGORY_TAG_2)
        assertThat(name2.languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
        assertThat(name2.name).isEqualTo(CATEGORY_NAME_NAME_2)
    }

    @Test
    fun deleteWithNullDao_throwsDaoException() {
        assertThrows(DaoException::class.java) { mCategory.delete() }
    }

    @Test
    fun deleteWithNonNullDao_callsDeleteOnDao() {
        mCategory.__setDaoSession(mockDaoSession)
        mCategory.delete()
        verify(mockCategoryDao).delete(mCategory)
    }

    @Test
    fun refreshWithNullDao_throwsDaoException() {
        assertThrows(DaoException::class.java) { mCategory.refresh() }
    }

    @Test
    fun refreshWithNonNullDao_callsRefreshOnDao() {
        mCategory.__setDaoSession(mockDaoSession)
        mCategory.refresh()
        verify(mockCategoryDao).refresh(mCategory)
    }

    @Test
    fun updateWithNullDao_throwsDaoException() {
        assertThrows(DaoException::class.java) { mCategory.update() }
    }

    @Test
    fun updateWithNonNullDao_callsUpdateOnDao() {
        mCategory.__setDaoSession(mockDaoSession)
        mCategory.update()
        verify(mockCategoryDao).update(mCategory)
    }

    @Test
    fun resetNames_callsGetLabelNameDao() {
        mCategory.__setDaoSession(mockDaoSession)
        mCategory.resetNames()
        mCategory.names
        verify(mockDaoSession).categoryNameDao
    }

    companion object {
        private const val CATEGORY_TAG_1 = "Tag1"
        private const val CATEGORY_TAG_2 = "Tag2"
        private const val LANGUAGE_CODE_ENGLISH = "en"
        private const val LANGUAGE_CODE_FRENCH = "fr"
        private const val CATEGORY_NAME_NAME_1 = "Gummy Bears"
        private const val CATEGORY_NAME_NAME_2 = "Ours Gommeux"
        private val CATEGORY_NAME_1 = CategoryName(CATEGORY_TAG_1, LANGUAGE_CODE_ENGLISH, CATEGORY_NAME_NAME_1)
        private val CATEGORY_NAME_2 = CategoryName(CATEGORY_TAG_2, LANGUAGE_CODE_FRENCH, CATEGORY_NAME_NAME_2)
    }
}