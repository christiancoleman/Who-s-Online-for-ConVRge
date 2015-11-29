package enfieldacademy.convrgecompanion;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import java.util.HashSet;
import java.util.Objects;

public class ConVRgeCompanionService extends IntentService{

    private final String TAG = "ConVRgeCompanionService";
    private final String ENDPOINT = "http://www.convrge.co/api/users";
    private final int NOTIFICATION_ID = 12345;

    public int mPauseDuration = 10000;
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
            updateUIorCreateNotifications();
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

    public void updateUIorCreateNotifications(){
        if(MyApplication.isActivityVisible()) {
            updateUI();
        } else {
            createNotifications();
            updateOldServerObject();
        }
    }

    public void updateUI(){
        Log.d(TAG, "updateUI() called");
        ConVRgeUIThread newUIThread = new ConVRgeUIThread();
        newUIThread.start();
    }

    public void createNotifications(){
        Log.d(TAG, "createNotifications() START");

        String newPlayersString = "";
        ArrayList<ConVRgePlayer> oldPlayersList = mOldServerObject.getOnlineUsersList();
        ArrayList<ConVRgePlayer> newPlayersList = mServerObject.getOnlineUsersList();

        Log.d(TAG, "newPlayersString = _" + newPlayersString + "_");
        Log.d(TAG, "oldPlayerList.size()" + oldPlayersList.size());
        Log.d(TAG, "newPlayersList.size()" + newPlayersList.size());

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

        if(newPlayersString.equals("")) return;

        Context c = getApplicationContext();
        Intent targetIntent = new Intent(c, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(c)
                .setContentTitle("Convrge - new player online!")
                .setContentText(newPlayersString)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
        Log.d(TAG, "createNotifications() END");
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
            stopSelf();
        }
    }
}
