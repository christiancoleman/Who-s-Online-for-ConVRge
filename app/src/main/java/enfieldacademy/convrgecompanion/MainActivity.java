package enfieldacademy.convrgecompanion;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    public Intent mServiceIntent;
    public ConVRgeCompanionServiceReceiver mServiceReceiver;
    public TextView mUsersOnlineTV;
    public TextView mUsersWatchingTV;
    public ListView mListView;

    private ConVRgeServer mLocalServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setTitle("");

        mUsersOnlineTV = (TextView) findViewById(R.id.users_online);
        mUsersWatchingTV = (TextView) findViewById(R.id.users_watching);
        mListView = (ListView) findViewById(R.id.list_view);
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onResume(){
        super.onResume();

        MyApplication.activityResumed();

        mLocalServer = new ConVRgeServer();

        /////////////////////////////////
        //// STARTS THE SERVICE /////////
        /////////////////////////////////
        if(!MyApplication.isServiceRunning()) startService();

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
        if (id == R.id.action_settings) {
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

                if(mUsersWatchingTV == null) return;
                if(mUsersOnlineTV == null) return;
                mUsersOnlineTV.setText(mLocalServer.getNumUsersOnline() + " users online");
                mUsersWatchingTV.setText(mLocalServer.getNumUsersWatching() + " users watching");

                Log.d(TAG, "***" + mLocalServer.getNumUsersOnline());
                Log.d(TAG, "***" + mLocalServer.getNumUsersWatching());
            }
        }
    }
}
