package openfoodfacts.github.scrachx.openfood.test;

import android.app.Activity;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;
import junit.framework.Assert;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.PrefManager;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

public class ScreenshotActivityTestRule<T extends Activity> extends ActivityTestRule<T> {
    public static final int MILLIS_TO_WAIT_TO_DISPLAY_ACTIVITY = 5000;
    String name;
    private Intent activityIntent;
    private ScreenshotParameter screenshotParameter;
    Consumer<ScreenshotActivityTestRule> afterActivityLaunchedAction;
    Consumer<ScreenshotActivityTestRule> beforeActivityStartedAction;

    public ScreenshotActivityTestRule(Class<T> activityClass) {
        this(activityClass, activityClass.getSimpleName(), null);
    }

    public void setBeforeActivityStartedAction(Consumer<ScreenshotActivityTestRule> beforeActivityStartedAction) {
        this.beforeActivityStartedAction = beforeActivityStartedAction;
    }

    public ScreenshotParameter getScreenshotParameter() {
        return screenshotParameter;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setScreenshotParameter(ScreenshotParameter screenshotParameter) {
        this.screenshotParameter = screenshotParameter;
    }

    public void setAfterActivityLaunchedAction(Consumer<ScreenshotActivityTestRule> afterActivityLaunchedAction) {
        this.afterActivityLaunchedAction = afterActivityLaunchedAction;
    }

    public void setActivityIntent(Intent activityIntent) {
        this.activityIntent = activityIntent;
    }

    private boolean firstTimeLaunched = false;

    public void setFirstTimeLaunched(boolean firstTimeLaunched) {
        this.firstTimeLaunched = firstTimeLaunched;
    }

    @Override
    protected void beforeActivityLaunched() {
        try {
            runOnUiThread(() -> {
                new PrefManager(OFFApplication.getInstance()).setFirstTimeLaunch(firstTimeLaunched);
                LocaleHelper.setLocale(InstrumentationRegistry.getInstrumentation().getTargetContext(),screenshotParameter.getLocale());
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
                Thread.sleep(MILLIS_TO_WAIT_TO_DISPLAY_ACTIVITY);
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            }
            else{
                Thread.sleep(MILLIS_TO_WAIT_TO_DISPLAY_ACTIVITY);
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            }
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

    public String getName() {
        return name;
    }
}
