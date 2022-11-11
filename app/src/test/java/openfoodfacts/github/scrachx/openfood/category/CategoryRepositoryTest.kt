package openfoodfacts.github.scrachx.openfood.category

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import openfoodfacts.github.scrachx.openfood.category.mapper.CategoryMapper
import openfoodfacts.github.scrachx.openfood.category.model.Category
import openfoodfacts.github.scrachx.openfood.category.network.CategoryNetworkService
import openfoodfacts.github.scrachx.openfood.category.network.CategoryResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * Created by Abdelali Eramli on 01/01/2018.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class CategoryRepositoryTest {
    @MockK
    private lateinit var mapper: CategoryMapper

    @MockK
    private lateinit var networkService: CategoryNetworkService

    @MockK
    private lateinit var category: Category

    private val response = CategoryResponse()
    private lateinit var repository: CategoryRepository

    @BeforeEach
    fun setup() {
        every { mapper.fromNetwork(any()) } returns listOf(category, category, category)
        coEvery { networkService.getCategories() } returns response

        repository = CategoryRepository(networkService, mapper)
    }

    @Test
    fun retrieveAll_Success() = runTest {
        val result = repository.retrieveAll()!![0]
        assertThat(result).isEqualTo(category)
    }

}