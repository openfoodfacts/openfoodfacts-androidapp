package openfoodfacts.github.scrachx.openfood.category.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.truth.Truth
import openfoodfacts.github.scrachx.openfood.category.network.CategoryResponse
import openfoodfacts.github.scrachx.openfood.utils.FileTestUtils.readTextFileFromResources
import org.junit.Test
import java.io.IOException

/**
 * Created by Abdelali Eramli on 01/01/2018.
 */
class CategoryMapperTest {
    @Test
    @Throws(IOException::class)
    fun fromNetworkFullResponseCategoryList() {
        val mapper = ObjectMapper()
        val response = mapper.readValue(readTextFileFromResources("mock_categories.json", this.javaClass.classLoader!!), CategoryResponse::class.java)
        val categories = CategoryMapper().fromNetwork(response.tags)
        Truth.assertThat(response.tags).hasSize(categories.size)
    }
}