package openfoodfacts.github.scrachx.openfood.views;

import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import holloway.allergenChecker.Consumer;
import holloway.allergenChecker.JSONManager;
import openfoodfacts.github.scrachx.openfood.FragmentConsumerEdit;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.ConsumerFragment;
import openfoodfacts.github.scrachx.openfood.utils.SwipeController;
import openfoodfacts.github.scrachx.openfood.views.adapters.ConsumerRecyclerViewAdapter;

public class AllergenDetectorActivity extends BaseActivity implements ConsumerFragment.OnListFragmentInteractionListener, FragmentConsumerEdit.OnFragmentInteractionListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Nullable
    @BindView(R.id.consumerFAB)
    FloatingActionButton consumerFAB;

    @BindView(R.id.consumerRecycleView)
    RecyclerView recyclerConsumerScanView;

    @BindView(R.id.textView2)
    TextView allergenWarningTextView;

    private ConsumerRecyclerViewAdapter consumerAdapter;
    SwipeController consumerSwipeController = new SwipeController(this, position -> {
        consumerAdapter.removeAt(position);
        consumerAdapter.notifyItemRemoved(position);
        consumerAdapter.notifyItemRangeChanged(position, consumerAdapter.getItemCount());
        consumerAdapter.notifyDataSetChanged();
        recyclerConsumerScanView.refreshDrawableState();

        //TODO delete action
        Toast toast = Toast.makeText(getBaseContext(), "Delete button tapped", Toast.LENGTH_SHORT);
        toast.show();
    });
    private ConsumerFragment.OnListFragmentInteractionListener consumerFragmentListener;

    ItemTouchHelper itemTouchhelper = new ItemTouchHelper(consumerSwipeController);

    private JSONManager jsonManager = JSONManager.getInstance();

    public HashSet<Consumer> getConsumerList() {
        return jsonManager.getConsumers();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* taken from http://stackoverflow.com/a/18296943#0#L0 */
        FragmentManager fragMan = getSupportFragmentManager();
        FragmentTransaction fragTransaction = fragMan.beginTransaction();

        Fragment myFrag = ConsumerFragment.newInstance(1);
        fragTransaction.add(R.id.allergenDetectorFragment, myFrag, "consumerFragment");
        fragTransaction.commit();

        getConsumerList().clear(); //Prevents duplication of Consumers
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

        consumerAdapter = new ConsumerRecyclerViewAdapter(getConsumerList(), consumerFragmentListener);
    }


    /**
     * Allows the user to create a serializable {@link Consumer}.
     * The method shall replace the fragment with {@link FragmentConsumerEdit}.
     * Called when the {@link FloatingActionButton} is clicked.
     */
    @OnClick(R.id.consumerFAB)
    @Override
    public void onFABClick() {
        replaceFragment(FragmentConsumerEdit.newInstance());
        recyclerConsumerScanView.setVisibility(View.GONE);
        assert consumerFAB != null;
        consumerFAB.setVisibility(View.GONE);
        allergenWarningTextView.setVisibility(View.GONE);
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

    /* taken from http://stackoverflow.com/a/18306258#0#L0 */
    private void replaceFragment(Fragment fragment) {
        String backStateName = fragment.getClass().getName();

        FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        if (!fragmentPopped) { //fragment not in back stack, create it.
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.allergenDetectorFragment, fragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
    }
}
