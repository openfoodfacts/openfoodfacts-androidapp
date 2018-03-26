package openfoodfacts.github.scrachx.openfood.category.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.Assert;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.category.model.Category;
import openfoodfacts.github.scrachx.openfood.category.network.CategoryResponse;
import openfoodfacts.github.scrachx.openfood.utils.FileHelper;

/**
 * Created by Abdelali Eramli on 01/01/2018.
 */

public class CategoryMapperTest {

    @Test
    public void fromNetwork_FullResponse_CategoryList() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        CategoryResponse response = mapper.readValue(FileHelper
                .readTextFileFromResources("mock_categories.json", this.getClass().getClassLoader()), CategoryResponse.class);
        List<Category> categories = new CategoryMapper().fromNetwork(response.getTags());
        Assert.assertEquals(response.getTags().size(), categories.size());
    }
}