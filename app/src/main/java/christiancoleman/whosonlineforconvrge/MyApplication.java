package christiancoleman.whosonlineforconvrge;

import android.app.Application;

// http://stackoverflow.com/questions/18038399/how-to-check-if-activity-is-in-foreground-or-in-visible-background
public class MyApplication extends Application {

    private static boolean activityVisible;
    private static boolean serviceStarted;
    private static boolean notificationsOn;
    private static boolean notificationSoundsOn;
    private static ConVRgeServer savedServer;

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

    public static boolean areNotificationsOn(){
        return notificationsOn;
    }

    public static void notificationsOn() {
        notificationsOn = true;
    }

    public static void notificationsOff(){
        notificationsOn = false;
    }

    public static boolean areNotificationSoundsOn(){
        return notificationSoundsOn;
    }

    public static void notificationSoundsOn(){
        notificationSoundsOn = true;
    }

    public static void notificationSoundsOff(){
        notificationSoundsOn = false;
    }

    public static ConVRgeServer getSavedServer(){
        return savedServer;
    }

    public static void setSavedServer(ConVRgeServer server){
        savedServer = new ConVRgeServer(server);
    }

}
