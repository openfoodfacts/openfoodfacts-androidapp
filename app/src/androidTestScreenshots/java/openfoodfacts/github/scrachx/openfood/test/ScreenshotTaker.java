package openfoodfacts.github.scrachx.openfood.test;

import android.app.Activity;
import android.util.Log;
import tools.fastlane.screengrab.FalconScreenshotStrategy;
import tools.fastlane.screengrab.Screengrab;

/**
 * Take screenshots...
 */
public class ScreenshotTaker {
    private static final String LOG_TAG = ScreenshotTaker.class.getSimpleName();
    private FileWritingScreenshotCallback callback = new FileWritingScreenshotCallback();

    public void takeScreenshot(ScreenshotParameter screenshotParameter, String suffix, ScreenshotActivityTestRule<? extends Activity> activityRule) {
        final String screenshotName = activityRule.getName()+ suffix;
        callback.setScreenshotParameter(screenshotParameter);
        Log.d(LOG_TAG, "Start screenshots for screenshotParameter " + screenshotParameter + " for activity " + screenshotName);
        Screengrab.screenshot(screenshotName, new FalconScreenshotStrategy(activityRule.getActivity()), callback);
    }

}
