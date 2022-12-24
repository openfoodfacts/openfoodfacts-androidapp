package openfoodfacts.github.scrachx.openfood.models.entities.additive

import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import org.greenrobot.greendao.DaoException
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Tests for [Additive]
 */
@ExtendWith(MockKExtension::class)
class AdditiveTest {
    @MockK
    private lateinit var mockDaoSession: DaoSession

    @MockK
    private lateinit var mockAdditiveDao: AdditiveDao

    @MockK
    private lateinit var mockAdditiveNameDao: AdditiveNameDao
    private lateinit var additive: Additive

    @BeforeEach
    fun beforeEach() {
        every { mockDaoSession.additiveNameDao } returns mockAdditiveNameDao
        every { mockDaoSession.additiveDao } returns mockAdditiveDao

        additive = Additive()
    }

    @Test
    fun `get names with null dao throws DaoException`() {
        assertThrows<DaoException> { additive.names }
    }

    @Test
    fun `get names with null names and non null dao sets names from AdditiveNamesDao`() {
        every { mockAdditiveNameDao._queryAdditive_Names(any()) }
            .returns(listOf(ADDITIVE_NAME_1, ADDITIVE_NAME_2))

        additive.__setDaoSession(mockDaoSession)

        val names = additive.names
        assertThat(names).hasSize(2)
        assertThat(names[0]!!.name).isEqualTo(ADDITIVE_NAME_NAME_1)
        assertThat(names[1]!!.name).isEqualTo(ADDITIVE_NAME_NAME_2)
    }

    @Test
    fun `delete with null dao throws DaoException`() {
        assertThrows<DaoException> { additive.delete() }
    }

    @Test
    fun `delete with non null dao calls delete on dao`() {
        every { mockAdditiveDao.delete(any()) } just Runs
        additive.__setDaoSession(mockDaoSession)
        additive.delete()
        verify { mockAdditiveDao.delete(additive) }
    }

    @Test
    fun `refresh with null dao throws DaoException`() {
        assertThrows(DaoException::class.java) { additive.refresh() }
    }

    @Test
    fun `refresh with non null dao calls refresh on dao`() {
        every { mockAdditiveDao.refresh(any()) } just Runs
        additive.__setDaoSession(mockDaoSession)
        additive.refresh()
        verify { mockAdditiveDao.refresh(additive) }
    }

    @Test
    fun `update with null dao throws DaoException`() {
        assertThrows<DaoException> { additive.update() }
    }

    @Test
    fun `update with non null dao calls update on dao`() {
        every { mockAdditiveDao.update(any()) } just Runs
        additive.__setDaoSession(mockDaoSession)
        additive.update()
        verify { mockAdditiveDao.update(additive) }
    }

    companion object {
        private const val ADDITIVE_NAME_NAME_1 = "AdditiveName"
        private const val ADDITIVE_NAME_NAME_2 = "AdditiveName2"
        private val ADDITIVE_NAME_1 = AdditiveName(ADDITIVE_NAME_NAME_1)
        private val ADDITIVE_NAME_2 = AdditiveName(ADDITIVE_NAME_NAME_2)
    }
}