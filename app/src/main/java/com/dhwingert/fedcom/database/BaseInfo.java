package com.dhwingert.fedcom.database;

/**
 * The base class for ShipInfo and PlayerInfo.
 *
 * Created by David Wingert on 12/7/2014.
 */
public abstract class BaseInfo {

    protected long mId = -1;
    protected String mName = "";
    protected long mOrder = -1;

    public long getId() {
        return mId;
    }
    public void setId(long id) {
        this.mId = id;
    }

    public String getName() { return mName; }
    public void setName(String name) {
        this.mName = name;
    }

    public long getOrder() {
        return mOrder;
    }
    public void setOrder(long order) {
        this.mOrder = order;
    }

}
