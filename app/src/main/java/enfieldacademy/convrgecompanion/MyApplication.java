package enfieldacademy.convrgecompanion;

import android.app.Application;
import android.util.Log;

// http://stackoverflow.com/questions/18038399/how-to-check-if-activity-is-in-foreground-or-in-visible-background
public class MyApplication extends Application {

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static boolean isServiceRunning(){
        return serviceRunning;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    public static void serviceRunning() {
        Log.d(TAG, "serviceRunning()");
        serviceRunning = true;
    }

    public static void servicePaused() {
        Log.d(TAG, "servicePaused()");
        serviceRunning = false;
    }

    private static final String TAG = "ConVRgeApplication";
    private static boolean activityVisible;
    private static boolean serviceRunning;

}
