package enfieldacademy.convrgecompanion;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "ConVRgeMainActivity";

    public ConVRgeCompanionServiceReceiver mServiceReceiver;
    public TextView mUsersOnlineTV;
    public TextView mUsersWatchingTV;
    public TextView mListOfUsersOnlineTV;

    private ConVRgeServer mLocalServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /////////////////////////////////
        //// STARTS THE SERVICE /////////
        /////////////////////////////////
        startService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /////////////////////////////////
        //// STOPS THE SERVICE //////////
        /////////////////////////////////
        stopService();
    }

    // Reference: http://developer.android.com/images/training/basics/basic-lifecycle.png
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
        // REGISTERS BROADCAST RECEIVER /
        /////////////////////////////////
        registerReceiver();
    }

    // Reference: http://developer.android.com/images/training/basics/basic-lifecycle.png
    @Override
    protected void onPause(){
        super.onPause();

        MyApplication.activityPaused();

        //////////////////////////////////////
        // UNREGISTERS BROADCAST RECEIVER ////
        //////////////////////////////////////
        unregisterReceiver(mServiceReceiver);
    }

    public void registerReceiver(){
        mServiceReceiver = new ConVRgeCompanionServiceReceiver();
        IntentFilter intentFilter = new IntentFilter("com.enfieldacademy.CUSTOM_INTENT");
        registerReceiver(mServiceReceiver, intentFilter);
    }

    // Reference: http://developer.android.com/guide/components/services.html
    public void startService(){
        startService(new Intent(this, ConVRgeCompanionService.class));
    }

    public void stopService(){
        stopService(new Intent(this, ConVRgeCompanionService.class));
        ConVRgeHelper.clearNotifications(this);
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
