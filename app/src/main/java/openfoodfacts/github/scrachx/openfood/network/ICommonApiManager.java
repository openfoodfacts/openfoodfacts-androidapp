package openfoodfacts.github.scrachx.openfood.network;

import openfoodfacts.github.scrachx.openfood.network.services.AnalysisDataAPI;
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI;
import openfoodfacts.github.scrachx.openfood.network.services.RobotoffAPI;

/**
 * Created by Lobster on 03.03.18.
 */

public interface ICommonApiManager {
    AnalysisDataAPI getAnalysisDataApi();

    ProductsAPI getProductsApi();

    RobotoffAPI getRobotoffApi();

}
