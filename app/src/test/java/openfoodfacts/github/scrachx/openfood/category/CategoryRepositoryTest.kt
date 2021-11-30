package openfoodfacts.github.scrachx.openfood.category

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import openfoodfacts.github.scrachx.openfood.MockitoHelper
import openfoodfacts.github.scrachx.openfood.category.mapper.CategoryMapper
import openfoodfacts.github.scrachx.openfood.category.model.Category
import openfoodfacts.github.scrachx.openfood.category.network.CategoryNetworkService
import openfoodfacts.github.scrachx.openfood.category.network.CategoryResponse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

/**
 * Created by Abdelali Eramli on 01/01/2018.
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class CategoryRepositoryTest {
    @Mock
    private lateinit var mapper: CategoryMapper

    @Mock
    private lateinit var networkService: CategoryNetworkService

    @Mock
    private lateinit var category: Category

    @Mock
    private lateinit var response: CategoryResponse
    private lateinit var repository: CategoryRepository

    @Before
    fun setup() = runBlockingTest {
        whenever(mapper.fromNetwork(MockitoHelper.anyObject())) doReturn listOf(category, category, category)
        whenever(networkService.getCategories()) doReturn response
        repository = CategoryRepository(networkService, mapper)
    }

    @Test
    fun retrieveAll_Success() = runBlocking { // runBlockingTest made this crash
        val result = repository.retrieveAll()!![0]
        assertThat(result).isEqualTo(category)
    }

}