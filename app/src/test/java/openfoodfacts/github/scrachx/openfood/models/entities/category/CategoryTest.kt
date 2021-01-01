package openfoodfacts.github.scrachx.openfood.models.entities.category

import com.google.common.truth.Truth
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import org.greenrobot.greendao.DaoException
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.Mockito.`when` as mockitoWhen

/**
 * Tests for [Category]
 */
@RunWith(MockitoJUnitRunner::class)
class CategoryTest {
    @Mock
    private val mockDaoSession: DaoSession? = null

    @Mock
    private val mockCategoryDao: CategoryDao? = null

    @Mock
    private val mockCategoryNameDao: CategoryNameDao? = null
    private lateinit var mCategory: Category

    @Before
    fun setup() {
        mockitoWhen(mockDaoSession!!.categoryDao).thenReturn(mockCategoryDao)
        mockitoWhen(mockDaoSession.categoryNameDao).thenReturn(mockCategoryNameDao)
        mockitoWhen(mockCategoryNameDao!!._queryCategory_Names(ArgumentMatchers.any()))
                .thenReturn(listOf(CATEGORY_NAME_1, CATEGORY_NAME_2))
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
        Truth.assertThat(names).hasSize(2)
        Truth.assertThat(names[0]!!.categoryTag).isEqualTo(CATEGORY_TAG_1)
        Truth.assertThat(names[0]!!.languageCode).isEqualTo(LANGUAGE_CODE_ENGLISH)
        Truth.assertThat(names[0]!!.name).isEqualTo(CATEGORY_NAME_NAME_1)
        Truth.assertThat(names[1]!!.categoryTag).isEqualTo(CATEGORY_TAG_2)
        Truth.assertThat(names[1]!!.languageCode).isEqualTo(LANGUAGE_CODE_FRENCH)
        Truth.assertThat(names[1]!!.name).isEqualTo(CATEGORY_NAME_NAME_2)
    }

    @Test
    fun deleteWithNullDao_throwsDaoException() {
        assertThrows(DaoException::class.java) { mCategory.delete() }
    }

    @Test
    fun deleteWithNonNullDao_callsDeleteOnDao() {
        mCategory.__setDaoSession(mockDaoSession)
        mCategory.delete()
        Mockito.verify(mockCategoryDao)!!.delete(mCategory)
    }

    @Test
    fun refreshWithNullDao_throwsDaoException() {
        assertThrows(DaoException::class.java) { mCategory.refresh() }
    }

    @Test
    fun refreshWithNonNullDao_callsRefreshOnDao() {
        mCategory.__setDaoSession(mockDaoSession)
        mCategory.refresh()
        Mockito.verify(mockCategoryDao)!!.refresh(mCategory)
    }

    @Test
    fun updateWithNullDao_throwsDaoException() {
        assertThrows(DaoException::class.java) { mCategory.update() }
    }

    @Test
    fun updateWithNonNullDao_callsUpdateOnDao() {
        mCategory.__setDaoSession(mockDaoSession)
        mCategory.update()
        Mockito.verify(mockCategoryDao)!!.update(mCategory)
    }

    @Test
    fun resetNames_callsGetLabelNameDao() {
        mCategory.__setDaoSession(mockDaoSession)
        mCategory.resetNames()
        mCategory.names
        Mockito.verify(mockDaoSession)!!.categoryNameDao
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