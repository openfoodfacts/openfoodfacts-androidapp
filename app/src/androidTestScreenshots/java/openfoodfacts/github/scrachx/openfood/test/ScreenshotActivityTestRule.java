package openfoodfacts.github.scrachx.openfood.test;

import android.app.Activity;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

public class ScreenshotActivityTestRule<T extends Activity> extends ActivityTestRule<T> {
    String name;
    private Intent activityIntent;
    private ScreenshotParameter screenshotParameter;
    Consumer<ScreenshotActivityTestRule> action;

    public ScreenshotActivityTestRule(Class<T> activityClass) {
        this(activityClass, activityClass.getSimpleName(), null);
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

    public void setAction(Consumer<ScreenshotActivityTestRule> action) {
        this.action = action;
    }

    public void setActivityIntent(Intent activityIntent) {
        this.activityIntent = activityIntent;
    }

    @Override
    protected void afterActivityLaunched() {
        try {
            Thread.sleep(5000);
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            if (action != null) {
                action.accept(this);
            } else {
                takeScreenshot();
            }
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
