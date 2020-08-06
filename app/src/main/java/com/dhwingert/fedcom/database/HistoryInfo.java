package com.dhwingert.fedcom.database;

/**
 * Information for one record in History Table
 * Created by David Wingert on 12/21/2014.
 */
public class HistoryInfo extends BaseInfo {

    private int mTurn;
    private int mImpulse;
    private long mPlayerID;
    private long mShipID;
    private int mShipType;
    private String mPlayerName;
    private String mShipName;
    private String mKey;
    private String mValue;

    public HistoryInfo(long id, int turn, int impulse, long playerID, long shipID, int shipType, String playerName, String shipName, String key, String value) {
        this.setId(id);
        mTurn = turn;
        mImpulse = impulse;
        mPlayerID = playerID;
        mShipID = shipID;
        mShipType = shipType;
        mPlayerName = playerName;
        mShipName = shipName;
        mKey = key;
        mValue = value;
    }

    public int getTurn() { return mTurn; }
    public void setTurn(int turn) { mTurn = turn;     }

    public int getImpulse() { return mImpulse; }
    public void setImpulse(int impulse) { mImpulse = impulse; }

    public long getPlayerID() { return mPlayerID; }

    public long getShipID() { return mShipID; }

    // Name of the Player this ship belongs to
    public String getPlayerName() {
        return mPlayerName;
    }

    // Name of the Ship
    public String getShipName() {
        return mShipName;
    }

    // Type of Ship (Ship, Shuttle, Drone, Torpedo)
    public int getShipType() {
        return mShipType;
    }

    // History Log Key (Action Logged)
    public String getKey() { return mKey; }

    // History Log Value (Additional info for Key)
    public String getValue() { return mValue; }

}
