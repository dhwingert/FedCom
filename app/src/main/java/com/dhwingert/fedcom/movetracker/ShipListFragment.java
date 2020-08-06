package com.dhwingert.fedcom.movetracker;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dhwingert.fedcom.database.DatabaseConnector;
import com.dhwingert.fedcom.database.DatabaseInfoFactory;
import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.R;
import com.dhwingert.fedcom.database.ShipInfo;

import java.util.ArrayList;

/**
 * Display all of the ships in the game for all player.
 *
 * Created by David Wingert on 12/6/2014.
 */
public class ShipListFragment extends Fragment {

    // Callback methods implemented by MainActivity
    public interface ShipListFragmentListener {
        // Called when the user clicks the Start Turn button
        // Called when the user clicks the Resume Turn button
        public void onStartTurn(GameInfo gameInfo);
        public void onReturnToImpulseDetails();
    }

    // Reference to MainActivity as a listener to this fragment
    private ShipListFragmentListener mListener;

    // This ListActivity's ListView
    // Adapter for ListView
    private ListView mShipListView;

    private ArrayList<ShipInfo> mShipListData;
    private ShipListAdapter mShipListAdapter;

    private TextView mShipListTitleTurnTextView;
    private Button mStartTurnButton;

    private GameInfo mGameInfo;

    // Set ShipListFragmentListener when fragment is attached
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (ShipListFragmentListener) activity;
    }

    // Remove ShipListFragmentListener when fragment is detached
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
        View view = inflater.inflate(R.layout.fragment_ship_list, container, false);

        // Save fragment across config changes
        // This fragment has menu items to display
        setRetainInstance(true);
        setHasOptionsMenu(true);

        // Set title text indicating what turn it is
        mShipListTitleTurnTextView = (TextView) view.findViewById(R.id.shipListTitleTextView);

        // Create ShipListView
        mShipListView = (ListView) view.findViewById(R.id.shipListView);
        mShipListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Get data array for ShipListView
        mShipListData = new ArrayList<ShipInfo>();
        mShipListAdapter = new ShipListAdapter(getActivity(), mShipListData);
        mShipListView.setAdapter(mShipListAdapter);

        // Set list empty message for ShipListView
        TextView shipListEmptyText = (TextView) view.findViewById(R.id.shipListEmptyView);
        mShipListView.setEmptyView(shipListEmptyText);

        // Setup Start Turn button
        mStartTurnButton = (Button) view.findViewById(R.id.startTurnButton);
        mStartTurnButton.setOnClickListener(mStartTurnButtonClicked);

        // To start with none of the buttons are visible until
        // we find out if there is a game in progress or not
        mStartTurnButton.setVisibility(View.GONE);

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

            // Tell the ship list what the maximum speed ships are allowed in this game
            mShipListAdapter.setGameInfo(mGameInfo);

            // Now that we know if there is a game in progress, make the buttons visible
            mStartTurnButton.setVisibility(View.VISIBLE);

            String resumeDefaultTitle = getActivity().getResources().getString(R.string.button_start_turn_1);
            mStartTurnButton.setText( MoveTrackerMessages.getStartGameWoPlanningLabelButton(resumeDefaultTitle, resumeDefaultTitle, mGameInfo, getActivity()) );

            String shipListTitle = getActivity().getResources().getString(R.string.fragment_unit_list);
            mShipListTitleTurnTextView.setText(MoveTrackerMessages.getTurnLabelTitleNewGame(shipListTitle, mGameInfo, getActivity()));

            // Fill data array for ShipListView
            new GetShipListTask().execute((Object[]) null);
        }
    }

    // Display the Current Impulse when the Start Turn button is clicked
    View.OnClickListener mStartTurnButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TURN 0 / IMPULSE 0 - Game hasn't started yet
            //      Check that all the ships in the game have valid speeds first
            //      If they do, start first turn
            if (mGameInfo.getCurrentTurn() == 0 && mGameInfo.getCurrentImpulse() == 0){
                boolean allShipsHaveValidSpeeds = true;
                int shipCount = 0;
                for (ShipInfo oneShip: mShipListData) {
                    shipCount++;
                    if (mGameInfo.isSpeedValid(oneShip.getCurrentSpeed()) == false) {
                        allShipsHaveValidSpeeds = false;
                    }
                }

                // If there are ships in the game and they all have valid speeds, go ahead and start the turn
                if (shipCount > 0 && allShipsHaveValidSpeeds) {
                    mListener.onStartTurn(mGameInfo);
                } else {
                    Toast errorMsg = Toast.makeText(v.getContext(), getResources().getString(R.string.message_start_turn_invalid_speeds), Toast.LENGTH_SHORT);
                    errorMsg.show();
                }
            }
            // TURN 1+ / IMPULSE 0 - Game has started, in planning stage for current turn
            //      Go ahead and start the turn
            else if (mGameInfo.getCurrentImpulse() == 0) {
                mListener.onStartTurn(mGameInfo);
            }
            // TURN 1+ / IMPULSE 1+ - Game has started and current turn is in progress
            //      Go to ImpulseDetails to show what is going on in current impulse
            else {
                mListener.onReturnToImpulseDetails();
            }
        }
    };

    // On fragment resume, us a GetShipListTask to load players
    @Override
    public void onResume() {
        super.onResume();

        // On resume restore the list of ships
        mShipListAdapter.notifyDataSetChanged();
    }

    // Performs database query outside GUI thread
    private class GetShipListTask extends AsyncTask<Object, Object, Cursor> {
        DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

        // Open database and return Cursor for all players
        @Override
        protected Cursor doInBackground(Object... params) {
            databaseConnector.open();
            return databaseConnector.getAllShips();
        }

        // Use the Cursor returned from the doInBackground method
        @Override
        protected void onPostExecute(Cursor result) {
            super.onPostExecute(result);

            mShipListAdapter.doingUpdate(true);

            mShipListData.clear();
            DatabaseInfoFactory.addShipInfoToArrayList(mShipListData, result);

            result.close();
            databaseConnector.close();

            mShipListAdapter.notifyDataSetChanged();
        }
    }

}
