package openfoodfacts.github.scrachx.openfood.views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import openfoodfacts.github.scrachx.openfood.R;

/**
 * DummyTestActivity is a class created only for test purposes.
 * Used at RatingFragmentTest.java
 */
public class DummyTestActivity extends AppCompatActivity{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dummy_test);
    }
}
