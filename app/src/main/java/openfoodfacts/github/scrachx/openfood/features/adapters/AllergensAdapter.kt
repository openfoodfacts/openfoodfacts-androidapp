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
package openfoodfacts.github.scrachx.openfood.features.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository

class AllergensAdapter(
        private val mProductRepository: ProductRepository,
        allergens: MutableList<AllergenName>?
) : RecyclerView.Adapter<AllergensAdapter.CustomViewHolder>() {

    var allergens: MutableList<AllergenName> = allergens ?: mutableListOf()


    class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageButton: Button = itemView.findViewById(R.id.delete_button)
        val nameTextView: TextView = itemView.findViewById(R.id.allergen_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val contactView = inflater.inflate(R.layout.item_allergens, parent, false)
        return CustomViewHolder(contactView)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val allergen = allergens[position]
        val textView = holder.nameTextView
        textView.text = allergen.name.substring(allergen.name.indexOf(':') + 1)
        val button = holder.messageButton
        button.setText(R.string.delete_txt)
        button.setOnClickListener {
            allergens.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
            mProductRepository.setAllergenEnabled(allergen.allergenTag, false)
        }
    }

    override fun getItemCount() = allergens.size
}