package enfieldacademy.convrgecompanion;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

public class ConVRgeServer implements Serializable{

    private final String TAG = "ConVRgeServer";

    ArrayList<ConVRgePlayer> mOnlineUsersList;
    int mNumUsersWatching;
    int mNumUsersOnline;

    public ConVRgeServer(){
        setOnlineUsersList(new ArrayList<ConVRgePlayer>());
        setNumUsersWatching(0);
    }

    public ArrayList<ConVRgePlayer> getOnlineUsersList() {
        return mOnlineUsersList;
    }

    public void setOnlineUsersList(ArrayList<ConVRgePlayer> onlineUsersList) {
        this.mOnlineUsersList = onlineUsersList;
    }

    public int getNumUsersWatching() {
        return mNumUsersWatching;
    }

    public void setNumUsersWatching(int numUsersWatching) {
        this.mNumUsersWatching = numUsersWatching;
    }

    public int getNumUsersOnline(){
        return mNumUsersOnline;
    }

    public void setNumUsersOnline(int numUsersOnline){
        this.mNumUsersOnline = numUsersOnline;
    }

    public void print(){
        for(int i = 0; i < getOnlineUsersList().size(); i++){
            ConVRgePlayer player = getOnlineUsersList().get(i);
            Log.d(TAG, (i+1) + ". " + player.getPlayerName() + " (" + player.getId() + ")");
        }
        Log.d(TAG, getNumUsersOnline() + " users online.");
        Log.d(TAG, getNumUsersWatching() + " users watching.");
    }

}
