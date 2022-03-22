package openfoodfacts.github.scrachx.openfood.category.mapper

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.category.network.CategoryResponse
import openfoodfacts.github.scrachx.openfood.utils.readTextFileFromResources
import org.junit.jupiter.api.Test
import java.io.IOException

/**
 * Created by Abdelali Eramli on 01/01/2018.
 */
class CategoryMapperTest {
    @Test
    @Throws(IOException::class)
    fun fromNetworkFullResponseCategoryList() {
        val response = jacksonObjectMapper().readValue(readTextFileFromResources("mock_categories.json", this.javaClass.classLoader!!), CategoryResponse::class.java)
        val categories = CategoryMapper().fromNetwork(response.tags)
        assertThat(response.tags).hasSize(categories.size)
    }
}