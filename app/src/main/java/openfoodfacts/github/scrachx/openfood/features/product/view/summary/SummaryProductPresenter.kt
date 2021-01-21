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
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.models.AnnotationAnswer
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelName
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper
import openfoodfacts.github.scrachx.openfood.utils.ProductInfoState

/**
 * Created by Lobster on 17.03.18.
 */
class SummaryProductPresenter(
        private val product: Product,
        private val view: ISummaryProductPresenter.View
) : ISummaryProductPresenter.Actions {
    private val disp = CompositeDisposable()

    override fun loadAdditives() {
        val additivesTags = product.additivesTags
        if (additivesTags.isEmpty()) {
            view.showAdditivesState(ProductInfoState.EMPTY)
            return
        }

        val languageCode = LocaleHelper.getLanguage(OFFApplication.instance)
        additivesTags.toObservable()
                .flatMapSingle { tag: String? ->
                    ProductRepository.getAdditiveByTagAndLanguageCode(tag, languageCode)
                            .flatMap { categoryName: AdditiveName ->
                                if (categoryName.isNull) {
                                    return@flatMap ProductRepository.getAdditiveByTagAndDefaultLanguageCode(tag)
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
                .doOnError {
                    Log.e(SummaryProductPresenter::class.java.simpleName, "loadAdditives", it)
                    view.showAdditivesState(ProductInfoState.EMPTY)
                }
                .subscribe { additives ->
                    if (additives.isEmpty()) {
                        view.showAdditivesState(ProductInfoState.EMPTY)
                    } else {
                        view.showAdditives(additives)
                    }
                }.addTo(disp)
    }

    override fun loadAllergens(runIfError: (() -> Unit)?) {
        val languageCode = LocaleHelper.getLanguage(OFFApplication.instance)
        ProductRepository.getAllergensByEnabledAndLanguageCode(true, languageCode)
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
        if (categoriesTags != null && categoriesTags.isNotEmpty()) {
            val languageCode = LocaleHelper.getLanguage(OFFApplication.instance)
            categoriesTags.toObservable()
                    .flatMapSingle { tag: String? ->
                        ProductRepository.getCategoryByTagAndLanguageCode(tag, languageCode)
                                .flatMap { categoryName: CategoryName ->
                                    return@flatMap if (categoryName.isNull) {
                                        ProductRepository.getCategoryByTagAndLanguageCode(tag)
                                    } else {
                                        Single.just(categoryName)
                                    }
                                }
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
            val languageCode = LocaleHelper.getLanguage(OFFApplication.instance)
            labelsTags.toObservable()
                    .flatMapSingle { tag: String? ->
                        ProductRepository.getLabelByTagAndLanguageCode(tag, languageCode)
                                .flatMap { labelName: LabelName ->
                                    if (labelName.isNull) {
                                        return@flatMap ProductRepository.getLabelByTagAndDefaultLanguageCode(tag)
                                    } else {
                                        return@flatMap Single.just(labelName)
                                    }
                                }
                    }
                    .filter { obj: LabelName? -> obj != null && obj.isNotNull }
                    .toList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { view.showLabelsState(ProductInfoState.LOADING) }
                    .doOnError { e ->
                        Log.e(SummaryProductPresenter::class.java.simpleName, "loadLabels", e)
                        view.showLabelsState(ProductInfoState.EMPTY)
                    }
                    .subscribe { labels ->
                        if (labels.isEmpty()) {
                            view.showLabelsState(ProductInfoState.EMPTY)
                        } else {
                            view.showLabels(labels)
                        }
                    }.addTo(disp)

        } else {
            view.showLabelsState(ProductInfoState.EMPTY)
        }
    }

    override fun loadProductQuestion() {
        val languageCode = LocaleHelper.getLanguage(OFFApplication.instance)
        ProductRepository.getProductQuestion(product.code, languageCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Log.e(this@SummaryProductPresenter::class.simpleName, "loadProductQuestion", it) }
                .subscribe { question -> view.showProductQuestion(question) }
                .addTo(disp)

    }

    override fun loadAnalysisTags() {
        if (!isFlavors(AppFlavors.OFF, AppFlavors.OBF, AppFlavors.OPFF)) return

        val analysisTags = product.ingredientsAnalysisTags
        val languageCode = LocaleHelper.getLanguage(OFFApplication.instance)
        if (analysisTags.isNotEmpty()) {
            analysisTags.toObservable()
                    .flatMapMaybe { ProductRepository.getAnalysisTagConfigByTagAndLanguageCode(it, languageCode) }
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
            ProductRepository.getUnknownAnalysisTagConfigsByLanguageCode(languageCode)
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
        ProductRepository.annotateInsight(insightId, annotation)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Log.e(this@SummaryProductPresenter::class.simpleName, "annotateInsight", it) }
                .subscribe { response -> view.showAnnotatedInsightToast(response) }
                .addTo(disp)
    }

    override fun dispose() = disp.dispose()

    override fun isDisposed() = disp.isDisposed
}