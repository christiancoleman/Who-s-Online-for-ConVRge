package christiancoleman.whosonlineforconvrge;

import android.app.NotificationManager;
import android.content.Context;

/**
 * This class will hold constants and static methods used throughout the app.
 */
public class ConVRgeHelper {

    public static final String ENDPOINT = "http://www.convrge.co/api/users?watching=true";
    public static final int NOTIFICATION_ID_ALL_PLAYERS_PERSISTENT = 31337;
    public static final int NOTIFICATION_ID_INDIVIDUAL_PLAYERS = 31338;
    public static final int PAUSE_DURATION = 5000;

    public static void clearNotifications(Context context){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID_ALL_PLAYERS_PERSISTENT);
        notificationManager.cancel(NOTIFICATION_ID_INDIVIDUAL_PLAYERS);
    }

    public static void clearIndividualPlayerNotifications(Context context){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID_INDIVIDUAL_PLAYERS);
    }

}
