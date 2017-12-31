package openfoodfacts.github.scrachx.openfood.fragments;

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

import com.afollestad.materialdialogs.MaterialDialog;

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
 *
 * There is a tab called "Personal Rating" on each product view pager.
 * In this tab, the user can create an evaluation on the product,
 * consist of stars rating (required) and a comment (possible).
 *
 *
 * If there is no rating saved, then the tab will contain:
 * an editable rating bar, an editable edit text view and a save button.
 * The save button will be enabled only if the user set number of stars to the rating bar.
 * Fun fact: in order to make it appear even more user friendly and modern,
 * each number rating will appear with a different color,
 * meaning if the rating is only one star it will appear red whereas five stars will appear green etc.
 *
 *
 * If there is a rating saved, then the tab will contain:
 * an non-editable rating bar (indicator), an non-editable edit text view, an edit and a delete buttons.
 *
 * If the user wants to make a change either on rating bar or the edit text view,
 * he has to press the edit button first.
 *
 * If the user press the delete button, then a message dialog will pop up,
 * asking the user to confirm the delete action.
 *
 *
 * Note that each rating is only visible to the user who created the rating and only him.
 * No rating is being saved online. Relative warning appears everytime the user creates new rating.
 */
public class RatingFragment extends BaseFragment {
    private static RatingProductDao ratingDao;

    private Product currentProduct;
    private RatingProduct productRating;
    private LayerDrawable ratingDrawable;

    @BindView(R.id.ratingRatingBar)
    RatingBar productRatingBar;

    @BindView(R.id.ratingCommentEditText)
    EditText ratingComment;

    @BindView(R.id.ratingEditDeleteLinearLayout)
    LinearLayout editDeleteButtons;

    @BindView(R.id.ratingSaveButton)
    Button saveRating;

    @BindView(R.id.ratingDeleteButton)
    Button deleteRating;

    @BindView(R.id.ratingEditButton)
    Button editRating;

    @BindView(R.id.ratingNoteTextView)
    TextView note;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ratingDao = Utils.getAppDaoSession(getActivity()).getRatingProductDao();
        return createView(inflater, container, R.layout.fragment_product_rating);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bun = getActivity().getIntent().getExtras();

        State state = (State) getActivity().getIntent().getExtras().getSerializable("state");

        final Product product = state.getProduct();

        currentProduct = product;
        List<RatingProduct> ratings = ratingDao.queryBuilder().where(RatingProductDao.Properties.Barcode.eq(product.getCode())).list();

        productRating = ratings.isEmpty() ? null : ratings.get(0);

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

    @OnClick(R.id.ratingDeleteButton)
    public void deleteRating() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.delete_rating)
                .content(R.string.delete_rating_prompt)
                .positiveText(R.string.txtYes)
                .negativeText(R.string.txtNo)
                .onPositive((dialog, which) -> {
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
                })
                .show();
    }

    @OnClick(R.id.ratingEditButton)
    public void editRating() {
        productRatingBar.setIsIndicator(false);
        ratingComment.setEnabled(true);
        editDeleteButtons.setVisibility(View.GONE);
        saveRating.setVisibility(View.VISIBLE);
        note.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.ratingSaveButton)
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