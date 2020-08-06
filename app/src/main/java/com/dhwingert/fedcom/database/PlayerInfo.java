package com.dhwingert.fedcom.database;

/**
 * Contains the info for one Player and all his ships in the Impulse Chart.
 *
 * Created by David Wingert on 11/29/2014.
 */
public class PlayerInfo extends BaseInfo {

    private int mPermCount = 0;
    private int mTempCount = 0;
    private int mDroneCount = 0;
    private int mTorpedoCount = 0;

    public PlayerInfo(long id, String name) {
        mId = id;
        mName = name;
        mPermCount = 0;
        mTempCount = 0;
        mDroneCount = 0;
        mTorpedoCount = 0;
    }

    public PlayerInfo(long id, String name, int permCount, int tempCount) {
        mId = id;
        mName = name;
        mPermCount = permCount;
        mTempCount = tempCount;
    }

    // Count of permanent units this player has in the Ships table
    public int getPermCount() {
        return mPermCount;
    }

    // Count of temporary units this player has in the Ships table
    public int getTempCount() {
        return mTempCount;
    }

}
