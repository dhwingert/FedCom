package com.dhwingert.fedcom.movetracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dhwingert.fedcom.database.BaseInfo;
import com.dhwingert.fedcom.database.DatabaseConnector;
import com.dhwingert.fedcom.database.DatabaseInfoFactory;
import com.dhwingert.fedcom.util.DragNDropListView;
import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.database.PlayerInfo;
import com.dhwingert.fedcom.R;

import java.util.ArrayList;

/**
 * PlayerListFragment displays the list of players in the game.
 * This is the starting point for starting a new game.
 *
 * Created by David Wingert on 11/26/2014.
 */
public class PlayerListFragment extends Fragment implements DragNDropListView.OnItemDragNDropListener {

    // Callback methods implemented by MainActivity
    public interface PlayerListFragmentListener {
        // Called when user selects a player
        // Called when user decides to Start the game or next game turn
        // Called when user decides to resume the turn already in progress
        public void onPlayerSelected(long playerID, String playerName);
        public void onStartTurnPlanning(GameInfo gameInfo);
        public void onShowImpulseDetails();
    }

    private PlayerListFragmentListener mListener;

    // This ListActivity's ListView
    private DragNDropListView mPlayerListView;

    private ArrayList<PlayerInfo> mPlayerListData;
    private PlayerListAdapter mPlayerListAdapter;

    private EditText mPlayerNameEditText;
    private ImageButton mAddPlayerButton;
    private Button mStartGameButton;

    private GameInfo mGameInfo;

