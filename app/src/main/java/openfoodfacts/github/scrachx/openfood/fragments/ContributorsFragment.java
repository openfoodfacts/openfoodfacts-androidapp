package openfoodfacts.github.scrachx.openfood.fragments;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentContributorsBinding;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.utils.FragmentUtils;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static org.apache.commons.lang.StringUtils.isNotBlank;
/*
 * Created by prajwalm on 14/04/18.
 */

/**
 * @see R.layout#fragment_contributors
 */
public class ContributorsFragment extends BaseFragment {
    private FragmentContributorsBinding binding;
    private State stateFromActivity;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentContributorsBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        stateFromActivity = FragmentUtils.requireStateFromArguments(this);

        refreshView(stateFromActivity);
    }

    @Override
    public void refreshView(State state) {
        super.refreshView(state);
        stateFromActivity = state;

        final Product product = stateFromActivity.getProduct();
        if (isNotBlank(product.getCreator())) {
            String[] createdDate = getDateTime(product.getCreatedDateTime());
            String creatorTxt = getString(R.string.creator_history, createdDate[0], createdDate[1], product.getCreator());
            binding.creatorTxt.setMovementMethod(LinkMovementMethod.getInstance());
            binding.creatorTxt.setText(creatorTxt);
        } else {
            binding.creatorTxt.setVisibility(View.INVISIBLE);
        }

        if (isNotBlank(product.getLastModifiedBy())) {
            String[] lastEditDate = getDateTime(product.getLastModifiedTime());
            String editorTxt = getString(R.string.last_editor_history, lastEditDate[0], lastEditDate[1], product.getLastModifiedBy());
            binding.lastEditorTxt.setMovementMethod(LinkMovementMethod.getInstance());
            binding.lastEditorTxt.setText(editorTxt);
        } else {
            binding.lastEditorTxt.setVisibility(View.INVISIBLE);
        }

        if (!product.getEditors().isEmpty()) {
            String otherEditorsTxt = getString(R.string.other_editors);
            binding.otherEditorsTxt.setMovementMethod(LinkMovementMethod.getInstance());
            binding.otherEditorsTxt.setText(otherEditorsTxt + " ");
            for (int i = 0; i < product.getEditors().size() - 1; i++) {
                final String editor = product.getEditors().get(i);
                binding.otherEditorsTxt.append(getContributorsTag(editor).subSequence(0, editor.length()));
                binding.otherEditorsTxt.append(", ");
            }
            binding.otherEditorsTxt.append(getContributorsTag(product.getEditors().get(product.getEditors().size() - 1)));
        } else {
            binding.otherEditorsTxt.setVisibility(View.INVISIBLE);
        }

        if (!product.getStatesTags().isEmpty()) {
            binding.statesTxt.setMovementMethod(LinkMovementMethod.getInstance());
            binding.statesTxt.setText("");
            for (int i = 0; i < product.getStatesTags().size(); i++) {
                binding.statesTxt.append(getStatesTag(product.getStatesTags().get(i).split(":")[1]));
                binding.statesTxt.append("\n ");
            }
        }
    }

    /**
     * Get date and time in MMMM dd, yyyy and HH:mm:ss a format
     *
     * @param dateTime date and time in miliseconds
     */
    private String[] getDateTime(String dateTime) {
        long unixSeconds = Long.parseLong(dateTime);
        Date date = new java.util.Date(unixSeconds * 1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM dd, yyyy", getResources().getConfiguration().locale);
        SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("HH:mm:ss a", getResources().getConfiguration().locale);
        sdf2.setTimeZone(java.util.TimeZone.getTimeZone("CET"));
        return new String[]{sdf.format(date), sdf2.format(date)};
    }

    private CharSequence getContributorsTag(String contributor) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
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
            public void onClick(@NonNull View view) {
                ProductBrowsingListActivity.startActivity(getContext(), state, SearchType.STATE);
            }
        };
        spannableStringBuilder.append(state);
        spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append(" ");
        return spannableStringBuilder;
    }
}

