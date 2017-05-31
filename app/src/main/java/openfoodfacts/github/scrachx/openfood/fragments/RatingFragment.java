package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.RatingProduct;
import openfoodfacts.github.scrachx.openfood.models.RatingProductDao;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Class containing everything related to the users rating of the product.
 * A user can rate a product from 1 to 5 stars. Also, he can add a comment about the product.
 * A user is able to edit either the rating or the comment any time he wish.
 */

public class RatingFragment extends BaseFragment {
    private static RatingProductDao ratingDao;

    private Product currentProduct;
    private RatingProduct productRating;
    private LayerDrawable ratingDrawable;

    @BindView(R.id.productRating)
    RatingBar productRatingBar;

    @BindView(R.id.textRatingComment)
    EditText ratingComment;

    @BindView(R.id.edit_delete_buttons)
    LinearLayout editDeleteButtons;

    @BindView(R.id.saving_rating_button)
    Button saveRating;

    @BindView(R.id.delete_rating_button)
    Button deleteRating;

    @BindView(R.id.edit_rating_button)
    Button editRating;

    @BindView(R.id.rating_note_textview)
    TextView note;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ratingDao = Utils.getAppDaoSession(getActivity()).getRatingProductDao();
        return createView(inflater, container, R.layout.fragment_product_rating);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();

        State state = (State) intent.getExtras().getSerializable("state");

        final Product product = state.getProduct();

        currentProduct = product;
        List<RatingProduct> ratings = ratingDao.queryBuilder().where(RatingProductDao.Properties.Barcode.eq(product.getCode())).list();

        productRating = ratings.size() == 0 ? null : ratings.get(0);

        ratingDrawable = (LayerDrawable) productRatingBar.getProgressDrawable();

        if (ratings != null && productRating != null) {
            productRatingBar.setRating((float)productRating.getStars());
            changeRatingBarColor();
            productRatingBar.setIsIndicator(true);
            if(isNotBlank(productRating.getComment())) {
                ratingComment.setText(productRating.getComment());
            }
            else{
                ratingComment.setHint(R.string.no_rating_comment);
            }
            ratingComment.setEnabled(false);
            editDeleteButtons.setVisibility(View.VISIBLE);
            saveRating.setVisibility(View.GONE);
            note.setVisibility(View.GONE);
        }
        else{
            productRatingBar.setIsIndicator(false);
            ratingComment.setHint(R.string.enter_comment);
            ratingComment.setEnabled(true);
            editDeleteButtons.setVisibility(View.GONE);
            saveRating.setVisibility(View.VISIBLE);
            note.setVisibility(View.VISIBLE);
        }

        productRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                changeRatingBarColor();

                if(productRating == null){
                    if(rating == 0){
                        saveRating.setEnabled(false);
                    }
                    else{
                        saveRating.setEnabled(true);
                    }
                }
                else{
                    if(productRating.getComment().equals(ratingComment.getText()) && productRating.getStars() == (short)rating){
                        saveRating.setEnabled(false);
                    }
                    else{
                        saveRating.setEnabled(true);
                    }
                }
            }
        });

        ratingComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(productRating == null){
                    if(productRatingBar.getRating() == 0){
                        saveRating.setEnabled(false);
                    }
                    else{
                        saveRating.setEnabled(true);
                    }
                }
                else{
                    if((productRating.getComment().equals(s)
                        && productRating.getStars() == (short)productRatingBar.getRating())
                        || productRatingBar.getRating() == 0){
                        saveRating.setEnabled(false);
                    }
                    else{
                        saveRating.setEnabled(true);
                    }
                }
            }
        });
    }

    public void changeRatingBarColor(){
        int numStars = (int)productRatingBar.getRating();

        int color = R.color.red_800;
        if(numStars == 2) color = R.color.deep_orange_500;
        else if(numStars == 3) color = R.color.yellow_700;
        else if(numStars == 4) color = R.color.light_green_400;
        else if(numStars == 5) color = R.color.green_A700;

        ratingDrawable.getDrawable(2).setColorFilter(ContextCompat.getColor(productRatingBar.getContext(), color), PorterDuff.Mode.SRC_ATOP);

    }

    @OnClick(R.id.delete_rating_button)
    public void deleteRating() {
        ratingDao.delete(productRating);
        productRating = null;
        productRatingBar.setIsIndicator(false);
        productRatingBar.setRating(0);
        ratingComment.setText("");
        ratingComment.setHint(R.string.enter_comment);
        ratingComment.setEnabled(true);
        editDeleteButtons.setVisibility(View.GONE);
        saveRating.setVisibility(View.VISIBLE);
        note.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.edit_rating_button)
    public void editRating() {
        productRatingBar.setIsIndicator(false);
        ratingComment.setEnabled(true);
        editDeleteButtons.setVisibility(View.GONE);
        saveRating.setVisibility(View.VISIBLE);
        note.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.saving_rating_button)
    public void saveRating() {
        if(productRating == null) {
            productRating = new RatingProduct((short)productRatingBar.getRating(), ratingComment.getText().toString(),
                    currentProduct.getCode(), currentProduct.getProductName(), currentProduct.getImageFrontUrl());
            ratingDao.insert(productRating);
        }
        else{
            productRating.setStars((short)productRatingBar.getRating());
            productRating.setComment(ratingComment.getText().toString());
            ratingDao.update(productRating);
        }
        productRatingBar.setIsIndicator(true);
        ratingComment.setEnabled(false);
        editDeleteButtons.setVisibility(View.VISIBLE);
        saveRating.setVisibility(View.GONE);
        note.setVisibility(View.GONE);
        Toast.makeText(getContext(), R.string.save_rating_success, Toast.LENGTH_LONG).show();
    }
}