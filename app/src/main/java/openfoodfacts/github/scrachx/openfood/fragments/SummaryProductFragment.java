package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.State;

/**
 * Created by scotscriven on 04/05/15.
 */
public class SummaryProductFragment extends Fragment {

    ImageView img;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_summary_product,container,false);
        String imgUrl;
        img = (ImageView) rootView.findViewById(R.id.imageViewProduct);

        Intent intent = getActivity().getIntent();
        State state = (State) intent.getExtras().getSerializable("state");
        System.out.println(state.toString());

        if(state.getProduct().getImageUrl() == null){
            imgUrl = state.getProduct().getImageSmallUrl();
        }else{
            imgUrl = state.getProduct().getImageUrl();
        }
        setImageView(imgUrl);



        return rootView;
    }

    public void setImageView(String imgUrl){
        Ion.with(img)
                .placeholder(R.drawable.placeholder_thumb)
                .error(R.drawable.error_image)
                .load(imgUrl);
    }
}
