package com.dhwingert.fedcom.movetracker;

import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dhwingert.fedcom.database.DatabaseConnector;
import com.dhwingert.fedcom.util.DragNDropListView;
import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.R;
import com.dhwingert.fedcom.database.ShipInfo;


/**
 * Custom view for one unit in the Unit ListView in the PlayerDetailsFragment.
 *
 * Created by David Wingert on 12/6/2014.
 */
public class PlayerShipRowView extends RelativeLayout {

    private TextView mUnitIdTextView;
    private Spinner mUnitTypeSpinner;
    private TextView mUnitNameTextView;
    private TextView mInitLblTextView;
    private Spinner mInitSpinner;
    private EditText mInitEditText;
    private TextView mSpeedLblTextView;
    private EditText mSpeedEditText;
    private TextView mUnitAddedTextView;

    private ShipInfo mShipInfo;
    private GameInfo mGameInfo;

    public static PlayerShipRowView inflate(ViewGroup parent) {
        PlayerShipRowView playerShipRowView = (PlayerShipRowView)LayoutInflater.from(parent.getContext()).inflate(R.layout.player_ship_row_view, parent, false);

        return playerShipRowView;
    }
    public PlayerShipRowView(Context context) {
        this(context, null);
    }

    public PlayerShipRowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerShipRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.player_ship_row_view_children, this, true);

        mUnitIdTextView = (TextView) findViewById(R.id.unitIdTextView);
        mUnitNameTextView = (TextView) findViewById(R.id.unitNameTextView);
        mInitLblTextView = (TextView) findViewById(R.id.unitInitLabelTextView);
        mSpeedLblTextView = (TextView) findViewById(R.id.unitSpeedLabelTextView);
        mSpeedEditText = (EditText) findViewById(R.id.unitSpeedEditText);
        mUnitAddedTextView = (TextView) findViewById(R.id.shipAddedTextView);

        // Set up the Unit Type Spinner
        mUnitTypeSpinner = (Spinner) findViewById(R.id.unitTypeSpinner);
        mUnitTypeSpinner.setOnItemSelectedListener(mShipTypeSpinnerOnItemSelectedListener);

        // Set up the Turn Mode Spinner and EditText (one or the other is visible)
        mInitEditText = (EditText) findViewById(R.id.unitInitEditText);
        mInitSpinner = (Spinner) findViewById(R.id.unitInitSpinner);
        mInitSpinner.setOnItemSelectedListener(mTurnModeSpinnerOnItemSelectedListener);

        // Speed and Init should select all text on focus so user can just type over it
        mSpeedEditText.setSelectAllOnFocus(true);
        mInitEditText.setSelectAllOnFocus(true);

        // If the user clicks on any of the controls in the row
        //      Tell the parent list
        mUnitIdTextView.setOnClickListener(mRowOnClickListener);
        mUnitNameTextView.setOnClickListener(mRowOnClickListener);
        mInitLblTextView.setOnClickListener(mRowOnClickListener);
        mSpeedLblTextView.setOnClickListener(mRowOnClickListener);
        mUnitAddedTextView.setOnClickListener(mRowOnClickListener);

        // Turn Mode and Ship Type Spinners don't get a click event.  They gets Touch instead.
        mInitSpinner.setOnTouchListener(mTurnModeSpinnerOnTouchListener);
        mUnitTypeSpinner.setOnTouchListener(mTurnModeSpinnerOnTouchListener);

        // Speed and Init EditText don't get a click event.  It gets Focus instead.
        mSpeedEditText.setOnFocusChangeListener(mSpeedAndInitEditTextOnFocusChangeListener);
        mInitEditText.setOnFocusChangeListener(mSpeedAndInitEditTextOnFocusChangeListener);

        // Whenever the user changes this row's speed or init save it immediately.
        mSpeedEditText.addTextChangedListener(mSpeedEditTextChangedListener);
        mInitEditText.addTextChangedListener(mInitEditTextChangedListener);
    }

    public void setItem(ShipInfo shipInfo, GameInfo gameInfo) {
        mShipInfo = shipInfo;
        mGameInfo = gameInfo;

        // Put the game labels for permanent units (and temp if the game has them)
        ArrayAdapter<String> unitTypeAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item_12, mGameInfo.getListOfUnitTypeLabels());
        unitTypeAdapter.setDropDownViewResource(R.layout.spinner_item_drop);
        mUnitTypeSpinner.setAdapter(unitTypeAdapter);

        // Unit Type spinner is only enabled if there are temp units.  (If not, spinner only has perm unit label in it anyway)
        mUnitTypeSpinner.setEnabled(mGameInfo.isHasTemp());

        mInitLblTextView.setText(mGameInfo.getInitLabel() + ":");

        // Unit Init List Spinner or EditText (numeric or string Init)
        if (mGameInfo.isInitIsNumber()) {
            mInitSpinner.setVisibility(View.GONE);
            mInitEditText.setVisibility(View.VISIBLE);
        } else {
            mInitEditText.setVisibility(View.GONE);
            mInitSpinner.setVisibility(View.VISIBLE);

            ArrayAdapter<String> unitInitListAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item_12, mGameInfo.getInitListSplit());
            unitInitListAdapter.setDropDownViewResource(R.layout.spinner_item_drop);
            mInitSpinner.setAdapter(unitInitListAdapter);
        }

        // NOW PUT UNIT VALUES IN CONTROLS
        String speed = (shipInfo.getCurrentSpeed() != ShipInfo.INVALID_SPEED) ? Integer.toString(shipInfo.getCurrentSpeed()) : "";

        mUnitTypeSpinner.setSelection(shipInfo.getType());

        if (mGameInfo.isInitIsNumber()) {
            mInitEditText.setText(Integer.toString(shipInfo.getInit()));
        } else {
            mInitSpinner.setSelection(shipInfo.getInit());
        }

        mUnitIdTextView.setText(Long.toString(shipInfo.getId()));
        mUnitNameTextView.setText(shipInfo.getName());
        mSpeedEditText.setText(speed);

        mUnitAddedTextView.setText(MoveTrackerMessages.getShipAddedLabel(shipInfo.getAddTurn(), shipInfo.getAddImpulse(), getContext()));
    }

    //**** When any control in row is clicked, tell Listener new row is selected *******************

    // Tell the DragNDropListView that this row has been clicked on
    //      PlayerDetailsFragment will show the X delete button for this row
    //      and hide it on the previous row it was shown on (if any).
    OnClickListener mRowOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            View rowView = (View) v.getParent();
            if (rowView != null) {
                DragNDropListView listParent = (DragNDropListView) getParent();
                if (listParent != null) {
                    // Put focus on Speed EditText since that is likely what the user will edit.
                    // This will trigger the Speed EditText OnFocusChangeListener
                    // which will then tell the parent list that the row has been clicked
                    mSpeedEditText.requestFocus();
                }
            }
        }
    };

    // If the user clicks in the Speed or Init EditText, also tell the DragNDropListView that this row has been clicked on
    OnFocusChangeListener mSpeedAndInitEditTextOnFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                checkRowClick(v);
            }
        }
    };

    // If the user clicks on the Turn Mode Spinner, also tell the DragNDropListView that this row has been clicked on
    OnTouchListener mTurnModeSpinnerOnTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction() == MotionEvent.ACTION_UP){
                checkRowClick(v);
            }
            return false;
        }
    };

    private void checkRowClick(View v) {
        View rowView = (View) v.getParent();
        if (rowView != null) {
            DragNDropListView listParent = (DragNDropListView) getParent();
            if (listParent != null) {
                listParent.rowClicked(rowView, mShipInfo);
            }
        }
    }

    //**** Save Ship's new Ship Type to Ships Tables ***********************************************
    // A ship's Type should not change during the game.
    //      Assume that if the user is changing it they are correcting a mistake on initial entry.
    //      So just update the Ship Type in the Ships Table and call it good.

    AdapterView.OnItemSelectedListener mShipTypeSpinnerOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position >= 0 && position != mShipInfo.getType()) {
                mShipInfo.setType(position);
                saveShipType();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void saveShipType() {

        // AsyncTask to save ship's new type
        AsyncTask<Object, Object, Object> saveShipTypeTask = new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {

                // Get DatabaseConnector to interact with the SQLite database
                DatabaseConnector databaseConnector = new DatabaseConnector(getContext());

                databaseConnector.updateShipType(mShipInfo.getId(), mShipInfo.getType());

                return null;
            }
            @Override
            protected void onPostExecute(Object result) {
            }
        };

        // Save the player to the database using a separate thread
        saveShipTypeTask.execute((Object[]) null);
    }

    //**** Save Ship's new Turn Mode to Ships Tables ***********************************************
    // A ship's Turn Mode should not change during the game.
    //      Assume that if the user is changing it they are correcting a mistake on initial entry.
    //      So just update the Turn Mode in the Ships Table and call it good.

    AdapterView.OnItemSelectedListener mTurnModeSpinnerOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position >= 0 && position != mShipInfo.getInit()) {
                mShipInfo.setInit(position);
                saveShipInit();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    // Whenever the user changes the ship's init validated it immediately and save the changes
    TextWatcher mInitEditTextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            String initString = mInitEditText.getText().toString().trim();
            if (initString.length() > 0) {
                try {
                    int newInit = Integer.parseInt(initString);

                    // Don't save the init if it hasn't actually changed.
                    // That just causes unwanted History log entries.
                    if (newInit != mShipInfo.getInit()) {
                        mShipInfo.setInit(newInit);
                        saveShipInit();
                    }
                } catch (Exception e) {
                    // User may have typed - as start of entering a negative number
                }
            }
        }
    };

    private void saveShipInit() {

        // AsyncTask to save ship's new turn mode
        AsyncTask<Object, Object, Object> saveShipTurnModeTask = new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {

                // Get DatabaseConnector to interact with the SQLite database
                DatabaseConnector databaseConnector = new DatabaseConnector(getContext());

                databaseConnector.updateShipInit(mShipInfo.getId(), mShipInfo.getInit());

                return null;
            }
            @Override
            protected void onPostExecute(Object result) {
            }
        };

        // Save the player to the database using a separate thread
        saveShipTurnModeTask.execute((Object[]) null);
    }

    //**** Save Ship's new speed to both History and Ships Tables **********************************

    // Whenever the user changes the ship's speed validated it immediately and save the changes
    TextWatcher mSpeedEditTextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            String speedString = mSpeedEditText.getText().toString().trim();
            if (speedString.length() > 0) {
                try {
                    int newSpeed = Integer.parseInt(speedString);
                    if (mGameInfo.isSpeedValid(newSpeed)) {

                        // Don't save the speed if it hasn't actually changed.
                        // That just causes unwanted History log entries.
                        if (newSpeed != mShipInfo.getCurrentSpeed()) {
                            mShipInfo.setCurrentSpeed(newSpeed);
                            saveShipSpeed();
                        }

                    } else {
                        Toast errorMsg = Toast.makeText(getContext(), getResources().getString(R.string.message_unit_speed_error), Toast.LENGTH_SHORT);
                        errorMsg.show();

                        mSpeedEditText.setText("");
                    }
                } catch (Exception e) {
                    // User may have typed - as start of entering a negative number
                }
            }
        }
    };

    private void saveShipSpeed() {

        // AsyncTask to save ship's new speed
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
