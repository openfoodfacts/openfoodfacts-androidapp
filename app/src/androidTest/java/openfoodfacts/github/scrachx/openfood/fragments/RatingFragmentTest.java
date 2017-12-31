package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.views.DummyTestActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

/**
 * Test cases for the RatingFragment.java.
 */
public class RatingFragmentTest {
    @Rule
    public ActivityTestRule<DummyTestActivity> mActivityTestRule =
            new ActivityTestRule<DummyTestActivity>(DummyTestActivity.class);

    private DummyTestActivity mActivity = null;
    private RatingFragment mFragment = null;

    private OpenFoodAPIClient api;
    private CountDownLatch latch;

    private RelativeLayout container;
    private Button saveButton;
    private EditText commentEditText;
    private LinearLayout editDeleteLinearLayout;
    private RatingBar ratingBar;
    private TextView noteTextView;

    /**
     * setUp contains all the initializations required by all test cases
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception{
        mActivity = mActivityTestRule.getActivity();
        container = (RelativeLayout) mActivity.findViewById(R.id.dummyRelativeLayout);
        api = new OpenFoodAPIClient(mActivity);

        latch = new CountDownLatch(1);

        api.getAPIService().getProductByBarcode("7622210090928").enqueue(new Callback<State>() {
            @Override
            public void onResponse(Call<State> call, Response<State> response) {
                final State s = response.body();
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("state", s);
                intent.putExtras(bundle);
                mActivity.setIntent(intent);
                latch.countDown();
            }

            @Override
            public void onFailure(Call<State> call, Throwable t) {
                return;
            }
        });

        /* Wait the callback's response */
        latch.await(2000, TimeUnit.MILLISECONDS);

        mFragment = new RatingFragment();
        mActivity.getSupportFragmentManager().beginTransaction()
                .add(container.getId(), mFragment).commitAllowingStateLoss();

        /* Wait the fragment to be opened */
        getInstrumentation().waitForIdleSync();

