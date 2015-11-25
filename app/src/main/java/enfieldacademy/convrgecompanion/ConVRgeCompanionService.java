package enfieldacademy.convrgecompanion;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

public class ConVRgeCompanionService extends IntentService{

    private final String TAG = "ConVRgeCompanionService";
    private final String ENDPOINT = "http://www.convrge.co/api/users";
    private final int NOTIFICATION_ID = 12345;

    public int mPauseDuration = 10000;
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
        createServerObject();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    protected void onHandleIntent(Intent workIntent){
        while(true){
            queryServer();
            parseResult();
            buildConVRgeServer();
            printResults();
            updateUI();
            createNotifications();
            sleep(mPauseDuration);
        }
    }

    public void createServerObject(){
        // this provides the server object with the default values
        mServerObject = new ConVRgeServer();
    }

    public void sleep(int duration){
        SystemClock.sleep(duration);
        Log.d(TAG, mPauseDuration / 1000 + " seconds has passed"); // 1000 is # ms = 1 second
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

    public void updateUI(){
        UIThread newUIThread = new UIThread();
        newUIThread.start();
    }

    public void createNotifications(){
        Intent targetIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Convrge - A friend is online!")
                .setContentText("Para is online!")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public class UIThread extends Thread{
        @Override
        public void run() {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            sendBroadcast(intent);
            stopSelf();
        }
    }
}
