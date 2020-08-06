package com.dhwingert.fedcom.movetracker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.database.HistoryInfo;
import com.dhwingert.fedcom.util.StableArrayAdapter;

import java.util.List;

/**
 * Adapter for ship ListView in HistoryListFragment.
 *
 * This supports the custom row views in the history list.
 *
 * Created by David Wingert on 12/21/2014.
 */
public class HistoryListAdapter extends StableArrayAdapter {

    private GameInfo mGameInfo;

    public HistoryListAdapter(Context context, List<HistoryInfo> historyInfoList) {
        super(context, 0, historyInfoList);
    }

    public void setGameInfo(GameInfo gameInfo) {
        mGameInfo = gameInfo;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HistoryListRowView historyListRowView = (HistoryListRowView)convertView;

        if (null == historyListRowView) {
            historyListRowView = HistoryListRowView.inflate(parent);
        }
        historyListRowView.setItem((HistoryInfo)  getItem(position), mGameInfo);

        return historyListRowView;
    }

}
