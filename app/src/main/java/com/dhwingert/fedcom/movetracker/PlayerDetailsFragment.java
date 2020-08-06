package com.dhwingert.fedcom.movetracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dhwingert.fedcom.database.BaseInfo;
import com.dhwingert.fedcom.database.DatabaseConnector;
import com.dhwingert.fedcom.database.DatabaseInfoFactory;
import com.dhwingert.fedcom.util.DragNDropListView;
import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.MainActivity;
import com.dhwingert.fedcom.R;
import com.dhwingert.fedcom.database.ShipInfo;

import java.util.ArrayList;

/**
 * Displays the ship list for one Player.
 *
 * Created by David Wingert on 11/26/2014.
 */
public class PlayerDetailsFragment extends Fragment
        implements PlayerShipListAdapter.PlayerShipListAdapterListener, DragNDropListView.OnItemDragNDropListener {

    // Callback methods implemented by MainActivity
    public interface PlayerDetailsFragmentListener {
        // Called when a player is deleted
        // Called when a player is changed
        // Called when user clicks return button
        public void onPlayerDeleted();
        public void onPlayerChanged();
        public void onPlayerDetailsReturn(GameInfo gameInfo);
    }

    private PlayerDetailsFragmentListener mListener;

    // Selected Player's name and ID
    private long mPlayerID = -1;
    private String mPlayerName = "";

    // Controls to add a new ship to this player's list of ships
    private TextView mPlayerDetailTitleTextView;
    private EditText mUnitNameEditText;
    private Spinner mUnitTypeSpinner;
    private TextView mUnitInitLblTextView;
    private Spinner mUnitInitSpinner;
    private EditText mUnitInitEditText;
    private EditText mUnitSpeedEditText;
    private ImageButton mAddUnitButton;
    private TextView mCurrentTurnTextView;

    private DragNDropListView mUnitsListView;
    private ArrayList<ShipInfo> mUnitsListData;
    private PlayerShipListAdapter mPlayerUnitsListAdapter;

    private Button mReturnButton;

    private GameInfo mGameInfo;

    // Set PlayerDetailsFragmentListener to MainActivity when fragment is attached
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (PlayerDetailsFragmentListener) activity;
    }

    // Remove PlayerDetailsFragmentListener when fragment is detached
    @Override
    public void onDetach() {
        super.onDetach();
        mPlayerUnitsListAdapter.removePlayerShipListAdapterListener();
        mListener = null;
    }

    // Called when View needs to be created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // save fragment across config changes
        setRetainInstance(true);

        // If PlayerDetailsFragment is being restored, get saved info
        if (savedInstanceState != null) {
            mPlayerID = savedInstanceState.getLong(MainActivity.BUNDLE_PLAYER_ID);
            mPlayerName = savedInstanceState.getString(MainActivity.BUNDLE_PLAYER_NAME);
        } else {
            // Get Bundle of arguments then extract them
            Bundle arguments = getArguments();
            if (arguments != null) {
                mPlayerID = arguments.getLong(MainActivity.BUNDLE_PLAYER_ID);
                mPlayerName = arguments.getString(MainActivity.BUNDLE_PLAYER_NAME);
            }
        }

        // Inflate GUI and get references to its controls
        // This fragment has menu items to display
        View view = inflater.inflate(R.layout.fragment_player_details, container, false);
        setHasOptionsMenu(true);
        
        mPlayerDetailTitleTextView = (TextView) view.findViewById(R.id.playerDetailTitleTextView);
        mPlayerDetailTitleTextView.setText(getResources().getString(R.string.fragment_player_details) + " " + mPlayerName);

        // Setup area to add new ships to game
        //      If user clicks "Done" on their keyboard while typing the ship's name
        //      that ship is immediately added to the list.
        mUnitNameEditText = (EditText) view.findViewById(R.id.addUnitNameEditText);
        mUnitNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addShipToList();
                }
                return false;
            }
        });

        // Speed should select all text on focus so user can just type over it
        mUnitSpeedEditText = (EditText) view.findViewById(R.id.playerUnitSpeedEditText);
        mUnitSpeedEditText.setSelectAllOnFocus(true);

        mCurrentTurnTextView = (TextView) view.findViewById(R.id.playerDetailsTurnTextView);

        // Create the Unit Type spinner - We'll add the types once the Game Info is retrieved
        mUnitTypeSpinner = (Spinner) view.findViewById(R.id.playerUnitTypeSpinner);
        mUnitTypeSpinner.setOnItemSelectedListener(mShipTypeSpinnerItemSelected);

        // Create the Unit Init Spinner and Edit Text (one or the other is visible)
        mUnitInitEditText = (EditText) view.findViewById(R.id.playerUnitInitEditText);
        mUnitInitLblTextView = (TextView) view.findViewById(R.id.playerUnitInitLblTextView);
        mUnitInitSpinner = (Spinner) view.findViewById(R.id.playerUnitInitSpinner);

        mAddUnitButton = (ImageButton) view.findViewById(R.id.addShipButton);
        mAddUnitButton.setOnClickListener(mAddShipButtonClicked);

        // Create ShipListView
        mUnitsListView = (DragNDropListView) view.findViewById(R.id.unitsListView);
        mUnitsListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Set data for ShipListView
        mUnitsListData = new ArrayList<ShipInfo>();
        mPlayerUnitsListAdapter = new PlayerShipListAdapter(getActivity(), mUnitsListData, R.id.moveUnitButton);
        mPlayerUnitsListAdapter.addPlayerShipListAdapterListener(this);
        mUnitsListView.setDragNDropAdapter(mPlayerUnitsListAdapter);
        mUnitsListView.setOnItemDragNDropListener(this);

        // Set list empty text for ShipListView
        TextView shipListEmptyText = (TextView) view.findViewById(R.id.playerShipListEmptyView);
        mUnitsListView.setEmptyView(shipListEmptyText);

        // Create return to player list button
        mReturnButton = (Button) view.findViewById(R.id.playerDetailsReturnButton);
        mReturnButton.setOnClickListener(mReturnButtonClicked);

        // To start with none of the buttons are visible until
        // we find out if there is a game in progress or not
        mReturnButton.setVisibility(View.GONE);
        mAddUnitButton.setVisibility(View.GONE);

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

            // Set GameInfo so Adapter can tell the rows what the max speed in the game is
            mPlayerUnitsListAdapter.setGameInfo(mGameInfo);

            // Now that we know if there is a game in progress, make the buttons visible
            mReturnButton.setVisibility(View.VISIBLE);
            mAddUnitButton.setVisibility(View.VISIBLE);

            // Set the text on the Return button
            if (mGameInfo.getCurrentTurn() == 0 && mGameInfo.getCurrentImpulse() == 0) {
                mReturnButton.setText(getResources().getString(R.string.button_return_to_players));
            } else {
                String startGame = getActivity().getResources().getString(R.string.turn_start_game);
                mReturnButton.setText(MoveTrackerMessages.getStartGameWPlanningLabelButton(startGame, startGame, mGameInfo, getActivity()));
            }

            // Put the game labels for permanent units (and temp if the game has them)
            ArrayAdapter<String> unitTypeAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item_14, mGameInfo.getListOfUnitTypeLabels());
            unitTypeAdapter.setDropDownViewResource(R.layout.spinner_item_drop);
            mUnitTypeSpinner.setAdapter(unitTypeAdapter);

            // Unit Type spinner is only enabled if there are temp units.  (If not, spinner only has perm unit label in it anyway)
            mUnitTypeSpinner.setEnabled(mGameInfo.isHasTemp());
            mUnitTypeSpinner.setVisibility(View.VISIBLE);

            // Unit Init List Spinner or EditText (numeric or string Init)
            if (mGameInfo.isInitIsNumber()) {
                mUnitInitSpinner.setVisibility(View.GONE);
                mUnitInitEditText.setVisibility(View.VISIBLE);
                mUnitInitEditText.setText(mGameInfo.getDefaultInit());
            } else {
                mUnitInitEditText.setVisibility(View.GONE);
                mUnitInitSpinner.setVisibility(View.VISIBLE);

                ArrayAdapter<String> unitInitListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item_14, mGameInfo.getInitListSplit());
                unitInitListAdapter.setDropDownViewResource(R.layout.spinner_item_drop);
                mUnitInitSpinner.setAdapter(unitInitListAdapter);
                mUnitInitSpinner.setSelection(unitInitListAdapter.getPosition(mGameInfo.getDefaultInit()));
            }

            // Unit Init label
            mUnitInitLblTextView.setText(mGameInfo.getInitLabel() + ":");

            // Initial default speed is 0
            mUnitSpeedEditText.setText("0");

            mCurrentTurnTextView.setText(MoveTrackerMessages.getGameAddedLabel(mGameInfo, getActivity()));

            // Load player at mPlayerID
            new LoadPlayerDetailsTask().execute(mPlayerID);
        }
    }

    // Whenever the Ship Type is changed, set the init turn mode and speed
    AdapterView.OnItemSelectedListener mShipTypeSpinnerItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//            switch (position) {
