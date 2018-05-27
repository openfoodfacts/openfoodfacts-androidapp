package org.openfoodfacts.scanner.category.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.Assert;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import org.openfoodfacts.scanner.category.model.Category;
import org.openfoodfacts.scanner.category.network.CategoryResponse;
import org.openfoodfacts.scanner.utils.FileHelper;

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