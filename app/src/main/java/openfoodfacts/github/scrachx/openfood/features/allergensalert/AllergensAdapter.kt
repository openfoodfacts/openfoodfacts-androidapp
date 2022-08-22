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
package openfoodfacts.github.scrachx.openfood.features.allergensalert

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.databinding.ItemAllergensBinding
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.utils.AutoUpdatableAdapter

class AllergensAdapter(
    val onDeleteButtonClick: (allergen: AllergenName) -> Unit,
) : RecyclerView.Adapter<AllergensAdapter.ViewHolder>(), AutoUpdatableAdapter {

    var allergens: List<AllergenName> by autoNotifying { o, n -> o.allergenTag == n.allergenTag }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val contactView = ItemAllergensBinding.inflate(inflater, parent, false)
        return ViewHolder(contactView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val allergen = allergens[position]
        holder.bind(allergen) {
            onDeleteButtonClick(it)
        }
    }

    override fun onViewRecycled(viewHolder: ViewHolder) {
        super.onViewRecycled(viewHolder)
        viewHolder.unbind()
    }

    override fun getItemCount() = allergens.size

    class ViewHolder(private val binding: ItemAllergensBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AllergenName, onDeleteButtonClick: (allergen: AllergenName) -> Unit) {
            binding.allergenName.text = item.name
            binding.deleteButton.setOnClickListener { onDeleteButtonClick(item) }
        }

        fun unbind() {
            binding.deleteButton.setOnClickListener(null)
        }
    }
}
