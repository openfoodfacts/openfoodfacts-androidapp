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
package openfoodfacts.github.scrachx.openfood.features.product.view.summary

import android.util.Log
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.models.AnnotationAnswer
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.ProductInfoState

class SummaryProductPresenter(
    private val languageCode: String,
    private val product: Product,
    private val view: ISummaryProductPresenter.View,
    private val productRepository: ProductRepository
) : ISummaryProductPresenter.Actions {
    private val disp = CompositeDisposable()

    override fun loadAdditives() {
        val additivesTags = product.additivesTags
        if (additivesTags.isEmpty()) {
            view.showAdditivesState(ProductInfoState.EMPTY)
            return
        }

        additivesTags.toObservable()
                .flatMapSingle { tag ->
                    productRepository.getAdditiveByTagAndLanguageCode(tag, languageCode).map { it to tag }
                }.flatMapSingle { (categoryName, tag) ->
                    if (categoryName.isNotNull) Single.just(categoryName) else productRepository.getAdditiveByTagAndDefaultLanguageCode(tag)
                }
                .filter { it.isNotNull }
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { view.showAdditivesState(ProductInfoState.LOADING) }
                .doOnError {
                    Log.e(SummaryProductPresenter::class.simpleName, "loadAdditives", it)
                    view.showAdditivesState(ProductInfoState.EMPTY)
                }
                .subscribe { additives ->
                    if (additives.isEmpty()) view.showAdditivesState(ProductInfoState.EMPTY) else view.showAdditives(additives)
                }.addTo(disp)
    }

    override fun loadAllergens(runIfError: (() -> Unit)?) {
        productRepository.getAllergensByEnabledAndLanguageCode(true, languageCode)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    runIfError?.invoke()
                    Log.e(SummaryProductPresenter::class.simpleName, "LoadAllergens", it)
                }
                .subscribe { allergens -> view.showAllergens(allergens) }
                .addTo(disp)
    }

    override fun loadCategories() {
        val categoriesTags = product.categoriesTags
        if (!categoriesTags.isNullOrEmpty()) {
            categoriesTags.toObservable()
                    .flatMapSingle { tag ->
                        productRepository.getCategoryByTagAndLanguageCode(tag, languageCode).map { it to tag }
                    }
                    .flatMapSingle { (categoryName, tag) ->
                        if (categoryName.isNotNull) Single.just(categoryName) else productRepository.getCategoryByTagAndLanguageCode(tag)
                    }
                    .toList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { view.showCategoriesState(ProductInfoState.LOADING) }
                    .doOnError {
                        Log.e(SummaryProductPresenter::class.java.simpleName, "loadCategories", it)
                        view.showCategoriesState(ProductInfoState.EMPTY)
                    }
                    .subscribe { categories ->
                        if (categories.isEmpty()) {
                            view.showCategoriesState(ProductInfoState.EMPTY)
                        } else {
                            view.showCategories(categories)
                        }
                    }.addTo(disp)
        } else {
            view.showCategoriesState(ProductInfoState.EMPTY)
        }
    }

    override fun loadLabels() {
        val labelsTags = product.labelsTags
        if (labelsTags != null && labelsTags.isNotEmpty()) {
            labelsTags.toObservable()
                    .flatMapSingle { tag ->
                        productRepository.getLabelByTagAndLanguageCode(tag, languageCode).map { it to tag }
                    }
                    .flatMapSingle { (labelName, tag) ->
                        if (labelName.isNotNull) Single.just(labelName) else productRepository.getLabelByTagAndDefaultLanguageCode(tag)
                    }
                    .filter { it.isNotNull }
                    .toList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { view.showLabelsState(ProductInfoState.LOADING) }
                    .doOnError {
                        Log.e(SummaryProductPresenter::class.java.simpleName, "loadLabels", it)
                        view.showLabelsState(ProductInfoState.EMPTY)
                    }
                    .subscribe { labels ->
                        if (labels.isEmpty()) view.showLabelsState(ProductInfoState.EMPTY)
                        else view.showLabels(labels)
                    }.addTo(disp)

        } else {
            view.showLabelsState(ProductInfoState.EMPTY)
        }
    }

    override fun loadProductQuestion() {
        productRepository.getProductQuestion(product.code, languageCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Log.e(this@SummaryProductPresenter::class.simpleName, "loadProductQuestion", it) }
                .subscribe { question -> view.showProductQuestion(question) }
                .addTo(disp)
    }

    override fun loadAnalysisTags() {
        if (!isFlavors(AppFlavors.OFF, AppFlavors.OBF, AppFlavors.OPFF)) return

        val analysisTags = product.ingredientsAnalysisTags
        if (analysisTags.isNotEmpty()) {
            analysisTags.toObservable()
                    .flatMapMaybe { productRepository.getAnalysisTagConfigByTagAndLanguageCode(it, languageCode) }
                    .toList()
                    .doOnSubscribe { view.showLabelsState(ProductInfoState.LOADING) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError {
                        Log.e(SummaryProductPresenter::class.java.simpleName, "loadAnalysisTags", it)
                        view.showLabelsState(ProductInfoState.EMPTY)
                    }
                    .subscribe { analysisTagConfigs ->
                        if (analysisTagConfigs.isEmpty()) {
                            view.showLabelsState(ProductInfoState.EMPTY)
                        } else {
                            view.showAnalysisTags(analysisTagConfigs)
                        }
                    }.addTo(disp)
        } else {
            productRepository.getUnknownAnalysisTagConfigsByLanguageCode(languageCode)
                    .doOnSubscribe { view.showLabelsState(ProductInfoState.LOADING) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError {
                        Log.e(SummaryProductPresenter::class.java.simpleName, "loadAnalysisTags", it)
                        view.showLabelsState(ProductInfoState.EMPTY)
                    }
                    .subscribe { analysisTagConfigs ->
                        if (analysisTagConfigs.isEmpty()) {
                            view.showLabelsState(ProductInfoState.EMPTY)
                        } else {
                            view.showAnalysisTags(analysisTagConfigs)
                        }
                    }.addTo(disp)
        }
    }

    override fun annotateInsight(insightId: String, annotation: AnnotationAnswer) {
        productRepository.annotateInsight(insightId, annotation)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Log.e(this@SummaryProductPresenter::class.simpleName, "annotateInsight", it) }
                .subscribe { response -> view.showAnnotatedInsightToast(response) }
                .addTo(disp)
    }

    override fun dispose() = disp.dispose()

    override fun isDisposed() = disp.isDisposed
}
