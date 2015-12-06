package christiancoleman.whosonlineforconvrge;

public class ConVRgePlayer {

    public int mId;
    public String mPlayerName;

    public ConVRgePlayer(int id, String playerName){
        setId(id);
        setPlayerName(playerName);
    }

    public ConVRgePlayer(ConVRgePlayer player){
        this(player.getId(), player.getPlayerName());
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public String getPlayerName() {
        return mPlayerName;
    }

    public void setPlayerName(String mPlayerName) {
        this.mPlayerName = mPlayerName;
    }
}
