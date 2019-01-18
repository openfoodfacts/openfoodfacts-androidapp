package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder> {

    private List<Uri> mPhotos;

    public PhotosAdapter(List<Uri> mPhotos) {
        this.mPhotos = mPhotos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView photo =
                (ImageView) LayoutInflater.from(parent.getContext())
                                          .inflate(R.layout.dialog_photo_item, parent, false);
        return new PhotosAdapter.ViewHolder(photo);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView imageView = holder.getImageView();

        Picasso.with(imageView.getContext())
               .load(mPhotos.get(position))
               .placeholder(R.drawable.placeholder_thumb)
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
