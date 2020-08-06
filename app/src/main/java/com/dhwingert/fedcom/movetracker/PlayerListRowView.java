package com.dhwingert.fedcom.movetracker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.database.PlayerInfo;
import com.dhwingert.fedcom.R;


/**
 * Custom view for one ship in the Player ListView in the PlayerListFragment.
 *
 * Created by David Wingert on 12/6/2014.
 */
public class PlayerListRowView extends RelativeLayout {

    private TextView mPlayerIdTextView;
    private TextView mPlayerNameTextView;
    private TextView mPlayerPermLabelTextView;
    private TextView mPlayerPermCountTextView;
    private TextView mPlayerTempLabelTextView;
    private TextView mPlayerTempCountTextView;

    public static PlayerListRowView inflate(ViewGroup parent) {
        PlayerListRowView playerListRowView = (PlayerListRowView)LayoutInflater.from(parent.getContext()).inflate(R.layout.player_list_row_view, parent, false);

        return playerListRowView;
    }
    public PlayerListRowView(Context context) {
        this(context, null);
    }

    public PlayerListRowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerListRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.player_list_row_view_children, this, true);

        mPlayerIdTextView = (TextView) findViewById(R.id.playerIdTextView);
        mPlayerNameTextView = (TextView) findViewById(R.id.playerNameTextView);
        mPlayerPermLabelTextView = (TextView) findViewById(R.id.playerPermLblTextView);
        mPlayerPermCountTextView = (TextView) findViewById(R.id.playerPermCountTextView);
        mPlayerTempLabelTextView = (TextView) findViewById(R.id.playerTempLblTextView);
        mPlayerTempCountTextView = (TextView) findViewById(R.id.playerTempCountTextView);
    }

    public void setItem(PlayerInfo playerInfo, GameInfo gameInfo) {
        mPlayerIdTextView.setText(Long.toString(playerInfo.getId()));

        String playerName = playerInfo.getName();
        mPlayerNameTextView.setText( (playerName.length() > 0) ? playerName : getResources().getString(R.string.no_player_name) );
        mPlayerNameTextView.setTag(playerName);

        mPlayerPermLabelTextView.setText(gameInfo.getPermLabel() + ":");
        mPlayerPermCountTextView.setText(Integer.toString(playerInfo.getPermCount()));

        if (gameInfo.isHasTemp()) {
            mPlayerTempLabelTextView.setText(gameInfo.getTempLabel() + ":");
            mPlayerTempCountTextView.setText(Integer.toString(playerInfo.getTempCount()));
            mPlayerTempLabelTextView.setVisibility(VISIBLE);
            mPlayerTempCountTextView.setVisibility(VISIBLE);
        } else {
            mPlayerTempLabelTextView.setVisibility(GONE);
            mPlayerTempCountTextView.setVisibility(GONE);
        }
    }

    public String getPlayerName() {
        return (String) mPlayerNameTextView.getTag();
    }

}
