package openfoodfacts.github.scrachx.openfood.network;

import openfoodfacts.github.scrachx.openfood.network.services.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.network.services.ProductApiService;
import openfoodfacts.github.scrachx.openfood.network.services.RobotoffAPIService;

/**
 * Created by Lobster on 03.03.18.
 */

public interface ICommonApiManager {

    ProductApiService getProductApiService();

    OpenFoodAPIService getOpenFoodApiService();

    RobotoffAPIService getRobotoffApiService();

}
