package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.WikidataApiClient;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;
import openfoodfacts.github.scrachx.openfood.views.product.ProductFragment;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

/**
 * Created by prajwalm on 14/04/18.
 */

public class ContributorsFragment extends BaseFragment {

    private State mState;
    @BindView(R.id.creator)
    TextView creatorText;
    @BindView(R.id.last_editor)
    TextView lastEditorText;
    @BindView(R.id.other_editors)
    TextView otherEditorsText;
    @BindView(R.id.states)
    TextView statesText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_contributors);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Intent intent = getActivity().getIntent();
        if(intent!=null && intent.getExtras()!=null && intent.getExtras().getSerializable("state")!=null) {
            mState = (State) intent.getExtras().getSerializable("state");
        }else{
            mState = ProductFragment.mState;
        }
        refreshView(mState);
    }

    @Override
    public void refreshView(State state) {
        super.refreshView(state);
        mState = state;

        final Product product = mState.getProduct();
        if (isNotBlank(product.getCreator())) {
            String[] createdDate = getDateTime(product.getCreatedDateTime());
            String creatorTxt = getString(R.string.creator_history, createdDate[0], createdDate[1]);
            creatorText.setMovementMethod(LinkMovementMethod.getInstance());
            creatorText.setText(creatorTxt + " ");
            creatorText.append(getContributorsTag(product.getCreator()));
        } else {
            creatorText.setVisibility(View.INVISIBLE);
        }

        if (isNotBlank(product.getLastModifiedBy())) {
            String[] lastEditDate = getDateTime(product.getLastModifiedTime());
            String editorTxt = getString(R.string.last_editor_history, lastEditDate[0], lastEditDate[1]);
            lastEditorText.setMovementMethod(LinkMovementMethod.getInstance());
            lastEditorText.setText(editorTxt + " ");
            lastEditorText.append(getContributorsTag(product.getLastModifiedBy()));

        } else {
            lastEditorText.setVisibility(View.INVISIBLE);
        }

        if (product.getStatesTags().size() != 0) {
            String otherEditorsTxt = getString(R.string.other_editors);
            otherEditorsText.setMovementMethod(LinkMovementMethod.getInstance());
            otherEditorsText.setText(otherEditorsTxt + " ");
            for (int i = 0; i < product.getEditors().size() - 1; i++) {
                otherEditorsText.append(getContributorsTag(product.getEditors().get(i)).subSequence(0, product.getEditors().get(i).length()));
                otherEditorsText.append(", ");
            }
            otherEditorsText.append(getContributorsTag(product.getEditors().get(product.getEditors().size() - 1)));
        } else {
            otherEditorsText.setVisibility(View.INVISIBLE);
        }

        if (!product.getStatesTags().equals("")) {

            statesText.setMovementMethod(LinkMovementMethod.getInstance());
            statesText.setText("");
            for (int i = 0; i < product.getStatesTags().size(); i++) {
                statesText.append(getStatesTag(product.getStatesTags().get(i).split(":")[1]));
                statesText.append("\n ");
            }
        }

    }

    private String[] getDateTime(String dateTime) {
        long unixSeconds = Long.valueOf(dateTime);
        Date date = new java.util.Date(unixSeconds * 1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM dd, yyyy");
        SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("HH:mm:ss a");
        sdf2.setTimeZone(java.util.TimeZone.getTimeZone("CET"));
        String[] formattedDates = new String[]{sdf.format(date), sdf2.format(date)};
        return formattedDates;
    }

    private CharSequence getContributorsTag(String contributor) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                ProductBrowsingListActivity.startActivity(getContext(), contributor, SearchType.CONTRIBUTOR);
            }
        };
        spannableStringBuilder.append(contributor);
        spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append(" ");
        return spannableStringBuilder;
    }


    private CharSequence getStatesTag(String state) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                ProductBrowsingListActivity.startActivity(getContext(), state, SearchType.STATE);
            }
        };
        spannableStringBuilder.append(state);
        spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append(" ");
        return spannableStringBuilder;
    }


    /**  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Intent intent = getActivity().getIntent();
    mState = (State) intent.getExtras().getSerializable("state");
    final Product product = mState.getProduct();

    if (isNotBlank(product.getCreator())) {

    String[] createdDate = getDateTime(product.getCreatedDateTime());
    String creatorTxt = getString(R.string.creator_history, createdDate[0], createdDate[1]);
    creatorText.setMovementMethod(LinkMovementMethod.getInstance());
    creatorText.setText(creatorTxt + " ");
    creatorText.append(getContributorsTag(product.getCreator()));

    } else {
    creatorText.setVisibility(View.INVISIBLE);
    }


    if (isNotBlank(product.getLastModifiedBy())) {
    String[] lastEditDate = getDateTime(product.getLastModifiedTime());
    String editorTxt = getString(R.string.last_editor_history, lastEditDate[0], lastEditDate[1]);
    lastEditorText.setMovementMethod(LinkMovementMethod.getInstance());
    lastEditorText.setText(editorTxt + " ");
    lastEditorText.append(getContributorsTag(product.getLastModifiedBy()));

    } else {
    lastEditorText.setVisibility(View.INVISIBLE);
    }


    if (product.getStatesTags().size() != 0) {
    String otherEditorsTxt = getString(R.string.other_editors);
    otherEditorsText.setMovementMethod(LinkMovementMethod.getInstance());
    otherEditorsText.setText(otherEditorsTxt + " ");

    for (int i = 0; i < product.getEditors().size() - 1; i++) {
    otherEditorsText.append(getContributorsTag(product.getEditors().get(i)));
    otherEditorsText.append(",");
    }
    otherEditorsText.append(getContributorsTag(product.getEditors().get(product.getEditors().size() - 1)));
    } else {
    otherEditorsText.setVisibility(View.INVISIBLE);
    }


    if (!product.getStatesTags().equals("")) {

    statesText.setMovementMethod(LinkMovementMethod.getInstance());
    statesText.setText("");
    for (int i = 0; i < product.getStatesTags().size(); i++) {
    statesText.append(getStatesTag(product.getStatesTags().get(i).split(":")[1]));
    statesText.append("\n ");
    }
    }

    if (isNotBlank(product.getImageFrontUrl())) {
    Picasso.with(getContext()).load(product.getImageFrontUrl()).into(imgFront);
    }

    if (isNotBlank(product.getImageIngredientsUrl())) {
    Picasso.with(getContext()).load(product.getImageIngredientsUrl()).into(imgIngredients);
    }

    if (isNotBlank(product.getImageNutritionUrl())) {
    Picasso.with(getContext()).load(product.getImageNutritionUrl()).into(imgNutrients);
    }

    }

    private String[] getDateTime(String dateTime) {
    long unixSeconds = Long.valueOf(dateTime);
    Date date = new java.util.Date(unixSeconds * 1000L);
    SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM dd, yyyy");
    SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("HH:mm:ss a");
    sdf2.setTimeZone(java.util.TimeZone.getTimeZone("CET"));
    String[] formattedDates = new String[]{sdf.format(date), sdf2.format(date)};
    return formattedDates;
    }


    private CharSequence getContributorsTag(String contributor) {
    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
    ClickableSpan clickableSpan = new ClickableSpan() {
    @Override public void onClick(View view) {

    ProductBrowsingListActivity.startActivity(getContext(), contributor, SearchType.CONTRIBUTOR);

    }
    };
    spannableStringBuilder.append(contributor);
    spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
    spannableStringBuilder.append(" ");
    return spannableStringBuilder;
    }


    private CharSequence getStatesTag(String state) {
    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
    ClickableSpan clickableSpan = new ClickableSpan() {
    @Override public void onClick(View view) {

    ProductBrowsingListActivity.startActivity(getContext(), state, SearchType.STATE);

    }
    };
    spannableStringBuilder.append(state);
    spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
    spannableStringBuilder.append(" ");
    return spannableStringBuilder;
    }**/

}

