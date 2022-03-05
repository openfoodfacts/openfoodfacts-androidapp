package openfoodfacts.github.scrachx.openfood.models.entities.additive

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import org.greenrobot.greendao.DaoException
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness

/**
 * Tests for [Additive]
 */
@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdditiveTest {
    @Mock
    private val mockDaoSession: DaoSession? = null

    @Mock
    private lateinit var mockAdditiveDao: AdditiveDao

    @Mock
    private lateinit var mockAdditiveNameDao: AdditiveNameDao
    private lateinit var mAdditive: Additive

    @BeforeEach
    fun setup() {
        whenever(mockDaoSession!!.additiveNameDao) doReturn mockAdditiveNameDao
        whenever(mockDaoSession.additiveDao) doReturn mockAdditiveDao
        whenever(mockAdditiveNameDao._queryAdditive_Names(ArgumentMatchers.any())) doReturn listOf(ADDITIVE_NAME_1, ADDITIVE_NAME_2)
        mAdditive = Additive()
    }

    @Test
    fun getNamesWithNullNamesAndNullDaoSession_throwsDaoException() {
        assertThrows(DaoException::class.java) { mAdditive.names }
    }

    @Test
    fun getNamesWithNullNamesAndNonNullDaoSession_setsNamesFromAdditiveNamesDao() {
        mAdditive.__setDaoSession(mockDaoSession)
        val names = mAdditive.names
        assertThat(names).hasSize(2)
        assertThat(names[0]!!.name).isEqualTo(ADDITIVE_NAME_NAME_1)
        assertThat(names[1]!!.name).isEqualTo(ADDITIVE_NAME_NAME_2)
    }

    @Test
    fun deleteWithNullDao_throwsDaoException() {
        assertThrows(DaoException::class.java) { mAdditive.delete() }
    }

    @Test
    fun deleteWithNonNullDao_callsDeleteOnDao() {
        mAdditive.__setDaoSession(mockDaoSession)
        mAdditive.delete()
        verify(mockAdditiveDao).delete(mAdditive)
    }

    @Test
    fun refreshWithNullDao_throwsDaoException() {
        assertThrows(DaoException::class.java) { mAdditive.refresh() }
    }

    @Test
    fun refreshWithNonNullDao_callsRefreshOnDao() {
        mAdditive.__setDaoSession(mockDaoSession)
        mAdditive.refresh()
        verify(mockAdditiveDao).refresh(mAdditive)
    }

    @Test
    fun updateWithNullDao_throwsDaoException() {
        assertThrows(DaoException::class.java) { mAdditive.update() }
    }

    @Test
    fun updateWithNonNullDao_callsUpdateOnDao() {
        mAdditive.__setDaoSession(mockDaoSession)
        mAdditive.update()
        verify(mockAdditiveDao).update(mAdditive)
    }

    companion object {
        private const val ADDITIVE_NAME_NAME_1 = "AdditiveName"
        private const val ADDITIVE_NAME_NAME_2 = "AdditiveName2"
        private val ADDITIVE_NAME_1 = AdditiveName(ADDITIVE_NAME_NAME_1)
        private val ADDITIVE_NAME_2 = AdditiveName(ADDITIVE_NAME_NAME_2)
    }
}