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

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper
import openfoodfacts.github.scrachx.openfood.utils.ProductInfoState.EMPTY
import openfoodfacts.github.scrachx.openfood.utils.ProductInfoState.LOADING

/**
 * Created by Lobster on 17.03.18.
 */
class IngredientsProductPresenter(
        private val context: Context,
        private val view: IIngredientsProductPresenter.View,
        private val productRepository: ProductRepository,
        private val product: Product,
        private val sharedPreferences: SharedPreferences
) : IIngredientsProductPresenter.Actions {
    private val disp = CompositeDisposable()

    override fun loadAdditives() {
        val additivesTags = product.additivesTags
        if (additivesTags.isEmpty()) {
            view.setAdditivesState(EMPTY)
            return
        }
        val languageCode = LocaleHelper.getLanguage(sharedPreferences)
        additivesTags.toObservable()
                .flatMapSingle { tag ->
                    productRepository.getAdditiveByTagAndLanguageCode(tag, languageCode).flatMap { categoryName ->
                        if (categoryName.isNull) {
                            productRepository.getAdditiveByTagAndDefaultLanguageCode(tag)
                        } else Single.just(categoryName)
                    }
                }
                .filter { it.isNotNull }
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { view.setAdditivesState(LOADING) }
                .doOnError {
                    Log.e(IngredientsProductPresenter::class.simpleName, "loadAdditives", it)
                    view.setAdditivesState(EMPTY)
                }
                .subscribe { additives ->
                    if (additives.isEmpty()) {
                        view.setAdditivesState(EMPTY)
                    } else {
                        view.showAdditives(additives)
                    }
                }.addTo(disp)

    }

    override fun loadAllergens() {
        val allergenTags = product.allergensTags
        if (allergenTags.isEmpty()) {
            view.setAllergensState(EMPTY)
            return
        }
        val languageCode = LocaleHelper.getLanguage(sharedPreferences)
        allergenTags.toObservable()
                .flatMapSingle { tag ->
                    productRepository.getAllergenByTagAndLanguageCode(tag, languageCode).flatMap { allergenName: AllergenName ->
                        if (allergenName.isNull) {
                            productRepository.getAllergenByTagAndDefaultLanguageCode(tag)
                        } else Single.just(allergenName)
                    }
                }
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { view.setAllergensState(LOADING) }
                .doOnError { e ->
                    Log.e(IngredientsProductPresenter::class.simpleName, "loadAllergens", e)
                    view.setAllergensState(EMPTY)
                }
                .subscribe { allergens ->
                    if (allergens.isEmpty()) {
                        view.setAllergensState(EMPTY)
                    } else {
                        view.showAllergens(allergens)
                    }
                }.addTo(disp)

    }

    override fun dispose() = disp.dispose()
    override fun isDisposed() = disp.isDisposed
}
