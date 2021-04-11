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

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.squareup.picasso.Picasso
import openfoodfacts.github.scrachx.openfood.R

class PhotosAdapter(private val photoUris: List<Uri>) : RecyclerView.Adapter<PhotosAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val imageView = LayoutInflater.from(parent.context)
                .inflate(R.layout.dialog_photo_item, parent, false) as ImageView
        return ViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageView = holder.imageView
        val drawableCompat = VectorDrawableCompat.create(
                holder.imageView.resources,
                R.drawable.ic_product_silhouette,
                null
        )!!

        // Todo: remove this direct call
        Picasso.get()
                .load(photoUris[position])
                .placeholder(drawableCompat)
                .error(R.drawable.error_image)
                .into(imageView)
    }

    override fun getItemCount() = photoUris.size

    class ViewHolder(view: ImageView) : RecyclerView.ViewHolder(view) {
        val imageView = view
    }
}