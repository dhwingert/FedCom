package com.dhwingert.fedcom.movetracker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.database.HistoryInfo;
import com.dhwingert.fedcom.R;

/**
 * Custom view for one history row in the History ListView in the HistoryListFragment.
 *
 * Created by David Wingert on 12/21/2014.
 */
public class HistoryListRowView extends RelativeLayout {

    private HistoryInfo mHistoryInfo;

    private TextView mHistoryIdTextView;
    private TextView mMoveTextView;
    private TextView mPlayerNameTextView;
    private TextView mShipTypeTextView;
    private TextView mShipNameTextView;
    private TextView mActionKeyTextView;
    private TextView mActionValueTextView;

    public static HistoryListRowView inflate(ViewGroup parent) {
        HistoryListRowView historyListRowView = (HistoryListRowView) LayoutInflater.from(parent.getContext()).inflate(R.layout.history_list_row_view, parent, false);

        return historyListRowView;
    }
    public HistoryListRowView(Context context) {
        this(context, null);
    }

    public HistoryListRowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HistoryListRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.history_list_row_view_children, this, true);
        setupChildren();
    }

    private void setupChildren() {
        mHistoryIdTextView = (TextView) findViewById(R.id.historyListIdTextView);
        mMoveTextView = (TextView) findViewById(R.id.historyMoveTextView);
        mPlayerNameTextView = (TextView) findViewById(R.id.historyPlayerNameTextView);
        mShipTypeTextView = (TextView) findViewById(R.id.historyShipTypeTextView);
        mShipNameTextView = (TextView) findViewById(R.id.historyShipNameTextView);
        mActionKeyTextView = (TextView) findViewById(R.id.historyKeyTextView);
        mActionValueTextView = (TextView) findViewById(R.id.historyValueTextView);
    }

    public void setItem(HistoryInfo historyInfo, GameInfo gameInfo) {
        mHistoryInfo = historyInfo;

        mHistoryIdTextView.setText(Long.toString(historyInfo.getId()));

        int turn = historyInfo.getTurn();
        int impulse = historyInfo.getImpulse();

        String move = "Start";
        if (turn > 0) {
            move = Integer.toString(turn) + ".";
            if (impulse > 0) {
                move += Integer.toString(impulse);
            } else {
                move += "Plan";
            }
        }
        mMoveTextView.setText(move);

        String shipType = (historyInfo.getShipID() >= 0) ? gameInfo.getUnitTypeLabel(historyInfo.getShipType()) + ":" : "";
        mShipTypeTextView.setText(shipType);

        mPlayerNameTextView.setText(historyInfo.getPlayerName());
        mShipNameTextView.setText(historyInfo.getShipName());

        mActionKeyTextView.setText(historyInfo.getKey());
        mActionValueTextView.setText(historyInfo.getValue());
    }

}
