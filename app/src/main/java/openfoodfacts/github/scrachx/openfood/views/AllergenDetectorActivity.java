package openfoodfacts.github.scrachx.openfood.views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import holloway.allergenChecker.Consumer;
import holloway.allergenChecker.JSONManager;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.ConsumerFragment;
import openfoodfacts.github.scrachx.openfood.utils.ConsumerSwipeController;

public class AllergenDetectorActivity extends MainActivity implements ConsumerFragment.OnListFragmentInteractionListener {

    public static List<Consumer> consumerList = new ArrayList<Consumer>();
    @Nullable
    @BindView(R.id.consumerFAB)
    FloatingActionButton consumerFAB;
    ConsumerSwipeController consumerSwipeController = new ConsumerSwipeController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allergen_detector);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.allergenDetector);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        initializeConsumers();
    }


    /**
     * Loads Consumer objects from local JSON files.
     *
     * @author ross-holloway94
     */
    private void initializeConsumers() {
        if (consumerList.isEmpty()) {
            consumerList.addAll(JSONManager.getInstance().setUpConsumers());
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

    }

    /**
     * Allow the user to create a new Consumer by loading a new layout.
     */
    @Override
    public void onFABClick(View view) {
        //TODO create a new layout to add Consumer
    }

    /**
     * Allow the user to delete a Consumer by swiping item to the left.
     */
    @Override
    public void onConsumerItemSwipeLeft() {
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(consumerSwipeController);
        itemTouchhelper.attachToRecyclerView(findViewById(R.id.consumerRecycleView));

        //TODO Figure out how to get the item from the recyclerView
    }

    /**
     * Allow the user to edit a Consumer by swiping item to the right.
     */
    @Override
    public void onConsumerItemSwipeRight() {
        //TODO create a new layout to edit Consumer
    }
}
