package openfoodfacts.github.scrachx.openfood.network;

/**
 * Created by Lobster on 03.03.18.
 */

public interface ICommonApiManager {

    ProductApiService getProductApiService();

    OpenFoodAPIService getOpenFoodApiService();

}
