package openfoodfacts.github.scrachx.openfood.category;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import openfoodfacts.github.scrachx.openfood.category.mapper.CategoryMapper;
import openfoodfacts.github.scrachx.openfood.category.model.Category;
import openfoodfacts.github.scrachx.openfood.category.network.CategoryNetworkService;
import openfoodfacts.github.scrachx.openfood.category.network.CategoryResponse;

/**
 * Created by Abdelali Eramli on 01/01/2018.
 */

@RunWith(MockitoJUnitRunner.class)
public class CategoryRepositoryTest {
    @Mock
    private CategoryMapper mapper;
    @Mock
    private CategoryNetworkService networkService;
    @Mock
    private Category category;
    @Mock
    private CategoryResponse response;

    private CategoryRepository repository;

    @Before
    public void setup() {
        Mockito.when(mapper.fromNetwork(ArgumentMatchers.any())).thenReturn(Arrays.asList(category, category, category));
        Mockito.when(networkService.getCategories()).thenReturn(Single.just(response));
        repository = new CategoryRepository(networkService, mapper);
    }

    @Test
    public void retrieveAll_Success() {
        TestObserver<List<Category>> testObserver = new TestObserver<>();
        repository.retrieveAll().subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        List<Category> result = testObserver.values().get(0);
        Assert.assertThat(result.get(0), CoreMatchers.is(category));
    }
}
