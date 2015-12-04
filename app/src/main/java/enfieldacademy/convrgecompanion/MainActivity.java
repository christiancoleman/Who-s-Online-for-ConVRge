package enfieldacademy.convrgecompanion;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
        Log.d(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);

        /////////////////////////////////
        //// STARTS THE SERVICE /////////
        /////////////////////////////////
        startService();

        MyApplication.notificationsOn();
        MyApplication.notificationSoundsOn();
    }

    @Override
    protected void onDestroy() {
        /////////////////////////////////
        //// STOPS THE SERVICE //////////
        /////////////////////////////////
        stopService();

        Log.d(TAG, "onDestroy() called");

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_notifications:
                notificationMenuAction();
                return true;
            case R.id.action_close:
                closeMenuAction();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

    public void notificationMenuAction(){
        Log.d(TAG, "notificationMenuAction()");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String notificationsToggle;
        String notificationSoundsToggle;
        if(MyApplication.areNotificationsOn()){
            notificationsToggle = getResources().getString(R.string.notifications_on);
        } else {
            notificationsToggle = getResources().getString(R.string.notifications_off);
        }
        if(MyApplication.areNotificationSoundsOn()){
            notificationSoundsToggle = getResources().getString(R.string.notification_sounds_on);
        } else {
            notificationSoundsToggle = getResources().getString(R.string.notification_sounds_off);
        }
        builder.setMessage(R.string.notification_setting)
                .setPositiveButton(notificationsToggle, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(MyApplication.areNotificationsOn()) MyApplication.notificationsOff();
                        else MyApplication.notificationsOn();
                    }
                })
                .setNegativeButton(notificationSoundsToggle, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (MyApplication.areNotificationSoundsOn())
                            MyApplication.notificationSoundsOff();
                        else MyApplication.notificationSoundsOn();
                    }
                });
        Log.d(TAG, "right before builder.create()");
        builder.create();
        builder.show();
    }

    public void closeMenuAction(){
        Log.d(TAG, "closeMenuAction()");
        finish();
    }

    // Reference: http://developer.android.com/guide/components/services.html
    public void startService(){
        Log.d(TAG, "startService() called");
        MyApplication.serviceStarted();
        getApplicationContext().startService(new Intent(this, ConVRgeCompanionService.class));
    }

    public void stopService(){
        Log.d(TAG, "stopService() called");
        MyApplication.serviceEnded();
        getApplicationContext().stopService(new Intent(this, ConVRgeCompanionService.class));
        ConVRgeHelper.clearNotifications(this);
    }

    // Reference: http://developer.android.com/images/training/basics/basic-lifecycle.png
    @Override
    protected void onResume(){
        Log.d(TAG, "onResume() called");
        super.onResume();

        /////////////////////////////////
        // REGISTERS BROADCAST RECEIVER /
        /////////////////////////////////
        registerReceiver();

        restoreState();

        setContentView(R.layout.activity_main);
        setTitle("");
        mLocalServer = new ConVRgeServer();
        mUsersOnlineTV = (TextView) findViewById(R.id.users_online);
        mUsersWatchingTV = (TextView) findViewById(R.id.users_watching);
        mListOfUsersOnlineTV = (TextView) findViewById(R.id.list_of_online_users);
    }

    // Reference: http://developer.android.com/images/training/basics/basic-lifecycle.png
    @Override
    protected void onPause(){
        Log.d(TAG, "onPause() called");
        super.onPause();

        saveState();

        //////////////////////////////////////
        // UNREGISTERS BROADCAST RECEIVER ////
        //////////////////////////////////////
        unregisterReceiver(mServiceReceiver);
    }

    public void restoreState(){
        MyApplication.activityResumed();
        if(MyApplication.getSavedServer() == null) mLocalServer = new ConVRgeServer();
        else mLocalServer = MyApplication.getSavedServer();

    }

    public void saveState(){
        MyApplication.setSavedServer(mLocalServer);
        MyApplication.activityPaused();
    }

    public void registerReceiver(){
        mServiceReceiver = new ConVRgeCompanionServiceReceiver();
        IntentFilter intentFilter = new IntentFilter("com.enfieldacademy.CUSTOM_INTENT");
        registerReceiver(mServiceReceiver, intentFilter);
    }

    public class ConVRgeCompanionServiceReceiver extends BroadcastReceiver {

        private final String TAG = "ConVRgeServiceReceiver";

        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d(TAG, "onReceive called!");
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

                //Log.d(TAG, "***");
                //mLocalServer.print();
                //Log.d(TAG, "***");
            }
        }
    }
}
