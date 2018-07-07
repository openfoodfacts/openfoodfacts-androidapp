package openfoodfacts.github.scrachx.openfood.views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import java.util.HashSet;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import holloway.allergenChecker.Consumer;
import holloway.allergenChecker.JSONManager;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.ConsumerFragment;
import openfoodfacts.github.scrachx.openfood.utils.ConsumerSwipeController;

public class AllergenDetectorActivity extends BaseActivity implements ConsumerFragment.OnListFragmentInteractionListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @Nullable
    @BindView(R.id.consumerFAB)
    FloatingActionButton consumerFAB;
    ConsumerSwipeController consumerSwipeController = new ConsumerSwipeController();
    private JSONManager jsonManager = JSONManager.getInstance();

    public HashSet<Consumer> getConsumerList() {
        return jsonManager.getConsumers();
    }

    /**
     * Check if the activity is already running
     */
    static boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getConsumerList().clear();
            if (getConsumerList() != null) {
                jsonManager.setUpConsumers();
            }


        setContentView(R.layout.activity_allergen_detector);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.allergenDetector);

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(consumerSwipeController);
        itemTouchhelper.attachToRecyclerView(findViewById(R.id.consumerRecycleView));
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

    }

    /**
     * Allow the user to create a new Consumer by loading a new layout.
     */
    @OnClick(R.id.consumerFAB)
    @Override
    public void onFABClick(View view) {
        //TODO create a new layout to add Consumer
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    /**
     * Allow the user to edit a Consumer by swiping item to the right.
     */
    @Override
    public void onConsumerItemSwipeRight() {
        //TODO create a new layout to edit Consumer
    }

    /**
     * Allow the user to delete a Consumer by swiping item to the left.
     */
    @Override
    public void onConsumerItemSwipeLeft() {


        //TODO Figure out how to get the item from the recyclerView
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }
}
