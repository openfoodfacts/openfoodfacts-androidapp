package org.openfoodfacts.scanner.network;

/**
 * Created by Lobster on 03.03.18.
 */

public interface ICommonApiManager {

    ProductApiService getProductApiService();

    OpenFoodAPIService getOpenFoodApiService();

}