    // Set PlayerListFragmentListener when fragment is attached
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (PlayerListFragmentListener) activity;
    }

    // Remove PlayerListFragmentListener when fragment is detached
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // Called when View needs to be created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate GUI and get references to its controls
        View view = inflater.inflate(R.layout.fragment_player_list, container, false);

        // Save fragment across config changes
        setRetainInstance(true);

        // Create PlayerListView
        mPlayerListView = (DragNDropListView) view.findViewById(R.id.playersListView);
        mPlayerListView.setOnItemClickListener(mOnPlayerRowClickListener);
        mPlayerListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Add data array to PlayerListView
        mPlayerListData = new ArrayList<PlayerInfo>();
        mPlayerListAdapter = new PlayerListAdapter(getActivity(), mPlayerListData, R.id.movePlayerImageView);
        mPlayerListView.setDragNDropAdapter(mPlayerListAdapter);
        mPlayerListView.setOnItemDragNDropListener(this);

        // Set list empty message for PlayerListView
        TextView shipListEmptyText = (TextView) view.findViewById(R.id.playerListEmptyView);
        mPlayerListView.setEmptyView(shipListEmptyText);

        // Setup area to add new players to game
        //      If user clicks "Done" on their keyboard while typing the player's name
        //      that player is immediately added to the list.
        mPlayerNameEditText = (EditText) view.findViewById(R.id.addPlayerEditText);
        mPlayerNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addPlayerToList();
                }
                return false;
            }
        });
        mAddPlayerButton = (ImageButton) view.findViewById(R.id.addPlayerButton);
        mAddPlayerButton.setOnClickListener(mAddPlayerButtonClicked);

        // Setup Start Game button
        mStartGameButton = (Button) view.findViewById(R.id.playersStartGameButton);
        mStartGameButton.setOnClickListener(mStartGameButtonClicked);

        // To start with none of the buttons are visible until
        // we find out if there is a game in progress or not
        mStartGameButton.setVisibility(View.GONE);
        mAddPlayerButton.setVisibility(View.GONE);

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

            // Set GameInfo so Adapter can tell the rows what the perm and temp unit labels are
            mPlayerListAdapter.setGameInfo(mGameInfo);

            // Now that we know if there is a game in progress, make the buttons visible
            mStartGameButton.setVisibility(View.VISIBLE);
            mAddPlayerButton.setVisibility(View.VISIBLE);

            // Update PlayerListFragment display according to what turn and impulse it is
            updateGameTurn();

            // Load list of players
            updatePlayerList();

        }
    }

    // Add player when button clicked if name is not blank
    View.OnClickListener mAddPlayerButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addPlayerToList();
        }
    };

    private void addPlayerToList() {
        String playerName = mPlayerNameEditText.getText().toString().trim();
        if (playerName.length() != 0) {
            mPlayerNameEditText.setText("");

            PlayerInfo playerInfo = new PlayerInfo(-1, playerName);
            mPlayerListData.add(playerInfo);
            mPlayerListAdapter.notifyDataSetChanged();

            saveNewPlayer(playerInfo);
        }
        // required ship name is blank, so display error dialog
        else {
            DialogFragment errorAddingPlayer = new DialogFragment(){
                @Override
                public Dialog onCreateDialog(Bundle savedInstantState) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.message_add_player_error);
                    builder.setPositiveButton(R.string.ok, null);
                    return builder.create();
                }
            };

            // Use FragmentManager to display the errorSaving DialogFragment
            errorAddingPlayer.show(getFragmentManager(), "error adding player");
        }
    }

    // Display the Ship List when the Start Game button is clicked
    View.OnClickListener mStartGameButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TURN 0 / IMPULSE 0 - Game hasn't started yet
            //      Check if there are any players with ships in the game first
            //      If there are, go to ShipList to set the ship speeds for the first turn
            if (mGameInfo.getCurrentImpulse() == 0 && mGameInfo.getCurrentImpulse() == 0){
                int shipCount = 0;
                for (PlayerInfo onePlayer : mPlayerListData) {
                    shipCount += onePlayer.getPermCount();
                }

                // If there are ships in the game, go ahead and start the game
                if (shipCount > 0) {
                    mListener.onStartTurnPlanning(mGameInfo);
                } else {
                    Toast errorMsg = Toast.makeText(v.getContext(), getResources().getString(R.string.message_start_game_no_units), Toast.LENGTH_SHORT);
                    errorMsg.show();
                }
            }
            // TURN 1+ / IMPULSE 0 - Game has started, in planning stage for current turn
            //      Go ShipList to set the ship speeds for the current turn
            else if (mGameInfo.getCurrentImpulse() == 0) {
                mListener.onStartTurnPlanning(mGameInfo);
            }
            // TURN 1+ / IMPULSE 1+ - Game has started and current turn is in progress
            //      Go to ImpulseDetails to show what is going on in current impulse
            else {
                mListener.onShowImpulseDetails();
            }
        }
    };

    // Responds to the user touching a player's name in the ListView
    AdapterView.OnItemClickListener mOnPlayerRowClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // Pass selection to MainActivity
            String name = ((PlayerListRowView) view).getPlayerName();
            mListener.onPlayerSelected(id, name);

        }
    };

    // Called when the PlayerListFragment resumes
    @Override
    public void onResume() {
        super.onResume();

        // On resume restore the list of players
        updatePlayerList();
    }

    // Fills the Player List - Performs database query outside GUI thread
    private class GetPlayerListTask extends AsyncTask<Object, Object, Cursor> {

        DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

        // Open database and return Cursor for all players
        @Override
        protected Cursor doInBackground(Object... params) {
            databaseConnector.open();
            return databaseConnector.getAllPlayers();
        }

        // Use the Cursor returned from the doInBackground method
        @Override
        protected void onPostExecute(Cursor result) {
            super.onPostExecute(result);

            mPlayerListAdapter.doingUpdate(true);

            mPlayerListData.clear();
            DatabaseInfoFactory.addPlayerInfoToArrayList(mPlayerListData, result);

            result.close();
            databaseConnector.close();

            mPlayerListAdapter.notifyDataSetChanged();
        }
    }

    // Update Player list - called by MainActivity
    public void updatePlayerList() {
        new GetPlayerListTask().execute((Object[]) null);
    }

    // Update Game Turn - called by MainActivity
    public void updateGameTurn() {

        // Update Start Game button text appropriately for current turn and impulse
        String startGame = getActivity().getResources().getString(R.string.turn_start_game);
        mStartGameButton.setText(MoveTrackerMessages.getStartGameWPlanningLabelButton(startGame, startGame, mGameInfo, getActivity()));
    }

    //**** Save Player List Order after being rearranged *******************************************

    // Called when the user starts dragging a player to a new position in the list
    public void onItemDrag(DragNDropListView parent, View view, int position, long id) {

    }

    // Called after the player has been dropped to a new position in the list
    public void onItemDrop(DragNDropListView parent, View view, int startPosition, int endPosition, long id) {
        if (startPosition != endPosition) {
            savePlayerListOrder();
        }
    }

    private void savePlayerListOrder() {

            // AsyncTask to save player, then notify mListener
            AsyncTask<Object, Object, Object> savePlayerListTask = new AsyncTask<Object, Object, Object>() {

                @Override
                protected Object doInBackground(Object... params) {

                    // Get DatabaseConnector to interact with the SQLite database
                    DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

                    // Set each player's order to its current position in the list
                    //      Adapter may have them in different order than Array has them
                    //      if user has rearranged the players
                    for (int idx = 0; idx < mPlayerListAdapter.getCount(); idx++) {
                        PlayerInfo onePlayer = (PlayerInfo) mPlayerListAdapter.getItem(idx);
                        onePlayer.setOrder(idx + 1);
                    }

                    databaseConnector.updatePlayerListOrder(mPlayerListData);

                    return null;
                }
                @Override
                protected void onPostExecute(Object result) {

                    // Reload list of players
                    new GetPlayerListTask().execute((Object[]) null);
                }
            };

            // Save the player to the database using a separate thread
            savePlayerListTask.execute((Object[]) null);
    }

    //**** Save New Player just added to bottom of list ********************************************

    private void saveNewPlayer(final PlayerInfo playerInfo) {

        // AsyncTask to save player, then notify mListener
        AsyncTask<Object, Object, Object> saveNewPlayerTask = new AsyncTask<Object, Object, Object>() {

            @Override
            protected Object doInBackground(Object... params) {

                // Get DatabaseConnector to interact with the SQLite database
                DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

                long playerID = databaseConnector.insertPlayerName(mGameInfo, playerInfo.getName());
                playerInfo.setId(playerID);

                return null;
            }
            @Override
            protected void onPostExecute(Object result) {

                // Automatically select the player so the user can start entering ships
                mListener.onPlayerSelected(playerInfo.getId(), playerInfo.getName());
            }
        };

        // Save the player to the database using a separate thread
        saveNewPlayerTask.execute((Object[]) null);
    }

    @Override
    public void onRowClicked(View view, BaseInfo baseInfo) {
    }

}
