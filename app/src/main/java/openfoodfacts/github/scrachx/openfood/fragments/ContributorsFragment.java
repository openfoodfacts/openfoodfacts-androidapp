package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.WikidataApiClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

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
        mState = (State) intent.getExtras().getSerializable("state");
        final Product product = mState.getProduct();

        if (!product.getCreator().equals("")) {

            String[] createdDate = getDateTime(product.getCreatedDateTime());
            String[] lastEditDate = getDateTime(product.getLastModifiedTime());
            String editors = getEditors(product.getEditors(), product.getCreator(), product.getLastModifiedBy());

            String creatorTxt = getString(R.string.creator_history, createdDate[0], createdDate[1], product.getCreator());
            String editorTxt = getString(R.string.last_editor_history, lastEditDate[0], lastEditDate[1], product.getLastModifiedBy());
            String otherEditorsTxt = getString(R.string.other_editors, editors);

            creatorText.setText(creatorTxt);
            lastEditorText.setText(editorTxt);
            otherEditorsText.setText(otherEditorsTxt);
        }

        if (!product.getStatesTags().equals("")) {

            statesText.setText(createStatesList(product.getStatesTags()));
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

    private String createStatesList(List<String> states) {

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < states.size(); i++) {


            builder.append(states.get(i));
            builder.append("\n");

        }

        return builder.toString();


    }

    private String getEditors(List<String> editorTags, String creator, String lastEditor) {

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < editorTags.size() - 2; i++) {


            builder.append(editorTags.get(i));
            builder.append(", ");

        }

        return builder.toString();

    }

}

