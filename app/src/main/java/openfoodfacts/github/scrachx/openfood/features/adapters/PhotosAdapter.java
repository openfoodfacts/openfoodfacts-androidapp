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

package openfoodfacts.github.scrachx.openfood.features.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import openfoodfacts.github.scrachx.openfood.R;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder> {
    private final List<Uri> mPhotos;

    public PhotosAdapter(List<Uri> mPhotos) {
        this.mPhotos = mPhotos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView photo = (ImageView) LayoutInflater.from(parent.getContext())
            .inflate(R.layout.dialog_photo_item, parent, false);
        return new PhotosAdapter.ViewHolder(photo);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView imageView = holder.getImageView();
        VectorDrawableCompat drawableCompat = Objects.requireNonNull(
            VectorDrawableCompat.create(
                holder.getImageView().getResources(),
                R.drawable.ic_product_silhouette,
                null
            )
        );

        Picasso.get()
            .load(mPhotos.get(position))
            .placeholder(drawableCompat)
            .error(R.drawable.error_image)
            .into(imageView);
    }

    @Override
    public int getItemCount() {
        return mPhotos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }

        ImageView getImageView() {
            return (ImageView) itemView;
        }
    }
}
