package openfoodfacts.github.scrachx.openfood.test;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import java.util.function.Consumer;

import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.PrefManager;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

public class ScreenshotActivityTestRule<T extends Activity> extends ActivityTestRule<T> {
    public static final int MILLIS_TO_WAIT_TO_DISPLAY_ACTIVITY = 5000;
    private Intent activityIntent;
    private Consumer<ScreenshotActivityTestRule<T>> afterActivityLaunchedAction;
    private Consumer<ScreenshotActivityTestRule<T>> beforeActivityStartedAction;
    private boolean firstTimeLaunched = false;
    private String name;
    private ScreenshotParameter screenshotParameter;

    public ScreenshotActivityTestRule(Class<T> activityClass) {
        this(activityClass, activityClass.getSimpleName(), null);
    }

    public ScreenshotActivityTestRule(Class<T> activityClass, String name) {
        this(activityClass, name, null);
    }

    public ScreenshotActivityTestRule(Class<T> activityClass, Intent intent) {
        this(activityClass, activityClass.getSimpleName(), intent);
    }

    public ScreenshotActivityTestRule(Class<T> activityClass, String name, Intent intent) {
        super(activityClass, false, false);
        this.name = name;
        this.activityIntent = intent;
    }

    public void setBeforeActivityStartedAction(Consumer<ScreenshotActivityTestRule<T>> beforeActivityStartedAction) {
        this.beforeActivityStartedAction = beforeActivityStartedAction;
    }

    public ScreenshotParameter getScreenshotParameter() {
        return screenshotParameter;
    }

    public void setScreenshotParameter(ScreenshotParameter screenshotParameter) {
        this.screenshotParameter = screenshotParameter;
    }

    public void setAfterActivityLaunchedAction(Consumer<ScreenshotActivityTestRule<T>> afterActivityLaunchedAction) {
        this.afterActivityLaunchedAction = afterActivityLaunchedAction;
    }

    public void setFirstTimeLaunched(boolean firstTimeLaunched) {
        this.firstTimeLaunched = firstTimeLaunched;
    }

    @Override
    protected void beforeActivityLaunched() {
        try {
            runOnUiThread(() -> {
                new PrefManager(OFFApplication.getInstance()).setFirstTimeLaunch(firstTimeLaunched);
                LocaleHelper.setLocale(InstrumentationRegistry.getInstrumentation().getTargetContext(), screenshotParameter.getLocale());
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            Assert.fail(throwable.getMessage());
        }
        if (beforeActivityStartedAction != null) {
            beforeActivityStartedAction.accept(this);
        }
    }

    @Override
    protected void afterActivityLaunched() {
        try {
            if (afterActivityLaunchedAction != null) {
                afterActivityLaunchedAction.accept(this);
            }
            Thread.sleep(MILLIS_TO_WAIT_TO_DISPLAY_ACTIVITY);
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            takeScreenshot();
            this.finishActivity();
        } catch (Throwable throwable) {
            Log.e(ScreenshotActivityTestRule.class.getSimpleName(), "run on ui", throwable);
        }
    }

    public void takeScreenshot() {
        takeScreenshot(StringUtils.EMPTY);
    }

    public void takeScreenshot(String suffix) {
        ScreenshotTaker taker = new ScreenshotTaker();
        taker.takeScreenshot(screenshotParameter, suffix, this);
    }

    @Override
    protected Intent getActivityIntent() {
        return activityIntent;
    }

    public void setActivityIntent(Intent activityIntent) {
        this.activityIntent = activityIntent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
