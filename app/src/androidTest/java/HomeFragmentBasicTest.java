import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.HomeFragment;
import openfoodfacts.github.scrachx.openfood.views.FragmentTestActivity;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import openfoodfacts.github.scrachx.openfood.views.ScannerFragmentActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by samagra on 22/3/18.
 */
@RunWith(AndroidJUnit4.class)
public class HomeFragmentBasicTest {
    @Rule
    public ActivityTestRule<FragmentTestActivity> mActivityTestRule
            = new ActivityTestRule<>(FragmentTestActivity.class);
    @Rule public IntentsTestRule<MainActivity> mIntentsTestRule =
            new IntentsTestRule<>(MainActivity.class);

    @Before
    public void setup(){
        mActivityTestRule.getActivity().setFragment(new HomeFragment());
    }

    @Test
    public void clickScanFabButton_OpensScanFragment(){
        onView(withId(R.id.buttonScan)).perform(click());
        intended(hasComponent(ScannerFragmentActivity.class.getName()));
    }
}
