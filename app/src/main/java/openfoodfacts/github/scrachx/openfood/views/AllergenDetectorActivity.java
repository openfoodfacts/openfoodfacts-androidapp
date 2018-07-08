package openfoodfacts.github.scrachx.openfood.views;

import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import holloway.allergenChecker.Consumer;
import holloway.allergenChecker.JSONManager;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.ConsumerFragment;
import openfoodfacts.github.scrachx.openfood.utils.SwipeController;

public class AllergenDetectorActivity extends BaseActivity implements ConsumerFragment.OnListFragmentInteractionListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Nullable
    @BindView(R.id.consumerFAB)
    FloatingActionButton consumerFAB;

    @BindView(R.id.consumerRecycleView)
    RecyclerView recyclerConsumerScanView;

    SwipeController consumerSwipeController = new SwipeController(this, position -> {
        //TODO Delete consumer logic
        Toast toast = Toast.makeText(getBaseContext(), "Delete button tapped", Toast.LENGTH_SHORT);
        toast.show();
    });
    ItemTouchHelper itemTouchhelper = new ItemTouchHelper(consumerSwipeController);

    private JSONManager jsonManager = JSONManager.getInstance();

    public HashSet<Consumer> getConsumerList() {
        return jsonManager.getConsumers();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Prevents duplication of Consumers
        getConsumerList().clear();
            if (getConsumerList() != null) {
                jsonManager.setUpConsumers();
            }


        setContentView(R.layout.activity_allergen_detector);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.allergenDetector);


        itemTouchhelper.attachToRecyclerView(recyclerConsumerScanView);
        recyclerConsumerScanView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                consumerSwipeController.onDraw(c);
            }
        });

    }


    /**
     * Allow the user to create a new Consumer by loading a new layout.
     */
    @OnClick(R.id.consumerFAB)
    @Override
    public void onFABClick(View view) {
        //TODO Consumer setup layout
        Toast toast = Toast.makeText(this, "Add Consumer", Toast.LENGTH_SHORT);
        toast.show();
    }


    /**
     * Allow the user to edit a Consumer when tapped.
     */
    @Override
    public void onConsumerItemTouch(View view, int position) {
        //TODO Consumer setup layout
        Toast toast = Toast.makeText(this, "Consumer tapped", Toast.LENGTH_SHORT);
        toast.show();
    }

}
