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
        setNumUsersOnline(0);
        setNumUsersWatching(0);
    }

    public ConVRgeServer(ArrayList<ConVRgePlayer> playerList, int numUsersOnline, int numUsersWatching){
        setOnlineUsersList(playerList);
        setNumUsersOnline(numUsersOnline);
        setNumUsersWatching(numUsersWatching);
    }

    public ConVRgeServer(ConVRgeServer server){
        this(new ArrayList<>(server.cloneList()), server.getNumUsersOnline(), server.getNumUsersWatching());
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

    public ArrayList<ConVRgePlayer> cloneList(){
        ArrayList<ConVRgePlayer> clonedPlayerList = new ArrayList<>();
        for(ConVRgePlayer player : this.getOnlineUsersList()){
            clonedPlayerList.add(new ConVRgePlayer(player));
        }
        return clonedPlayerList;
    }

    public void print(){
        for(int i = 0; i < getOnlineUsersList().size(); i++){
            ConVRgePlayer player = getOnlineUsersList().get(i);
            Log.d(TAG, (i + 1) + ". " + player.getPlayerName() + " (" + player.getId() + ")");
        }
        Log.d(TAG, getNumUsersOnline() + " users online.");
        Log.d(TAG, getNumUsersWatching() + " users watching.");
    }

}
