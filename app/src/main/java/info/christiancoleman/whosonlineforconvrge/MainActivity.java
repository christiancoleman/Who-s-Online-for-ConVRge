package info.christiancoleman.whosonlineforconvrge;

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
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "ConVRgeMainActivity";

    public WhosOnlineServiceReceiver mServiceReceiver;
    public TextView mUsersOnlineTV;
    public TextView mUsersWatchingTV;
    public TextView mListOfUsersOnlineTV;

    private ConVRgeServer mLocalServer;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);

        /////////////////////////////////
        //// STARTS THE SERVICE /////////
        /////////////////////////////////
        startService();

        WhosOnlineApplication.notificationsOn();
        WhosOnlineApplication.notificationSoundsOn();
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
        if(WhosOnlineApplication.areNotificationsOn()){
            notificationsToggle = getResources().getString(R.string.notifications_on);
        } else {
            notificationsToggle = getResources().getString(R.string.notifications_off);
        }
        if(WhosOnlineApplication.areNotificationSoundsOn()){
            notificationSoundsToggle = getResources().getString(R.string.notification_sounds_on);
        } else {
            notificationSoundsToggle = getResources().getString(R.string.notification_sounds_off);
        }
        builder.setMessage(R.string.notification_setting)
                .setPositiveButton(notificationsToggle, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(WhosOnlineApplication.areNotificationsOn()) {
                            WhosOnlineApplication.notificationsOff();
                            showToast(getResources().getString(R.string.notifications_off));
                        }
                        else {
                            WhosOnlineApplication.notificationsOn();
                            showToast(getResources().getString(R.string.notifications_on));
                        }
                    }
                })
                .setNegativeButton(notificationSoundsToggle, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (WhosOnlineApplication.areNotificationSoundsOn()) {
                            WhosOnlineApplication.notificationSoundsOff();
                            showToast(getResources().getString(R.string.notification_sounds_off));
                        }
                        else {
                            WhosOnlineApplication.notificationSoundsOn();
                            showToast(getResources().getString(R.string.notification_sounds_on));
                        }
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
        WhosOnlineApplication.serviceStarted();
        getApplicationContext().startService(new Intent(this, WhosOnlineService.class));
    }

    public void stopService(){
        Log.d(TAG, "stopService() called");
        WhosOnlineApplication.serviceEnded();
        getApplicationContext().stopService(new Intent(this, WhosOnlineService.class));
        WhosOnlineHelper.clearNotifications(this);
    }

    // Reference: http://developer.android.com/images/training/basics/basic-lifecycle.png
    @Override
    protected void onResume(){
        Log.d(TAG, "onResume() called");
        super.onResume();

        ///////////////////////////////////////
        // REGISTERS BROADCAST RECEIVER ///////
        ///////////////////////////////////////
        registerReceiver();

        ////////////////////////////////////////
        // RESTORES STATE OR STARTS NEW STATE //
        ////////////////////////////////////////
        restoreState();

        setContentView(R.layout.activity_main);

        setTitle("");

        updateUI();
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
        WhosOnlineApplication.activityResumed();
        if(WhosOnlineApplication.getSavedServer() == null) mLocalServer = new ConVRgeServer();
        else mLocalServer = WhosOnlineApplication.getSavedServer();

    }

    public void saveState(){
        WhosOnlineApplication.setSavedServer(mLocalServer);
        WhosOnlineApplication.activityPaused();
    }

    public void registerReceiver(){
        mServiceReceiver = new WhosOnlineServiceReceiver();
        IntentFilter intentFilter = new IntentFilter("com.info.info.christiancoleman.CUSTOM_INTENT");
        registerReceiver(mServiceReceiver, intentFilter);
    }

    public class WhosOnlineServiceReceiver extends BroadcastReceiver {

        //private final String TAG = "ConVRgeServiceReceiver";

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
                updateUI();
            }
        }
    }

    public void updateUI(){
        mUsersOnlineTV = (TextView) findViewById(R.id.users_online);
        mUsersWatchingTV = (TextView) findViewById(R.id.users_watching);
        mListOfUsersOnlineTV = (TextView) findViewById(R.id.list_of_online_users);

        String online = getResources().getString(R.string.users_online_textview);
        String watching = getResources().getString(R.string.users_watching_textview);

        mUsersOnlineTV.setText(String.format(online, mLocalServer.getNumUsersOnline()));
        mUsersWatchingTV.setText(String.format(watching, mLocalServer.getNumUsersWatching()));
        mListOfUsersOnlineTV.setText("");
        for(int i = 0; i < mLocalServer.getOnlineUsersList().size(); i++){
            String newName;
            String oldNames = mListOfUsersOnlineTV.getText().toString();
            if(i == 0) newName = String.format(getResources().getString(R.string.users_list_textview_start), mLocalServer.getOnlineUsersList().get(i).getPlayerName());
            else newName = String.format(getResources().getString(R.string.users_list_textview_more), mLocalServer.getOnlineUsersList().get(i).getPlayerName());
            mListOfUsersOnlineTV.setText(String.format(getResources().getString(R.string.users_textview_format_adder), oldNames, newName));
        }

        //Log.d(TAG, "***");
        //mLocalServer.print();
        //Log.d(TAG, "***");
    }

    public void showToast(String s){
        if(mToast != null) mToast.cancel();
        mToast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT);
        mToast.show();
    }
}
