package com.dhwingert.fedcom.movetracker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.database.ShipInfo;
import com.dhwingert.fedcom.util.StableArrayAdapter;

import java.util.List;

/**
 * Adapter for ship ListView in ImpulseListFragment.
 *
 * This supports the custom row views in the impulse details list.
 *
 * Created by David Wingert on 12/21/2014.
 */
public class ImpulseDetailsAdapter extends StableArrayAdapter {

    private GameInfo mGameInfo;

    public ImpulseDetailsAdapter(Context context, List<ShipInfo> impulseInfoList) {
        super(context, 0, impulseInfoList);
    }

    public void setGameInfo(GameInfo gameInfo) {
        mGameInfo = gameInfo;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImpulseDetailsRowView impulseDetailsRowView = (ImpulseDetailsRowView)convertView;

        if (null == impulseDetailsRowView) {
            impulseDetailsRowView = ImpulseDetailsRowView.inflate(parent);
        }
        impulseDetailsRowView.setItem((ShipInfo) getItem(position), mGameInfo);

        return impulseDetailsRowView;
    }

}
