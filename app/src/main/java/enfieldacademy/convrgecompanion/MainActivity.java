package enfieldacademy.convrgecompanion;

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

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    public Intent mServiceIntent;
    public ListView mListView;
    public ServiceReceiver mServiceReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart(){
        super.onStart();

        /////////////////////////////////
        // REGISTERS BROADCAST RECEIVER /
        /////////////////////////////////
        registerReceiver();

        /////////////////////////////////
        //// STARTS THE SERVICE /////////
        /////////////////////////////////
        startService();

        mListView = (ListView) findViewById(R.id.list_view);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mServiceReceiver);
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
        mServiceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        registerReceiver(mServiceReceiver, intentFilter);
    }

    public void startService(){
        mServiceIntent = new Intent(this, ConVRgeCompanionService.class);
        startService(mServiceIntent);
    }

    private class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            Log.d(TAG, "onReceive called!");
        }
    }
}
