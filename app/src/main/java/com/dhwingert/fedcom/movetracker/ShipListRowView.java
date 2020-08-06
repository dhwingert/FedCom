package com.dhwingert.fedcom.movetracker;

import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dhwingert.fedcom.database.DatabaseConnector;
import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.R;
import com.dhwingert.fedcom.database.ShipInfo;

/**
 * Custom view for one ship in the Ship ListView in the ShipListFragment.
 *
 * Created by David Wingert on 12/6/2014.
 */
public class ShipListRowView extends RelativeLayout {

    private ShipInfo mShipInfo;
    private GameInfo mGameInfo;

    private TextView mUnitIdTextView;
    private TextView mUnitNameTextView;
    private TextView mPlayerNameTextView;
    private EditText mUnitSpeedEditText;
    private TextView mUnitTypeTextView;
    private TextView mUnitInitLblTextView;
    private TextView mUnitInitValTextView;
    private TextView mUnitAddedTextView;

    public static ShipListRowView inflate(ViewGroup parent) {
        ShipListRowView shipListRowView = (ShipListRowView)LayoutInflater.from(parent.getContext()).inflate(R.layout.ship_list_row_view, parent, false);

        return shipListRowView;
    }
    public ShipListRowView(Context context) {
        this(context, null);
    }

    public ShipListRowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShipListRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.ship_list_row_view_children, this, true);

        mUnitIdTextView = (TextView) findViewById(R.id.shipListIdTextView);
        mPlayerNameTextView = (TextView) findViewById(R.id.playerNameTextView);
        mUnitNameTextView = (TextView) findViewById(R.id.shipListNameTextView);
        mUnitSpeedEditText = (EditText) findViewById(R.id.shipListSpeedEditText);
        mUnitTypeTextView = (TextView) findViewById(R.id.shipListTypeTextView);
        mUnitInitLblTextView = (TextView) findViewById(R.id.shipListInitLblTextView);
        mUnitInitValTextView = (TextView) findViewById(R.id.shipListInitValTextView);
        mUnitAddedTextView = (TextView) findViewById(R.id.shipListAddedTextView);

        // Speed should select all text on focus so user can just type over it
        mUnitSpeedEditText.setSelectAllOnFocus(true);
    }

    public void setItem(ShipInfo shipInfo, GameInfo gameInfo) {
        mShipInfo = shipInfo;
        mGameInfo = gameInfo;

        mUnitIdTextView.setText(Long.toString(shipInfo.getId()));
        mUnitNameTextView.setText(shipInfo.getName());

        mUnitTypeTextView.setText(mGameInfo.getUnitTypeLabel(shipInfo.getType()) + ":");

        mUnitInitLblTextView.setText(mGameInfo.getInitLabel() + ":");
        if (mGameInfo.isInitIsNumber()) {
            mUnitInitValTextView.setText( Integer.toString(shipInfo.getInit()) );
        } else {
            mUnitInitValTextView.setText(mGameInfo.getInitListLabel(shipInfo.getInit()));
        }

        mUnitSpeedEditText.setText(mGameInfo.isSpeedValid(shipInfo.getCurrentSpeed()) ? Integer.toString(shipInfo.getCurrentSpeed()) : "");
        mUnitSpeedEditText.addTextChangedListener(mShipSpeedEditTextChangedListener);

        String playerName = shipInfo.getPlayerName();
        mPlayerNameTextView.setText( (playerName.length() > 0) ? playerName : getResources().getString(R.string.no_player_name) );
        mPlayerNameTextView.setTag(playerName);

        mUnitAddedTextView.setText(MoveTrackerMessages.getShipAddedLabel(shipInfo.getAddTurn(), shipInfo.getAddImpulse(), getContext()));
    }

    // Whenever the user changes the ship's speed validated it immediately and save the changes
    TextWatcher mShipSpeedEditTextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            String speedString = mUnitSpeedEditText.getText().toString().trim();
            if (speedString.length() > 0) {
                try {
                    int newSpeed = Integer.parseInt(speedString);
                    if (mGameInfo.isSpeedValid(newSpeed)) {
                        mShipInfo.setCurrentSpeed(newSpeed);
                        saveShipSpeed();

                    } else {
                        Toast errorMsg = Toast.makeText(getContext(), getResources().getString(R.string.message_unit_speed_error), Toast.LENGTH_SHORT);
                        errorMsg.show();

                        mUnitSpeedEditText.setText("");
                    }
                } catch (Exception e) {
                    // User may have typed - as start of entering a negative number
                }
            }
        }
    };

    //**** Save Ship's new speed to both History and Ships Tables **********************************

    private void saveShipSpeed() {

            // AsyncTask to save player, then notify mListener
            AsyncTask<Object, Object, Object> saveShipSpeedTask = new AsyncTask<Object, Object, Object>() {
                @Override
                protected Object doInBackground(Object... params) {
                    // Get DatabaseConnector to interact with the SQLite database
                    DatabaseConnector databaseConnector = new DatabaseConnector(getContext());

                    databaseConnector.saveCurrentShipSpeed(mShipInfo.getId(),
                                                           mShipInfo.getPlayerId(),
                                                           mShipInfo.getCurrentSpeed());

                    return null;
                }
                @Override
                protected void onPostExecute(Object result) {
                }
            };

            // Save the player to the database using a separate thread
            saveShipSpeedTask.execute((Object[]) null);
    }

}
