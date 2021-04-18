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

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.models.LanguageData

class LanguageDataAdapter(
        context: Context,
        resource: Int,
        objects: List<LanguageData>
) : ArrayAdapter<LanguageData>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        val data = getItem(position)
        view.setTextColor(ContextCompat.getColor(context, if (data!!.isSupported) R.color.white else R.color.orange))
        return view
    }

    fun getPosition(code: String) =
            (0 until count).firstOrNull { getItem(it)?.code == code } ?: -1

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent) as TextView
        val data = getItem(position) as LanguageData
        if (data.isSupported) {
            view.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        } else {
            view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_plus_blue_24, 0, 0, 0)
        }
        return view
    }
}
