package com.dhwingert.fedcom.movetracker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.database.ShipInfo;
import com.dhwingert.fedcom.util.StableArrayAdapter;

import java.util.List;

/**
 * Adapter for ship ListView in ShipListFragment.
 *
 * This supports the custom row views in the ship list.
 *
 * Created by David Wingert on 12/6/2014.
 */
public class ShipListAdapter extends StableArrayAdapter {

    private GameInfo mGameInfo;

    public ShipListAdapter(Context context, List<ShipInfo> shipInfoList) {
        super(context, 0, shipInfoList);
    }

    public void setGameInfo(GameInfo gameInfo) {
        mGameInfo = gameInfo;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ShipListRowView shipListRowView = (ShipListRowView)convertView;

        if (null == shipListRowView) {
            shipListRowView = ShipListRowView.inflate(parent);
        }
        shipListRowView.setItem((ShipInfo) getItem(position), mGameInfo);

        return shipListRowView;
    }

}
