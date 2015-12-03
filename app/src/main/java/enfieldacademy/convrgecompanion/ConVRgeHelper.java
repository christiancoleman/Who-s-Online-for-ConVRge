package enfieldacademy.convrgecompanion;

import android.app.NotificationManager;
import android.content.Context;

/**
 * This class will hold constants and static methods used throughout the app.
 */
public class ConVRgeHelper {

    public static final String ENDPOINT = "http://www.convrge.co/api/users?watching=true";
    public static final int NOTIFICATION_ID_STATIC = 31337;
    public static final int NOTIFICATION_ID_DYNAMIC = 31338;

    public static void clearNotifications(Context context){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID_DYNAMIC);
        notificationManager.cancel(NOTIFICATION_ID_STATIC);
    }

}
