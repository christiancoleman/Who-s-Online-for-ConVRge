package info.christiancoleman.whosonlineforconvrge;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Looper;
import android.os.Handler;
import android.os.Message;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.Nullable;
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

// Reference: http://developer.android.com/guide/components/services.html
public class WhosOnlineService extends Service {

    private final String TAG = "WhosOnlineService";

    public ConVRgeServer mOldServerObject;
    public ConVRgeServer mServerObject;
    public String mResultString;
    public JSONObject mConVRgeMainJSONObject = null;
    public JSONArray mPlayersOnlineJSONArray = null;

    private ServiceHandler mServiceHandler;

    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            /////////////////////////////////
            // INSTANTIATES SERVER OBJECT ///
            ////////////////////////////////
            createServerObjects();

            // After we are done doing our work
            // We just sleep for 5 seconds.
            while(WhosOnlineApplication.isServiceStarted()){
                //Log.d(TAG, "=============================================================");
                queryServer();
                parseResult();
                buildConVRgeServer();
                printResults();
                updateUIAndCreateNotifications();
                sleep(WhosOnlineHelper.PAUSE_DURATION);
            }

            Log.d(TAG, "Do we make it here?");
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate() called");
        super.onCreate();

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_FOREGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        Looper mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() called");

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() called");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind() called");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()1 called");
        super.onDestroy();
        Log.d(TAG, "onDestroy()2 called");
        WhosOnlineHelper.clearNotifications(this);
        stopSelf();
    }

    public void createServerObjects(){
        mOldServerObject = new ConVRgeServer();
        mServerObject = new ConVRgeServer();
    }

    public void queryServer(){
        URL url = null;
        HttpURLConnection connection = null;
        InputStream in;
        StringBuilder total = new StringBuilder();
        String line;

        try {
            url = new URL(WhosOnlineHelper.ENDPOINT);
        } catch (MalformedURLException e){
            // TODO: error handling
            //Log.d(TAG, "queryServer1");
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
            //Log.d(TAG, "queryServer2");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        mResultString = total.toString();

        //Log.d(TAG, mResultString);
    }

    public void parseResult(){
        mConVRgeMainJSONObject = null;
        mPlayersOnlineJSONArray = null;

        try {
            mConVRgeMainJSONObject = new JSONObject(mResultString);
            mPlayersOnlineJSONArray = mConVRgeMainJSONObject.getJSONArray("playersOnline");
        } catch (JSONException e){
            // TODO: error handling
            //Log.d(TAG, "parseResult1");
        }
    }

    public void buildConVRgeServer(){
        if(mConVRgeMainJSONObject == null || mPlayersOnlineJSONArray == null) {
            // TODO: error handling
            //Log.d(TAG, "buildConVRgeServer1");
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
                //Log.d(TAG, "buildConVRgeServer2");
            }
        }
        mServerObject.setNumUsersOnline(counter);
        mServerObject.setOnlineUsersList(temp);

        try {
            mServerObject.setNumUsersWatching(mConVRgeMainJSONObject.getInt("playersWatching"));
        } catch (JSONException e){
            // TODO: error handling
            //Log.d(TAG, "buildConVRgeServer3");
        }
    }

    public void printResults(){
        //mServerObject.print();
    }

    public void updateUIAndCreateNotifications(){
        updatePersistentNotification();
        if(WhosOnlineApplication.isActivityVisible()) {
            updateUI();
        } else {
            createNewPlayerOnlineNotifications();
            updateOldServerObject();
        }
    }

    public void updateUI(){
        //Log.d(TAG, "updateUI() called");
        ConVRgeUIThread newUIThread = new ConVRgeUIThread();
        newUIThread.start();
    }

    public void updatePersistentNotification(){
        //Log.d(TAG, "createOrUpdatePersistentNotification() STARTED");

        if(mServerObject.getNumUsersOnline() == 0) WhosOnlineHelper.clearIndividualPlayerNotifications(this);

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
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(String.format(getResources().getString(R.string.persistent_notification_title), mServerObject.getNumUsersOnline()))
                        .setContentText(String.format(getResources().getString(R.string.persistent_notification_subtext), allPlayersOnlineString))
                        .setColor(Color.BLACK)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(false)
                .build();

        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        //////////////////////////////////////////////////
        // KEEPS SERVICE TRULY PERSISTENT ////////////////
        //////////////////////////////////////////////////
        startForeground(WhosOnlineHelper.NOTIFICATION_ID_ALL_PLAYERS_PERSISTENT, notification);

        //Log.d(TAG, "createOrUpdateStaticNotification() ENDED (Notification made/updated!)");
    }

    public void createNewPlayerOnlineNotifications(){
        //Log.d(TAG, "createNewPlayerOnlineNotifications() STARTED");

        if(!WhosOnlineApplication.areNotificationsOn()) {
            WhosOnlineHelper.clearIndividualPlayerNotifications(this);
            return;
        }

        String newPlayersString = "";
        ArrayList<ConVRgePlayer> oldPlayersList = mOldServerObject.getOnlineUsersList();
        ArrayList<ConVRgePlayer> newPlayersList = mServerObject.getOnlineUsersList();

        //Log.d(TAG, "newPlayersString = _" + newPlayersString + "_");
        //Log.d(TAG, "oldPlayerList.size() = " + oldPlayersList.size());
        //Log.d(TAG, "newPlayersList.size() = " + newPlayersList.size());

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
            //Log.d(TAG, "createNewPlayerOnlineNotifications() ENDED (Premature) - i.e. No new players online.");
            return;
        }

        Context c = getApplicationContext();
        Intent targetIntent = new Intent(c, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(WhosOnlineApplication.areNotificationSoundsOn()) {
            MediaPlayer mp = MediaPlayer.create(this, R.raw.player_joined_sound);
            mp.start();
        }

        Notification notification = new NotificationCompat.Builder(c)
                .setContentTitle(getResources().getString(R.string.player_notification_title))
                .setContentText(newPlayersString)
                .setTicker(String.format(getResources().getString(R.string.player_notification_subtext), newPlayersString))
                        .setSmallIcon(R.mipmap.friend_online)
                        .setColor(Color.BLACK)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(WhosOnlineHelper.NOTIFICATION_ID_INDIVIDUAL_PLAYERS, notification);
        //Log.d(TAG, "createNewPlayerOnlineNotifications() ENDED (Notification made!)");
    }

    public void updateOldServerObject(){
        //Log.d(TAG, "updateOldServerObject()");
        mOldServerObject = new ConVRgeServer(mServerObject);
    }

    public void sleep(int duration){
        //Log.d(TAG, "=============================================================");
        SystemClock.sleep(duration);
        Log.d(TAG, WhosOnlineHelper.PAUSE_DURATION / 1000 + " seconds has passed"); // 1000 is # ms = 1 second
    }

    public class ConVRgeUIThread extends Thread{

        @Override
        public void run() {
            //Log.d(TAG, "This is being called at least.");
            Intent intent = new Intent();
            intent.setAction("com.info.info.christiancoleman.CUSTOM_INTENT");
            intent.putExtra("USERS_ONLINE", mServerObject.getNumUsersOnline());
            intent.putExtra("USERS_WATCHING", mServerObject.getNumUsersWatching());
            for(int i = 0 ; i < mServerObject.getNumUsersOnline(); i++){
                intent.putExtra(i + "-id", mServerObject.getOnlineUsersList().get(i).getId());
                intent.putExtra(i + "-name", mServerObject.getOnlineUsersList().get(i).getPlayerName());
            }
            sendBroadcast(intent);
        }
    }
}
