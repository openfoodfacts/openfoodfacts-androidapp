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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.FragmentIngredientsAnalysisProductBinding
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_STATE
import openfoodfacts.github.scrachx.openfood.features.product.view.ingredients_analysis.adapter.IngredientAnalysisRecyclerAdapter
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.models.ProductIngredient
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.utils.requireProductState

@AndroidEntryPoint
class IngredientsAnalysisProductFragment : BaseFragment() {
    private var _binding: FragmentIngredientsAnalysisProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IngredientsAnalysisViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentIngredientsAnalysisProductBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshView(requireProductState())

        viewModel.ingredients
            .flowWithLifecycle(lifecycle)
            .onEach(::displayIngredientList)
            .launchIn(lifecycleScope)
    }

    private fun displayIngredientList(result: Result<List<ProductIngredient>>) {
        when (val ingredientList = result.getOrNull()) {
            null -> Toast.makeText(
                activity,
                requireActivity().getString(R.string.errorWeb),
                Toast.LENGTH_LONG
            ).show()

            else -> {
                val adapter = IngredientAnalysisRecyclerAdapter(ingredientList, requireActivity())
                binding.ingredientAnalysisRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
                binding.ingredientAnalysisRecyclerView.adapter = adapter
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun refreshView(productState: ProductState) {
        super.refreshView(productState)
        viewModel.updateProduct(productState.product!!)
    }

    companion object {
        fun newInstance(productState: ProductState) = IngredientsAnalysisProductFragment().apply {
            arguments = bundleOf(
                KEY_STATE to productState
            )
        }
    }
}