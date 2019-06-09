package openfoodfacts.github.scrachx.openfood.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @see R.layout#fragment_contributors
 */

public class ContributorsFragment extends BaseFragment {

    private State stateFromActivity;
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
        stateFromActivity =getStateFromActivityIntent();
        refreshView(stateFromActivity);
    }

    @Override
    public void refreshView(State state) {
        super.refreshView(state);
        stateFromActivity = state;

        final Product product = stateFromActivity.getProduct();
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

        if (!product.getEditors().isEmpty()) {
            String otherEditorsTxt = getString(R.string.other_editors);
            otherEditorsText.setMovementMethod(LinkMovementMethod.getInstance());
            otherEditorsText.setText(otherEditorsTxt + " ");
            for (int i = 0; i < product.getEditors().size() - 1; i++) {
                final String editor = product.getEditors().get(i);
                otherEditorsText.append(getContributorsTag(editor).subSequence(0, editor.length()));
                otherEditorsText.append(", ");
            }
            otherEditorsText.append(getContributorsTag(product.getEditors().get(product.getEditors().size() - 1)));
        } else {
            otherEditorsText.setVisibility(View.INVISIBLE);
        }

        if (!product.getStatesTags().isEmpty()) {
            statesText.setMovementMethod(LinkMovementMethod.getInstance());
            statesText.setText("");
            for (int i = 0; i < product.getStatesTags().size(); i++) {
                statesText.append(getStatesTag(product.getStatesTags().get(i).split(":")[1]));
                statesText.append("\n ");
            }
        }

    }

    /**
     * Get date and time in MMMM dd, yyyy and HH:mm:ss a format
     * @param dateTime date and time in miliseconds
     * */

    private String[] getDateTime(String dateTime) {
        long unixSeconds = Long.parseLong(dateTime);
        Date date = new java.util.Date(unixSeconds * 1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM dd, yyyy");
        SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("HH:mm:ss a");
        sdf2.setTimeZone(java.util.TimeZone.getTimeZone("CET"));
        return new String[]{sdf.format(date), sdf2.format(date)};
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


}