        ratingBar = (RatingBar) mFragment.getView().findViewById(R.id.ratingRatingBar);
        saveButton = (Button) mFragment.getView().findViewById(R.id.ratingSaveButton);
        commentEditText = (EditText) mFragment.getView().findViewById(R.id.ratingCommentEditText);
        editDeleteLinearLayout = (LinearLayout) mFragment.getView().findViewById(R.id.ratingEditDeleteLinearLayout);
        noteTextView = (TextView) mFragment.getView().findViewById(R.id.ratingNoteTextView);
    }

    /**
     * Test case: ensure the fragment launches with no errors
     */
    @Test
    public void RatingFragment_LaunchTest(){
        assertNotNull(mActivity);
    }

    /**
     * Test case: ensure that in case there hasn't been set stars at the rating bar,
     * each view component will be at the corresponding state.
     * Most importantly that the save button will be disable.
     *
     * At the beginning of the method there is an if statement,
     * in order to ensure that if there was already a rating saved
     * the method will delete it, in order to get to the state that
     * we want to test in this particular test case.
     */
    @Test
    public void RatingFragment_ButtonsStateWithoutRatingTest(){
        if(ratingBar.getRating() != 0 && saveButton.getVisibility() == View.GONE)
        {
            onView(withId(R.id.ratingDeleteButton))
                    .perform(click());
            onView(withText(R.string.txtYes))
                    .perform(click());
        }

        assertEquals(saveButton.getVisibility(), View.VISIBLE);
        assertFalse(saveButton.isEnabled());
        assertEquals(editDeleteLinearLayout.getVisibility(), View.GONE);
        assertEquals(noteTextView.getVisibility(), View.VISIBLE);
        assertTrue(commentEditText.isEnabled());
        assertFalse(ratingBar.isIndicator());
    }

    /**
     * Test case: ensure that whenever there has been stars set at the rating bar,
     * each view component will be at the corresponding state.
     * In our test case example we set the stars to 4.
     *
     * At the beginning of the method there is an if statement,
     * in order to ensure that if there was already a rating saved
     * the method will delete it, in order to get to the state that
     * we want to test in this particular test case.
     */
    @Test
    public void RatingFragment_ButtonsStateWithRatingTest(){
        if(ratingBar.getRating() != 0 && saveButton.getVisibility() == View.GONE){
            onView(withId(R.id.ratingDeleteButton))
                    .perform(click());
            onView(withText(R.string.txtYes))
                    .perform(click());
        }

        ratingBar.setRating(4);
        assertEquals((int)ratingBar.getRating(), 4);
        assertEquals(saveButton.getVisibility(), View.VISIBLE);
        assertEquals(editDeleteLinearLayout.getVisibility(), View.GONE);
        assertEquals(noteTextView.getVisibility(), View.VISIBLE);
        assertTrue(commentEditText.isEnabled());
        assertFalse(ratingBar.isIndicator());
    }

    /**
     * Test case: ensure that whenever the user press the enabled save button,
     * each view component will be at the corresponding state.
     * Most importantly, the save button should now become "gone",
     * whereas the edit and delete buttons should become "visible".
     * In the same way, the rating bar should become an indicator
     * and the edit text view should be disable, in order to avoid any further confusion.
     *
     * At the beginning of the method there is an if statement,
     * in order to ensure that if there was already a rating saved
     * the method will delete it, in order to get to the state that
     * we want to test in this particular test case.
     */
    @Test
    public void RatingFragment_ButtonsStateAfterSaveOnClickTest(){
        if(ratingBar.getRating() != 0 && saveButton.getVisibility() == View.GONE){
            onView(withId(R.id.ratingDeleteButton))
                    .perform(click());
            onView(withText(R.string.txtYes))
                    .perform(click());
        }

        ratingBar.setRating(4);
        assertEquals((int)ratingBar.getRating(), 4);

        onView(withId(R.id.ratingSaveButton))
                .perform(click());

        assertEquals(saveButton.getVisibility(), View.GONE);
        assertEquals(editDeleteLinearLayout.getVisibility(), View.VISIBLE);
        assertEquals(noteTextView.getVisibility(), View.GONE);
        assertFalse(commentEditText.isEnabled());
        assertTrue(ratingBar.isIndicator());
    }

    /**
     * Test case: ensure that whenever the user press the enabled delete button,
     * each view component will be at the corresponding state.
     * Most importantly, the save button should now become "visible" once again,
     * whereas the edit and delete buttons should become "gone".
     * In the same way, the rating bar should become editable again
     * and the edit text view should become editable as well.
     *
     * At the beginning of the method there is an if statement,
     * in order to ensure that if there hasn't been saved any rating yet
     * the method will create one, in order to get to the state that
     * we want to test in this particular test case.
     */
    @Test
    public void RatingFragment_ButtonsStateAfterDeleteOnClickTest(){
        if(ratingBar.getRating() == 0 && saveButton.getVisibility() == View.VISIBLE){
            ratingBar.setRating(4);
            onView(withId(R.id.ratingSaveButton))
                    .perform(click());
        }

        onView(withId(R.id.ratingDeleteButton))
                .perform(click());
        onView(withText(R.string.txtYes))
                .perform(click());

        assertEquals((int)ratingBar.getRating(), 0);
        assertEquals(saveButton.getVisibility(), View.VISIBLE);
        assertFalse(saveButton.isEnabled());
        assertEquals(editDeleteLinearLayout.getVisibility(), View.GONE);
        assertEquals(noteTextView.getVisibility(), View.VISIBLE);
        assertTrue(commentEditText.isEnabled());
        assertFalse(ratingBar.isIndicator());
    }

    /**
     * Test case: ensure that whenever the user press the enabled edit button,
     * each view component will be at the corresponding state.
     * Most importantly, the save button should now become "visible" once again,
     * whereas the edit and delete buttons should become "gone".
     * In the same way, the rating bar should become editable again
     * and the edit text view should become editable as well.
     *
     * At the beginning of the method there is an if statement,
     * in order to ensure that if there hasn't been saved any rating yet
     * the method will create one, in order to get to the state that
     * we want to test in this particular test case.
     */
    @Test
    public void RatingFragment_ButtonsStateAfterEditOnClickTest(){
        if(ratingBar.getRating() == 0 && saveButton.getVisibility() == View.VISIBLE){
            ratingBar.setRating(4);
            onView(withId(R.id.ratingSaveButton))
                    .perform(click());
        }

        onView(withId(R.id.ratingEditButton))
                .perform(click());

        assertEquals((int)ratingBar.getRating(), 4);
        assertEquals(saveButton.getVisibility(), View.VISIBLE);
        assertEquals(editDeleteLinearLayout.getVisibility(), View.GONE);
        assertEquals(noteTextView.getVisibility(), View.VISIBLE);
        assertTrue(commentEditText.isEnabled());
        assertFalse(ratingBar.isIndicator());
    }

    /**
     * tearDown method is going to be executed aster all test cases has been done.
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception{
        mActivity = null;
    }
}
