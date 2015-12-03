package enfieldacademy.convrgecompanion;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.SystemClock;
import android.util.Log;

import android.support.v4.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ConVRgeCompanionService extends IntentService{

    private final String TAG = "ConVRgeCompanionService";
    private final String ENDPOINT = "http://www.convrge.co/api/users";

    public static final int NOTIFICATION_ID_STATIC = 31337;
    public static final int NOTIFICATION_ID_DYNAMIC = 31338;

    // TODO: This is a hack - fix this
    public static boolean SERVICE_STOPPED = false;

    public int mPauseDuration = 5000;
    public ConVRgeServer mOldServerObject;
    public ConVRgeServer mServerObject;
    public String mResultString;
    public JSONObject mConVRgeMainJSONObject = null;
    public JSONArray mPlayersOnlineJSONArray = null;

    public ConVRgeCompanionService(String name) {
        super(name);
    }

    public ConVRgeCompanionService(){
        super("ConVRgeCompanionService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /////////////////////////////////
        // INSTANTIATES SERVER OBJECT ///
        ////////////////////////////////
        createServerObjects();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    protected void onHandleIntent(Intent workIntent){
        //noinspection InfiniteLoopStatement
        while(true){
            Log.d(TAG, "=============================================================");
            MyApplication.serviceRunning();
            queryServer();
            parseResult();
            buildConVRgeServer();
            printResults();
            updateUIAndCreateNotifications();
            sleep(mPauseDuration);
        }
    }

    public void sleep(int duration){
        Log.d(TAG, "=============================================================");
        SystemClock.sleep(duration);
        Log.d(TAG, mPauseDuration / 1000 + " seconds has passed"); // 1000 is # ms = 1 second
    }

    @Override
    public boolean onUnbind(Intent intent) {
        MyApplication.servicePaused();
        return super.onUnbind(intent);
    }

    public void createServerObjects(){
        mOldServerObject = new ConVRgeServer();
        mServerObject = new ConVRgeServer();
    }

    public void queryServer(){
        URL url = null;
        HttpURLConnection connection;
        InputStream in;
        StringBuilder total = new StringBuilder();
        String line;

        try {
            url = new URL(ENDPOINT);
        } catch (MalformedURLException e){
            // TODO: error handling
            Log.d(TAG, "queryServer1");
        }

        if(url == null) return;

        try {
            connection = (HttpURLConnection) url.openConnection();
            in = connection.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e){
            // TODO: error handling
            Log.d(TAG, "queryServer2");
        }

        mResultString = total.toString();

        Log.d(TAG, mResultString);
    }

    public void parseResult(){
        mConVRgeMainJSONObject = null;
        mPlayersOnlineJSONArray = null;

        try {
            mConVRgeMainJSONObject = new JSONObject(mResultString);
            mPlayersOnlineJSONArray = mConVRgeMainJSONObject.getJSONArray("playersOnline");
        } catch (JSONException e){
            // TODO: error handling
            Log.d(TAG, "parseResult1");
        }
    }

    public void buildConVRgeServer(){
        if(mConVRgeMainJSONObject == null || mPlayersOnlineJSONArray == null) {
            // TODO: error handling
            Log.d(TAG, "buildConVRgeServer1");
            return;
        }

        int counter = 0;
        ArrayList<ConVRgePlayer> temp = new ArrayList<>();
        for(; counter < mPlayersOnlineJSONArray.length(); counter++){
            try {
                JSONObject playerJSONObject = mPlayersOnlineJSONArray.getJSONObject(counter);
                int id = playerJSONObject.getInt("id");
                String playerName = playerJSONObject.getString("name");
                ConVRgePlayer newConVRgePlayer = new ConVRgePlayer(id, playerName);
                temp.add(newConVRgePlayer);
            } catch (JSONException e){
                // TODO: error handling
                Log.d(TAG, "buildConVRgeServer2");
            }
        }
        mServerObject.setNumUsersOnline(counter);
        mServerObject.setOnlineUsersList(temp);

        try {
            mServerObject.setNumUsersWatching(mConVRgeMainJSONObject.getInt("playersWatching"));
        } catch (JSONException e){
            // TODO: error handling
            Log.d(TAG, "buildConVRgeServer3");
        }
    }

    public void printResults(){
        mServerObject.print();
    }

    public void updateUIAndCreateNotifications(){
        createOrUpdateStaticNotification();
        if(MyApplication.isActivityVisible()) {
            updateUI();
        } else {
            createNewPlayerOnlineNotifications();
            updateOldServerObject();
        }
        // TODO: This is a hack - fix this
        if(SERVICE_STOPPED) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(ConVRgeCompanionService.NOTIFICATION_ID_DYNAMIC);
            notificationManager.cancel(ConVRgeCompanionService.NOTIFICATION_ID_STATIC);
        }
    }

    public void updateUI(){
        Log.d(TAG, "updateUI() called");
        ConVRgeUIThread newUIThread = new ConVRgeUIThread();
        newUIThread.start();
    }

    public void createOrUpdateStaticNotification(){
        Log.d(TAG, "createOrUpdateStaticNotification() STARTED");

        ArrayList<ConVRgePlayer> playerList = mServerObject.getOnlineUsersList();
        String allPlayersOnlineString = "";
        for(ConVRgePlayer player : playerList){
            if(allPlayersOnlineString.equals("")) allPlayersOnlineString = player.getPlayerName();
            else allPlayersOnlineString += ", " + player.getPlayerName();
        }

        Context c = getApplicationContext();
        Intent targetIntent = new Intent(c, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(c)
                .setContentTitle("ConVRge - " + mServerObject.getNumUsersOnline() + " players online")
                .setContentText(allPlayersOnlineString)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(Color.BLACK)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID_STATIC, notification);
        Log.d(TAG, "createOrUpdateStaticNotification() ENDED (Notification made/updated!)");
    }

    public void createNewPlayerOnlineNotifications(){
        Log.d(TAG, "createNewPlayerOnlineNotifications() STARTED");

        String newPlayersString = "";
        ArrayList<ConVRgePlayer> oldPlayersList = mOldServerObject.getOnlineUsersList();
        ArrayList<ConVRgePlayer> newPlayersList = mServerObject.getOnlineUsersList();

        Log.d(TAG, "newPlayersString = _" + newPlayersString + "_");
        Log.d(TAG, "oldPlayerList.size() = " + oldPlayersList.size());
        Log.d(TAG, "newPlayersList.size() = " + newPlayersList.size());

        for(ConVRgePlayer newPlayer : newPlayersList){
            boolean found = false;
            for(ConVRgePlayer oldPlayer : oldPlayersList){
                if(oldPlayer.getPlayerName().equals(newPlayer.getPlayerName())) found = true;
            }
            if(!found) {
                if(newPlayersString.equals("")) newPlayersString = newPlayer.getPlayerName();
                else newPlayersString += ", " + newPlayer.getPlayerName();
            }
        }

        if(newPlayersString.equals("")) {
            Log.d(TAG, "createNewPlayerOnlineNotifications() ENDED (Premature) - i.e. No new players online.");
            return;
        }

        Context c = getApplicationContext();
        Intent targetIntent = new Intent(c, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(c)
                .setContentTitle("ConVRge - player online!")
                .setContentText(newPlayersString)
                .setTicker(newPlayersString + " online now!")
                .setSmallIcon(R.mipmap.friend_online)
                .setColor(Color.BLACK)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID_DYNAMIC, notification);
        Log.d(TAG, "createNewPlayerOnlineNotifications() ENDED (Notification made!)");
    }

    public void updateOldServerObject(){
        Log.d(TAG, "updateOldServerObject()");
        ArrayList<ConVRgePlayer> serverList = new ArrayList<>();
        for(int i = 0; i < mServerObject.getOnlineUsersList().size(); i++){
            ConVRgePlayer player = mServerObject.getOnlineUsersList().get(i);
            serverList.add(new ConVRgePlayer(player.getId(), player.getPlayerName()));
        }
        mOldServerObject.setOnlineUsersList(serverList);
        mOldServerObject.setNumUsersOnline(mServerObject.getNumUsersOnline());
        mOldServerObject.setNumUsersWatching(mServerObject.getNumUsersWatching());
    }

    public class ConVRgeUIThread extends Thread{

        private final String TAG = "ConVRgeUIThread";

        @Override
        public void run() {
            Log.d(TAG, "This is being called at least.");
            Intent intent = new Intent();
            intent.setAction("com.enfieldacademy.CUSTOM_INTENT");
            intent.putExtra("USERS_ONLINE", mServerObject.getNumUsersOnline());
            intent.putExtra("USERS_WATCHING", mServerObject.getNumUsersWatching());
            for(int i = 0 ; i < mServerObject.getNumUsersOnline(); i++){
                intent.putExtra(i + "-id", mServerObject.getOnlineUsersList().get(i).getId());
                intent.putExtra(i + "-name", mServerObject.getOnlineUsersList().get(i).getPlayerName());
            }
            sendBroadcast(intent);
            //stopSelf();
        }
    }
}
