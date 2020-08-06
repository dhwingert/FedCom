package com.dhwingert.fedcom.movetracker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.dhwingert.fedcom.database.BaseInfo;
import com.dhwingert.fedcom.util.DragNDropAdapter;
import com.dhwingert.fedcom.util.DragNDropListView;
import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.database.PlayerInfo;
import com.dhwingert.fedcom.util.StableArrayAdapter;

import java.util.List;

/**
 * Adapter for ship ListView in PlayerListFragment.
 *
 * This supports the custom row views in the players list.
 *
 * Created by David Wingert on 12/30/2014.
 */
public class PlayerListAdapter extends StableArrayAdapter implements DragNDropAdapter {

    int mPosition[];
    int mHandler;

    private GameInfo mGameInfo;

    public void setGameInfo(GameInfo gameInfo) {
        mGameInfo = gameInfo;
    }

    public PlayerListAdapter(Context context, List<PlayerInfo> playerInfoList, int handler) {
        super(context, 0, playerInfoList);

        mHandler = handler;
        setup(playerInfoList.size());
    }

    private void setup(int size) {
        mPosition = new int[size];

        for (int i = 0; i < size; ++i) mPosition[i] = i;
    }

    @Override
    public void notifyDataSetChanged () {
        setup(getCount());
        super.notifyDataSetChanged();
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup group) {
        return super.getDropDownView(mPosition[position], view, group);
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(mPosition[position]);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(mPosition[position]);
    }

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < getCount()) {
            return super.getItemId(mPosition[position]);
        } else {
            return INVALID_ID;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PlayerListRowView playerListRowView = (PlayerListRowView)convertView;

        if (null == playerListRowView) {
            playerListRowView = PlayerListRowView.inflate(parent);
        }
        playerListRowView.setItem((PlayerInfo) getItem(position), mGameInfo);

        return playerListRowView;
    }

    @Override
    public boolean isEnabled(int position) {
        return super.isEnabled(mPosition[position]);
    }

    @Override
    public int getDragHandler() {
        return mHandler;
    }

    @Override
    public void onItemDrag(DragNDropListView parent, View view, int position, long id) {

    }

    @Override
    public void onRowClicked(View view, BaseInfo baseInfo) {

    }

    @Override
    public void onItemDrop(DragNDropListView parent, View view, int startPosition, int endPosition, long id) {
        int position = mPosition[startPosition];

        if (startPosition < endPosition)
            for(int i = startPosition; i < endPosition; ++i)
                mPosition[i] = mPosition[i + 1];
        else if (endPosition < startPosition)
            for(int i = startPosition; i > endPosition; --i)
                mPosition[i] = mPosition[i - 1];

        mPosition[endPosition] = position;
    }
}
