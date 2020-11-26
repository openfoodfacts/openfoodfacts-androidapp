/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package openfoodfacts.github.scrachx.openfood.features.product.view.ingredients

import android.util.Log
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper
import openfoodfacts.github.scrachx.openfood.utils.ProductInfoState

/**
 * Created by Lobster on 17.03.18.
 */
class IngredientsProductPresenter(
        private val product: Product,
        private val view: IIngredientsProductPresenter.View
) : IIngredientsProductPresenter.Actions {
    private val disposable = CompositeDisposable()
    private val repository = ProductRepository.instance

    override fun loadAdditives() {
        val additivesTags = product.additivesTags
        if (additivesTags != null && additivesTags.isNotEmpty()) {
            val languageCode = LocaleHelper.getLanguage(OFFApplication.getInstance())
            disposable.add(Observable.fromArray(*additivesTags.toTypedArray())
                    .flatMapSingle { tag: String? ->
                        repository.getAdditiveByTagAndLanguageCode(tag, languageCode).flatMap { categoryName: AdditiveName ->
                            if (categoryName.isNull) {
                                return@flatMap repository.getAdditiveByTagAndDefaultLanguageCode(tag)
                            } else {
                                return@flatMap Single.just(categoryName)
                            }
                        }
                    }
                    .filter { it.isNotNull }
                    .toList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { view.showAdditivesState(ProductInfoState.LOADING) }
                    .subscribe({ additives: List<AdditiveName> ->
                        if (additives.isEmpty()) {
                            view.showAdditivesState(ProductInfoState.EMPTY)
                        } else {
                            view.showAdditives(additives)
                        }
                    }) { e: Throwable? ->
                        Log.e(IngredientsProductPresenter::class.java.simpleName, "loadAdditives", e)
                        view.showAdditivesState(ProductInfoState.EMPTY)
                    }
            )
        } else {
            view.showAdditivesState(ProductInfoState.EMPTY)
        }
    }

    override fun loadAllergens() {
        val allergenTags = product.allergensTags
        if (allergenTags != null && allergenTags.isNotEmpty()) {
            val languageCode = LocaleHelper.getLanguage(OFFApplication.getInstance())
            disposable.add(Observable.fromArray(*allergenTags.toTypedArray())
                    .flatMapSingle { tag: String? ->
                        repository.getAllergenByTagAndLanguageCode(tag, languageCode).flatMap { allergenName: AllergenName ->
                            if (allergenName.isNull) {
                                return@flatMap repository.getAllergenByTagAndDefaultLanguageCode(tag)
                            } else {
                                return@flatMap Single.just(allergenName)
                            }
                        }
                    }
                    .toList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { view.showAllergensState(ProductInfoState.LOADING) }
                    .subscribe({ allergens: List<AllergenName> ->
                        if (allergens.isEmpty()) {
                            view.showAllergensState(ProductInfoState.EMPTY)
                        } else {
                            view.showAllergens(allergens)
                        }
                    }) { e: Throwable? ->
                        Log.e(IngredientsProductPresenter::class.java.simpleName, "loadAllergens", e)
                        view.showAllergensState(ProductInfoState.EMPTY)
                    }
            )
        } else {
            view.showAllergensState(ProductInfoState.EMPTY)
        }
    }

    override fun dispose() {
        if (!disposable.isDisposed) {
            disposable.clear()
        }
    }
}