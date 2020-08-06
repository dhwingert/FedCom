package com.dhwingert.fedcom.movetracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;

import com.dhwingert.fedcom.database.DatabaseConnector;
import com.dhwingert.fedcom.database.DatabaseInfoFactory;
import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.R;

/**
 * Configure the settings to use for the new game.
 *
 * Created by David Wingert on 1/24/2015.
 */
public class ConfigGameFragment extends Fragment {

    // Callback methods implemented by MainActivity
    public interface ConfigGameListener {
        // Called when the user clicks the Resume Game button
        public void gameSettingsSelected(GameInfo gameInfo);
    }

    // Reference to MainActivity as a listener to this fragment
    private ConfigGameListener mListener;

    private Spinner mConfigGameTypeSpinner;
    private EditText mConfigGameNameEditText;
    private EditText mConfigImpulsesEditText;
    private CheckBox mConfigHasTempCheckBox;
    private EditText mConfigPermLabelEditText;
    private EditText mConfigTempLabelEditText;
    private CheckBox mConfigHasInitOrderCheckBox;
    private EditText mConfigInitiativeLabelEditText;
    private CheckBox mConfigInitIsNumCheckBox;
    private EditText mConfigInitiativeListEditText;
    private EditText mConfigDefaultInitEditText;

    private TableRow mConfigTempLabelRow;
    private TableRow mConfigInitLabelRow;
    private TableRow mConfigInitIsNumRow;
    private TableRow mConfigInitListRow1;
    private TableRow mConfigInitListRow2;
    private TableRow mConfigDefaultInitRow;

    private Spinner m1stMoveOrderSpinner;
    private Spinner m2ndMoveOrderSpinner;
    private Button mStartGameButton;

    private GameInfo mGameInfo;

