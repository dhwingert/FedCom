package com.dhwingert.fedcom.movetracker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.R;
import com.dhwingert.fedcom.database.ShipInfo;

/**
 * Custom view for one ship's row in the ListViews in the ImpulseDetailsFragment.
 *
 * Created by David Wingert on 12/21/2014.
 */
public class ImpulseDetailsRowView extends RelativeLayout {

    private ShipInfo mShipInfo;

    private TextView mPlayerNameTextView;
    private TextView mShipNameTextView;
    private TextView mShipMoveTextView;
    private TextView mTurnModeTextView;

    public static ImpulseDetailsRowView inflate(ViewGroup parent) {
        ImpulseDetailsRowView impulseDetailsRowView = (ImpulseDetailsRowView) LayoutInflater.from(parent.getContext()).inflate(R.layout.impulse_details_row_view, parent, false);

        return impulseDetailsRowView;
    }
    public ImpulseDetailsRowView(Context context) {
        this(context, null);
    }

    public ImpulseDetailsRowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImpulseDetailsRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.impulse_details_row_view_children, this, true);
        setupChildren();
    }

    private void setupChildren() {
        mPlayerNameTextView = (TextView) findViewById(R.id.impulsePlayerTextView);
        mShipNameTextView = (TextView) findViewById(R.id.impulseShipTextView);
        mShipMoveTextView = (TextView) findViewById(R.id.impulseShipMove);
        mTurnModeTextView = (TextView) findViewById(R.id.impulseTurnMode);
    }

    public void setItem(ShipInfo shipInfo, GameInfo gameInfo) {
        mShipInfo = shipInfo;

        mPlayerNameTextView.setText(shipInfo.getPlayerName());
        mShipNameTextView.setText(shipInfo.getName());

        mPlayerNameTextView.setTag(shipInfo.getPlayerId());
        mShipNameTextView.setTag(shipInfo.getId());

        mShipMoveTextView.setText(shipInfo.getMoveText(gameInfo));

        String turnMode = (shipInfo.getInit() >= 0) ? gameInfo.getInitListLabel(shipInfo.getInit()) : "?";

        mTurnModeTextView.setText(turnMode);
    }

    // Get the Player ID and Ship ID of this row
    public ShipInfo getBasicShipInfo() {
        return mShipInfo.getBasicShipInfo();
    }

}