//                case DatabaseConnector.SHIP_TYPE_PERM_KEY:
//                    if (mGameInfo.isInitIsNumber()) {
//                        mUnitInitEditText.setText("1");
//                    } else {
//                        mUnitInitSpinner.setSelection(DatabaseConnector.SHIP_TURN_KEY_5);
//                    }
//                    mUnitSpeedEditText.setText("0");
//                    break;
//                case DatabaseConnector.SHIP_TYPE_TEMP_KEY:
//                    if (mGameInfo.isInitIsNumber()) {
//                        mUnitInitEditText.setText("1");
//                    } else {
//                        mUnitInitSpinner.setSelection(DatabaseConnector.SHIP_TURN_KEY_2);
//                    }
//                    mUnitSpeedEditText.setText("0");
//                    break;
//            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    // Whenever the Add Ship button is clicked, check if a ship name was specified
    //      If a ship name was specified, add it to the end of the list and save the player
    View.OnClickListener mAddShipButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addShipToList();
        }
    };

    private void addShipToList() {
        String shipName = mUnitNameEditText.getText().toString().trim();
        if (shipName.length() != 0) {
            mUnitNameEditText.setText("");

            int shipType = mUnitTypeSpinner.getSelectedItemPosition();

            int shipInit = -1;
            if (mGameInfo.isInitIsNumber()) {
                String initString = mUnitInitEditText.getText().toString().trim();
                if (initString.length() > 0) {
                    shipInit = Integer.parseInt(initString);
                }
            } else {
                shipInit = mUnitInitSpinner.getSelectedItemPosition();
            }

            int speed = ShipInfo.INVALID_SPEED;
            String speedString = mUnitSpeedEditText.getText().toString().trim();
            if (speedString.length() > 0) {
                int newSpeed = Integer.parseInt(speedString);
                if (Math.abs(newSpeed) <= mGameInfo.getImpulsesPerTurn()) {
                    speed = newSpeed;
                }
            }

            ShipInfo newShipInfo = new ShipInfo(-1, mPlayerID, shipName, shipType, shipInit, mGameInfo.getCurrentTurn(), mGameInfo.getCurrentImpulse(), speed);

            mUnitsListData.add(newShipInfo);
            mPlayerUnitsListAdapter.notifyDataSetChanged();

            savePlayerShipList();
        }
        // required ship name is blank, so display error dialog
        else {
            DialogFragment errorAddingShip = new DialogFragment(){
                @Override
                public Dialog onCreateDialog(Bundle savedInstantState) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.message_add_unit_error);
                    builder.setPositiveButton(R.string.ok, null);
                    return builder.create();
                }
            };

            // Use FragmentManager to display the errorSaving DialogFragment
            errorAddingShip.show(getFragmentManager(), "error adding ship");
        }
    }

    // Whenever a row in the list is selected, show the Delete button on that row.
    private int mPreviousPosition = -1;
    @Override
    public void onRowClicked(View view, BaseInfo baseInfo) {
        int position = mPlayerUnitsListAdapter.getPosition(baseInfo);

        if (mPreviousPosition != -1) {
            View previousSelectedRowView = getViewByPosition(mPreviousPosition, mUnitsListView);
            if (previousSelectedRowView != null) {
                ImageButton previousRowDeleteButton = (ImageButton) previousSelectedRowView.findViewById(R.id.deleteUnitButton);
                previousRowDeleteButton.setVisibility(View.INVISIBLE);
            }
        }

        ImageButton currentRowDeleteButton = (ImageButton) view.findViewById(R.id.deleteUnitButton);
        currentRowDeleteButton.setVisibility(View.VISIBLE);

        mPreviousPosition = position;
    }

    // When user clicks the Return button, tell the MainActivity to return
    // to the appropriate location
    View.OnClickListener mReturnButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mListener.onPlayerDetailsReturn(mGameInfo);
        }
    };

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    // Called when the user starts dragging a ship to a new position in the list
    public void onItemDrag(DragNDropListView parent, View view, int position, long id) {

    }

    // Called after the ship has been dropped to a new position in the list
    public void onItemDrop(DragNDropListView parent, View view, int startPosition, int endPosition, long id) {
        if (startPosition != endPosition) {
            savePlayerShipList();
        }
    }

    // Called when a ship has been deleted from the list
    public void onShipDeleted() {
        savePlayerShipList();
    }

    // Called when the PlayerDetailsFragment resumes
    @Override
    public void onResume() {
        super.onResume();

        // On resume restore the list of ships
        mPlayerUnitsListAdapter.notifyDataSetChanged();
    }

    // Fills the Ship List - Performs database query outside GUI thread
    private class LoadPlayerDetailsTask extends AsyncTask<Long, Object, Cursor> {
        DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

        // Open database and return Cursor for specified player
        @Override
        protected Cursor doInBackground(Long... params) {
            databaseConnector.open();
            return databaseConnector.getOnePlayersShipList(params[0]);
        }

        // Use the Cursor returned from the doInBackground method
        @Override
        protected void onPostExecute(Cursor result) {
            super.onPostExecute(result);

            mPlayerUnitsListAdapter.doingUpdate(true);

            mUnitsListData.clear();
            DatabaseInfoFactory.addShipInfoToArrayList(mUnitsListData, result);

            result.close();
            databaseConnector.close();

            mPlayerUnitsListAdapter.notifyDataSetChanged();
        }
    }

    // Save currently displayed info
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(MainActivity.BUNDLE_PLAYER_ID, mPlayerID);
        outState.putString(MainActivity.BUNDLE_PLAYER_NAME, mPlayerName);
    }

    //**** Menu ************************************************************************************
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_player_details_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_delete:
                deletePlayer();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //**** Save Player Ship List *******************************************************************

    private void savePlayerShipList() {

        // AsyncTask to save player, then notify mListener
        AsyncTask<Object, Object, Object> savePlayerShipListTask = new AsyncTask<Object, Object, Object>() {

            @Override
            protected Object doInBackground(Object... params) {

                // Get DatabaseConnector to interact with the SQLite database
                DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

                // Set each ship's order to its current position in the Adapter
                //      Adapter may have them in different order than Array has them
                //      if user has rearranged the ships
                for (int idx = 0; idx < mPlayerUnitsListAdapter.getCount(); idx++) {
                    ShipInfo oneShip = (ShipInfo) mPlayerUnitsListAdapter.getItem(idx);
                    oneShip.setOrder(idx + 1);
                }

                databaseConnector.updatePlayerAndTheirShips(mGameInfo, mPlayerID, mPlayerName, mUnitsListData);

                return null;
            }
            @Override
            protected void onPostExecute(Object result) {

                // Reload player at mPlayerID
                new LoadPlayerDetailsTask().execute(mPlayerID);

                mListener.onPlayerChanged();
            }
        };

        // Save the player to the database using a separate thread
        savePlayerShipListTask.execute((Object[]) null);
    }

    //**** Delete Player ***************************************************************************

    // Menu Item Clicked: Delete a player
    private void deletePlayer() {
        // use FragmentManager to display the mConfirmDeletePlayer DialogFragment
        mConfirmDeletePlayer.show(getFragmentManager(), "confirm delete");
    }

    // DialogFragment to confirm deletion of player
    private DialogFragment mConfirmDeletePlayer = new DialogFragment() {

        // create an AlertDialog and return it
        @Override
        public Dialog onCreateDialog(Bundle bundle) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.confirm_delete_player_title);
            builder.setMessage(R.string.confirm_delete_player_message);

            // provide an OK button that simply dismisses the dialog
            builder.setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

                    // AsyncTask deletes player and notifies Listener
                    AsyncTask<Long, Object, Object> deleteTask = new AsyncTask<Long, Object, Object>() {
                        @Override
                        protected Object doInBackground(Long... params) {
                            databaseConnector.deletePlayerAndTheirShips(mGameInfo, params[0]);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Object result) {
                            mListener.onPlayerDeleted();
                        }
                    };

                    deleteTask.execute(new Long[] {mPlayerID});
                }
            });

            builder.setNegativeButton(R.string.button_cancel, null);
            return builder.create();
        }

    };

}
