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
package openfoodfacts.github.scrachx.openfood.features.product.view.ingredients_analysis

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.FragmentIngredientsAnalysisProductBinding
import openfoodfacts.github.scrachx.openfood.features.product.view.ingredients_analysis.adapter.IngredientAnalysisRecyclerAdapter
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.getProductState
import openfoodfacts.github.scrachx.openfood.utils.requireProductState

class IngredientsAnalysisProductFragment : BaseFragment() {
    private var _binding: FragmentIngredientsAnalysisProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var api: OpenFoodAPIClient
    private lateinit var product: Product

    private var adapter: IngredientAnalysisRecyclerAdapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        product = requireProductState().product!!
        api = OpenFoodAPIClient(requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentIngredientsAnalysisProductBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        api.getIngredients(product)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Toast.makeText(activity, requireActivity().getString(R.string.errorWeb), Toast.LENGTH_LONG).show() }
                .subscribe { ingredients ->
                    adapter = IngredientAnalysisRecyclerAdapter(ingredients, requireActivity())
                    binding.ingredientAnalysisRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
                    binding.ingredientAnalysisRecyclerView.adapter = adapter
                }.addTo(disp)

        getProductState()?.let { refreshView(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun refreshView(productState: ProductState) {
        super.refreshView(productState)
        product = productState.product!!
        adapter?.notifyDataSetChanged()
    }
}