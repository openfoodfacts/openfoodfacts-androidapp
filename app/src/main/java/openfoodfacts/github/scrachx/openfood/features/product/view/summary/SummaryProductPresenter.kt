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
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
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
) : ISummaryProductPresenter.Actions, Disposable {
    private val disposable = CompositeDisposable()
    override fun loadAdditives() {
        val additivesTags = product.additivesTags
        if (additivesTags != null && additivesTags.isNotEmpty()) {
            val languageCode = LocaleHelper.getLanguage(OFFApplication.instance)
            disposable.add(Observable.fromArray(*additivesTags.toTypedArray())
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
                    .subscribe({ additives ->
                        if (additives.isEmpty()) {
                            view.showAdditivesState(ProductInfoState.EMPTY)
                        } else {
                            view.showAdditives(additives)
                        }
                    }) { e: Throwable? ->
                        Log.e(SummaryProductPresenter::class.java.simpleName, "loadAdditives", e)
                        view.showAdditivesState(ProductInfoState.EMPTY)
                    })
        } else {
            view.showAdditivesState(ProductInfoState.EMPTY)
        }
    }

    override fun loadAllergens(runIfError: Runnable?) {
        val languageCode = LocaleHelper.getLanguage(OFFApplication.instance)
        disposable.add(ProductRepository.getAllergensByEnabledAndLanguageCode(true, languageCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ allergens -> view.showAllergens(allergens) }) { e: Throwable? ->
                    runIfError?.run()
                    Log.e(SummaryProductPresenter::class.java.simpleName, "loadAllergens", e)
                })
    }

    override fun loadCategories() {
        val categoriesTags = product.categoriesTags
        if (categoriesTags != null && categoriesTags.isNotEmpty()) {
            val languageCode = LocaleHelper.getLanguage(OFFApplication.instance)
            disposable.add(Observable.fromArray(*categoriesTags.toTypedArray())
                    .flatMapSingle { tag: String? ->
                        ProductRepository.getCategoryByTagAndLanguageCode(tag, languageCode)
                                .flatMap { categoryName: CategoryName ->
                                    return@flatMap if (categoryName.isNull) {
                                        ProductRepository.getCategoryByTagAndDefaultLanguageCode(tag)
                                    } else {
                                        Single.just(categoryName)
                                    }
                                }
                    }
                    .toList()
                    .doOnSubscribe { view.showCategoriesState(ProductInfoState.LOADING) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ categories ->
                        if (categories.isEmpty()) {
                            view.showCategoriesState(ProductInfoState.EMPTY)
                        } else {
                            view.showCategories(categories)
                        }
                    }) { e: Throwable? ->
                        Log.e(SummaryProductPresenter::class.java.simpleName, "loadCategories", e)
                        view.showCategoriesState(ProductInfoState.EMPTY)
                    })
        } else {
            view.showCategoriesState(ProductInfoState.EMPTY)
        }
    }

    override fun loadLabels() {
        val labelsTags = product.labelsTags
        if (labelsTags != null && labelsTags.isNotEmpty()) {
            val languageCode = LocaleHelper.getLanguage(OFFApplication.instance)
            disposable.add(
                    Observable.fromArray(*labelsTags.toTypedArray())
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
                            .doOnSubscribe { view.showLabelsState(ProductInfoState.LOADING) }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ labels ->
                                if (labels.isEmpty()) {
                                    view.showLabelsState(ProductInfoState.EMPTY)
                                } else {
                                    view.showLabels(labels)
                                }
                            }) { e: Throwable? ->
                                Log.e(SummaryProductPresenter::class.java.simpleName, "loadLabels", e)
                                view.showLabelsState(ProductInfoState.EMPTY)
                            }
            )
        } else {
            view.showLabelsState(ProductInfoState.EMPTY)
        }
    }

    override fun loadProductQuestion() {
        val languageCode = LocaleHelper.getLanguage(OFFApplication.instance)
        disposable.add(ProductRepository.getSingleProductQuestion(product.code, languageCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ view.showProductQuestion(it) })
                { e: Throwable? -> Log.e(this@SummaryProductPresenter.javaClass.simpleName, "loadProductQuestion", e) }
        )
    }

    override fun loadAnalysisTags() {
        if (!isFlavors(AppFlavors.OFF, AppFlavors.OBF, AppFlavors.OPFF)) {
            return
        }
        val analysisTags = product.ingredientsAnalysisTags
        val languageCode = LocaleHelper.getLanguage(OFFApplication.instance)
        if (analysisTags != null && analysisTags.isNotEmpty()) {
            disposable.add(Observable.fromIterable(analysisTags)
                    .flatMapMaybe { tag: String? -> ProductRepository.getAnalysisTagConfigByTagAndLanguageCode(tag, languageCode) }
                    .toList()
                    .doOnSubscribe { view.showLabelsState(ProductInfoState.LOADING) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ analysisTagConfigs ->
                        if (analysisTagConfigs.isEmpty()) {
                            view.showLabelsState(ProductInfoState.EMPTY)
                        } else {
                            view.showAnalysisTags(analysisTagConfigs)
                        }
                    }) { e: Throwable? ->
                        Log.e(SummaryProductPresenter::class.java.simpleName, "loadAnalysisTags", e)
                        view.showLabelsState(ProductInfoState.EMPTY)
                    })
        } else {
            disposable.add(ProductRepository.getUnknownAnalysisTagConfigsByLanguageCode(languageCode)
                    .doOnSubscribe { view.showLabelsState(ProductInfoState.LOADING) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ analysisTagConfigs ->
                        if (analysisTagConfigs.isEmpty()) {
                            view.showLabelsState(ProductInfoState.EMPTY)
                        } else {
                            view.showAnalysisTags(analysisTagConfigs)
                        }
                    }) { e: Throwable? ->
                        Log.e(SummaryProductPresenter::class.java.simpleName, "loadAnalysisTags", e)
                        view.showLabelsState(ProductInfoState.EMPTY)
                    })
        }
    }

    override fun annotateInsight(insightId: String, annotation: AnnotationAnswer) {
        disposable.add(ProductRepository.annotateInsight(insightId, annotation)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ annotationResponse -> view.showAnnotatedInsightToast(annotationResponse) })
                { e: Throwable? -> Log.e(this@SummaryProductPresenter.javaClass.simpleName, "annotateInsight", e) })
    }

    override fun dispose() {
        disposable.dispose()
    }

    override fun isDisposed(): Boolean {
        return disposable.isDisposed
    }
}