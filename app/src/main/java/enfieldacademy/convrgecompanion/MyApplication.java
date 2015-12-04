package enfieldacademy.convrgecompanion;

import android.app.Application;
import android.util.Log;

// http://stackoverflow.com/questions/18038399/how-to-check-if-activity-is-in-foreground-or-in-visible-background
public class MyApplication extends Application {

    private static final String TAG = "ConVRgeApplication";
    private static boolean activityVisible;
    private static boolean serviceStarted;

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    public static boolean isServiceStarted() {
        return serviceStarted;
    }

    public static void serviceStarted(){
        serviceStarted = true;
    }

    public static void serviceEnded(){
        serviceStarted = false;
    }

}