    // Set AboutFragmentListener when fragment is attached
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (ConfigGameListener) activity;
    }

    // Remove AboutFragmentListener when fragment is detached
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // Called after View is created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate GUI and get references to its controls
        View view = inflater.inflate(R.layout.fragment_config_game, container, false);

        // Create the Game Type spinner
        mConfigGameTypeSpinner = (Spinner) view.findViewById(R.id.configGameTypeSpinner);
        ArrayAdapter<CharSequence> gameTypeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.game_type_array, R.layout.spinner_item_16);
        gameTypeAdapter.setDropDownViewResource(R.layout.spinner_item_drop);
        mConfigGameTypeSpinner.setAdapter(gameTypeAdapter);
        mConfigGameTypeSpinner.setOnItemSelectedListener(mConfigGameTypeSpinnerItemSelected);

        // Create the controls to edit the game config
        mConfigGameNameEditText = (EditText) view.findViewById(R.id.configGameNameEditText);
        mConfigImpulsesEditText = (EditText) view.findViewById(R.id.configImpulsesEditText);
        mConfigPermLabelEditText = (EditText) view.findViewById(R.id.configPermLabelEditText);
        mConfigTempLabelEditText = (EditText) view.findViewById(R.id.configTempLabelEditText);
        mConfigInitiativeLabelEditText = (EditText) view.findViewById(R.id.configInitiativeLabelEditText);
        mConfigInitiativeListEditText = (EditText) view.findViewById(R.id.configInitiativeListEditText);
        mConfigDefaultInitEditText = (EditText) view.findViewById(R.id.configDefaultInitEditText);

        // Create the Has Temp Units checkbox
        mConfigTempLabelRow = (TableRow) view.findViewById(R.id.configTempLabelRow);
        mConfigHasTempCheckBox = (CheckBox) view.findViewById(R.id.configHasTempCheckBox);
        mConfigHasTempCheckBox.setOnClickListener(mConfigHasTempCheckBoxClickListener);

        // Create the Has Initiative Order checkbox
        mConfigInitLabelRow = (TableRow) view.findViewById(R.id.configInitLabelRow);
        mConfigInitIsNumRow = (TableRow) view.findViewById(R.id.configInitIsNumRow);
        mConfigDefaultInitRow = (TableRow) view.findViewById(R.id.configDefaultInitRow);
        mConfigHasInitOrderCheckBox = (CheckBox) view.findViewById(R.id.configHasInitOrderCheckBox);
        mConfigHasInitOrderCheckBox.setOnClickListener(mConfigHasInitOrderCheckBoxClickListener);

        // Create the Initiative is Numeric checkbox
        mConfigInitListRow1 = (TableRow) view.findViewById(R.id.configInitListRow1);
        mConfigInitListRow2 = (TableRow) view.findViewById(R.id.configInitListRow2);
        mConfigInitIsNumCheckBox = (CheckBox) view.findViewById(R.id.configInitIsNumCheckBox);
        mConfigInitIsNumCheckBox.setOnClickListener(mConfigInitIsNumCheckBoxClickListener);

        // Setup Start Game button
        mStartGameButton = (Button) view.findViewById(R.id.configStartGameButton);
        mStartGameButton.setOnClickListener(mStartGameButtonClicked);

        // Create the First Move Order spinner
        m1stMoveOrderSpinner = (Spinner) view.findViewById(R.id.config1stMoveOrderSpinner);
        ArrayAdapter<CharSequence> firstMoveOrderAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.move_order_array, R.layout.spinner_item_16);
        firstMoveOrderAdapter.setDropDownViewResource(R.layout.spinner_item_drop);
        m1stMoveOrderSpinner.setAdapter(firstMoveOrderAdapter);

        // Create the Second Move Order spinner
        m2ndMoveOrderSpinner = (Spinner) view.findViewById(R.id.config2ndMoveOrderSpinner);
        ArrayAdapter<CharSequence> secondMoveOrderAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.move_order_array, R.layout.spinner_item_16);
        secondMoveOrderAdapter.setDropDownViewResource(R.layout.spinner_item_drop);
        m2ndMoveOrderSpinner.setAdapter(secondMoveOrderAdapter);

        // To start with none of the buttons are visible until
        // we find out if there is a game in progress or not
        mStartGameButton.setVisibility(View.GONE);

        // Set the default game to Star Fleet Battles.
        mConfigGameTypeSpinner.setSelection(3);

        // Get the current GameInfo to find out if there is a current game and what turn it is in.
        mGameInfo = null;
        new GetGameInfoTask().execute();

        return view;
    }

    // Fills the Game Info object at startup - Performs database query outside GUI thread
    private class GetGameInfoTask extends AsyncTask<Object, Object, Cursor> {

        DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

        // Open database and return Cursor for all game info
        @Override
        protected Cursor doInBackground(Object... params) {
            databaseConnector.open();
            return databaseConnector.getAllGameInfo();
        }

        // Use the Cursor returned from the doInBackground method
        @Override
        protected void onPostExecute(Cursor result) {
            super.onPostExecute(result);

            mGameInfo = DatabaseInfoFactory.createGameInfo(result);

            result.close();
            databaseConnector.close();

            // Set text on Start (or Resume) game button
            // Once we know whether starting a game or resuming, Start Game button is always visible.
            String startGame = getActivity().getResources().getString(R.string.turn_add_players);
            mStartGameButton.setText(MoveTrackerMessages.getStartGameWPlanningLabelButton(startGame, startGame, mGameInfo, getActivity()));
            mStartGameButton.setVisibility(View.VISIBLE);
        }
    }

    // Whenever the user switches the game type, set the default values for the new game type
    AdapterView.OnItemSelectedListener mConfigGameTypeSpinnerItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            // Get the default values for the specified Game Type and set them in the edit controls
            GameInfo defaults = GameInfo.getGameTypeDefaultSettings(position, getActivity().getResources());
            mConfigGameNameEditText.setText( defaults.getGameName() );
            mConfigImpulsesEditText.setText( Integer.toString(defaults.getImpulsesPerTurn()) );
            mConfigHasTempCheckBox.setChecked( defaults.isHasTemp() );
            mConfigPermLabelEditText.setText( defaults.getPermLabel() );
            mConfigTempLabelEditText.setText( defaults.getTempLabel() );
            mConfigHasInitOrderCheckBox.setChecked( defaults.isHasInit() );
            mConfigInitiativeLabelEditText.setText( defaults.getInitLabel() );
            mConfigInitIsNumCheckBox.setChecked( defaults.isInitIsNumber() );
            mConfigInitiativeListEditText.setText( defaults.getInitList() );
            mConfigDefaultInitEditText.setText( defaults.getDefaultInit() );
            m1stMoveOrderSpinner.setSelection( defaults.getMoveOrder1() );
            m2ndMoveOrderSpinner.setSelection( defaults.getMoveOrder2() );

            // Trigger click events so controls will be hidden/shown appropriate to checkbox settings
            mConfigHasTempCheckBox.callOnClick();
            mConfigHasInitOrderCheckBox.callOnClick();
            mConfigInitIsNumCheckBox.callOnClick();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    // User clicked the "Has Temp Units" checkbox
    View.OnClickListener mConfigHasTempCheckBoxClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int showRow = mConfigHasTempCheckBox.isChecked() ? View.VISIBLE : View.GONE;
            mConfigTempLabelRow.setVisibility(showRow);
        }
    };

    // User clicked the "Has Initiative Order" checkbox
    View.OnClickListener mConfigHasInitOrderCheckBoxClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int showRow = mConfigHasInitOrderCheckBox.isChecked() ? View.VISIBLE : View.GONE;
            mConfigInitLabelRow.setVisibility(showRow);
            mConfigInitIsNumRow.setVisibility(showRow);
            mConfigDefaultInitRow.setVisibility(showRow);

            // Trigger check of whether Initiative List is visible
            mConfigInitIsNumCheckBox.callOnClick();
        }
    };

    // User clicked the "Initiative is Numeric" checkbox
    View.OnClickListener mConfigInitIsNumCheckBoxClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Initiative List is only visible when
            //      a) There is an Initiative Order AND
            //      b) Initiative is not numeric
            int showRow = (mConfigHasInitOrderCheckBox.isChecked() && ! mConfigInitIsNumCheckBox.isChecked()) ? View.VISIBLE : View.GONE;
            mConfigInitListRow1.setVisibility(showRow);
            mConfigInitListRow2.setVisibility(showRow);
        }
    };

    // Verify the Game Config the user selected is valid.
    //      If it is, save it and go to PlayerListFragment to add players to the game.
    View.OnClickListener mStartGameButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveGameConfig();
        }
    };

    private void saveGameConfig() {

        // Get the values from the edit controls
        // Have mGameInfo validate the values and set itself from them if they are all valid.
        // It will return null if they were all valid, or a string containing the first error msg it encountered.
        String errorMsg = mGameInfo.setGameInfoIfValid(
            mConfigGameNameEditText.getText().toString().trim(),
            mConfigImpulsesEditText.getText().toString().trim(),
            mConfigPermLabelEditText.getText().toString().trim(),
            mConfigHasTempCheckBox.isChecked(),
            mConfigTempLabelEditText.getText().toString().trim(),
            mConfigHasInitOrderCheckBox.isChecked(),
            mConfigInitiativeLabelEditText.getText().toString().trim(),
            mConfigInitIsNumCheckBox.isChecked(),
            mConfigInitiativeListEditText.getText().toString().trim(),
            mConfigDefaultInitEditText.getText().toString().trim(),
            m1stMoveOrderSpinner.getSelectedItemPosition(),
            m2ndMoveOrderSpinner.getSelectedItemPosition(),
            getActivity().getResources()
        );

        // *** ALL CONFIG IS VALID
        if (errorMsg == null) {
            final DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

            // AsyncTask restarts game and notifies Listener
            AsyncTask<Object, Object, Object> saveGameInfoTask = new AsyncTask<Object, Object, Object>() {
                @Override
                protected Object doInBackground(Object... params) {
                    databaseConnector.storeGameConfig((GameInfo) params[0]);
                    return null;
                }

                @Override
                protected void onPostExecute(Object result) {
                    mListener.gameSettingsSelected(mGameInfo);
                }
            };

            saveGameInfoTask.execute(mGameInfo);

//            mListener.gameSettingsSelected(mGameInfo);
        }
        // *** ONE OR MORE CONFIG WAS INVALID
        else {
            final String errorMessage = errorMsg;
            DialogFragment errorSettingGameConfig = new DialogFragment(){
                @Override
                public Dialog onCreateDialog(Bundle savedInstantState) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(errorMessage);
                    builder.setPositiveButton(R.string.ok, null);
                    return builder.create();
                }
            };

            // Use FragmentManager to display the errorSaving DialogFragment
            errorSettingGameConfig.show(getFragmentManager(), "error configuring game");
        }
    }


    // Save currently displayed info
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    // Called when the PlayerListFragment resumes
    @Override
    public void onResume() {
        super.onResume();
    }

}
