package enfieldacademy.convrgecompanion;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "ConVRgeMainActivity";

    public static boolean serviceCancelled = false;

    public Intent mServiceIntent;
    public ConVRgeCompanionServiceReceiver mServiceReceiver;
    public TextView mUsersOnlineTV;
    public TextView mUsersWatchingTV;
    public TextView mListOfUsersOnlineTV;

    private ConVRgeServer mLocalServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onResume(){
        super.onResume();

        setContentView(R.layout.activity_main);

        setTitle("");

        mLocalServer = new ConVRgeServer();

        mUsersOnlineTV = (TextView) findViewById(R.id.users_online);
        mUsersWatchingTV = (TextView) findViewById(R.id.users_watching);
        mListOfUsersOnlineTV = (TextView) findViewById(R.id.list_of_online_users);

        MyApplication.activityResumed();

        /////////////////////////////////
        //// STARTS THE SERVICE /////////
        /////////////////////////////////
        if(!MyApplication.isServiceRunning()) {
            ConVRgeCompanionService.SERVICE_STOPPED = false;
            startService();
        }

        /////////////////////////////////
        // REGISTERS BROADCAST RECEIVER /
        /////////////////////////////////
        registerReceiver();
    }

    @Override
    protected void onPause(){
        super.onPause();

        MyApplication.activityPaused();

        mLocalServer = null;

        unregisterReceiver(mServiceReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // TODO: This is a hack - fix this
        if (id == R.id.action_close_app) {
            stopService();
            finish();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void registerReceiver(){
        mServiceReceiver = new ConVRgeCompanionServiceReceiver();
        IntentFilter intentFilter = new IntentFilter("com.enfieldacademy.CUSTOM_INTENT");
        registerReceiver(mServiceReceiver, intentFilter);
    }

    public void startService(){
        mServiceIntent = new Intent(this, ConVRgeCompanionService.class);
        startService(mServiceIntent);
    }

    // TODO: This is a hack - fix this
    public void stopService(){
        stopService(new Intent(getApplicationContext(), ConVRgeCompanionService.class));
        ConVRgeCompanionService.SERVICE_STOPPED = true;
    }

    public class ConVRgeCompanionServiceReceiver extends BroadcastReceiver {

        private final String TAG = "ConVRgeServiceReceiver";

        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive called!");
            Bundle extras = intent.getExtras();
            if(extras != null) {
                mLocalServer.setNumUsersOnline(extras.getInt("USERS_ONLINE"));
                mLocalServer.setNumUsersWatching(extras.getInt("USERS_WATCHING"));
                ArrayList<ConVRgePlayer> temp = new ArrayList<>();
                for(int i = 0; i < mLocalServer.getNumUsersOnline(); i++){
                    int id = extras.getInt(i + "-id");
                    String name = extras.getString(i + "-name");
                    temp.add(new ConVRgePlayer(id, name));
                }
                mLocalServer.setOnlineUsersList(temp);

                mUsersOnlineTV.setText(mLocalServer.getNumUsersOnline() + " users online");
                mUsersWatchingTV.setText(mLocalServer.getNumUsersWatching() + " users watching");
                mListOfUsersOnlineTV.setText("");
                for(int i = 0; i < mLocalServer.getOnlineUsersList().size(); i++){
                    String optionalText = "";
                    if(i != 0) optionalText = ", ";
                    mListOfUsersOnlineTV.setText(mListOfUsersOnlineTV.getText()
                            + optionalText
                            + mLocalServer.getOnlineUsersList().get(i).getPlayerName());
                }

                Log.d(TAG, "***");
                mLocalServer.print();
                Log.d(TAG, "***");
            }
        }
    }
}
