package com.dhwingert.fedcom.movetracker;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.dhwingert.fedcom.database.DatabaseConnector;
import com.dhwingert.fedcom.database.DatabaseInfoFactory;
import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.R;
import com.dhwingert.fedcom.database.ShipInfo;

import java.util.ArrayList;

/**
 * Show the details for one Impulse in the History table.
 *
 * Created by David Wingert on 12/21/2014.
 */
public class ImpulseDetailsFragment extends Fragment {

    // Callback methods implemented by MainActivity
    public interface ImpulseDetailsFragmentListener {
        // Called when user decides to view the list of players in the game
        // Called when user decides to view the list of ships in the game
        // Called when user decides to view the history of the game so far
        // Called when user clicks the next impulse button
        public void onShowPlayers();
        public void onShowShips();
        public void onShowHistory();
        public void onNextImpulse(GameInfo gameInfo);
        public void onImpulseDetailSelected(ShipInfo shipInfo);
    }

    public static String FROM_NAME = "IDF";

    private ImpulseDetailsFragmentListener mListener;

    // ListView for ships that are moving this impulse
    private ListView mMovingShipsListView;
    private ArrayList<ShipInfo> mMovingShipsListData;
    private ImpulseDetailsAdapter mMovingShipsListAdapter;

    // ListView for ships that are paused (not moving) this impulse
    private ListView mPausedShipsListView;
    private ArrayList<ShipInfo> mPausedShipsListData;
    private ImpulseDetailsAdapter mPausedShipsListAdapter;

    private TextView mImpulseDetailsTitleTextView;
    private Button mNextImpulseButton;

    private ProgressDialog mProgressDialog;

    private GameInfo mGameInfo;

    // Set ImpulseDetailsFragmentListener when fragment is attached
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (ImpulseDetailsFragmentListener) activity;
    }

    // Remove ImpulseDetailsFragmentListener when fragment is detached
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
        View view = inflater.inflate(R.layout.fragment_impulse_details, container, false);

        // Save fragment across config changes
        // This fragment has menu items to display
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mImpulseDetailsTitleTextView = (TextView) view.findViewById(R.id.impulseDetailTitleTextView);

        // CREATE MOVING SHIPS LIST VIEW
        mMovingShipsListView = (ListView) view.findViewById(R.id.movingShipsListView);
        mMovingShipsListView.setOnItemClickListener(mMovingShipsListViewItemClick);
        mMovingShipsListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Set data for MovingShipsListView
        mMovingShipsListData = new ArrayList<ShipInfo>();
        mMovingShipsListAdapter = new ImpulseDetailsAdapter(getActivity(), mMovingShipsListData);
        mMovingShipsListView.setAdapter(mMovingShipsListAdapter);

        // Set list empty message for MovingShipsListView
        TextView movingShipsListEmptyText = (TextView) view.findViewById(R.id.impulseMovingShipsEmptyView);
        mMovingShipsListView.setEmptyView(movingShipsListEmptyText);

        // CREATE PAUSED (NOT MOVING) SHIPS LIST VIEW
        mPausedShipsListView = (ListView) view.findViewById(R.id.pausedShipsListView);
        mPausedShipsListView.setOnItemClickListener(mPausedShipsListViewItemClick);
        mPausedShipsListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Set data for PausedListView
        mPausedShipsListData = new ArrayList<ShipInfo>();
        mPausedShipsListAdapter = new ImpulseDetailsAdapter(getActivity(), mPausedShipsListData);
        mPausedShipsListView.setAdapter(mPausedShipsListAdapter);

        // Set list empty message for PausedListView
        TextView pausedShipsListEmptyText = (TextView) view.findViewById(R.id.impulsePausedShipsEmptyView);
        mPausedShipsListView.setEmptyView(pausedShipsListEmptyText);

        // CREATE NEXT IMPULSE BUTTON
        mNextImpulseButton = (Button) view.findViewById(R.id.nextImpulseButton);
        mNextImpulseButton.setOnClickListener(mNextImpulseButtonClicked);

        // To start with none of the buttons are visible until
        // we find out if there is a game in progress or not
        mNextImpulseButton.setVisibility(View.GONE);

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

            // The Moving and Paused ship lists need to know the current game info
            // to properly display what move each ship is on
            mMovingShipsListAdapter.setGameInfo(mGameInfo);
            mPausedShipsListAdapter.setGameInfo(mGameInfo);

            // Now that we know if there is a game in progress, make the buttons visible
            mNextImpulseButton.setVisibility(View.VISIBLE);

            // Now show the info for the current impulse
            updateDisplayToCurrentImpulse(false);
        }
    }

    // Advance to next impulse (or next turn if currently at last impulse)
    //      If impulse 0, return to planning.  User back out of planning back to ImpulseDetailsFragment
    View.OnClickListener mNextImpulseButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mGameInfo.getCurrentImpulse() > 0) {
                mListener.onNextImpulse(mGameInfo);
            } else {
                mListener.onShowShips();
            }
        }
    };

    // Responds to the user touching a player's name in the Moving Ships ListView
    AdapterView.OnItemClickListener mMovingShipsListViewItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // Pass selection to MainActivity
            mListener.onImpulseDetailSelected( ((ImpulseDetailsRowView) view).getBasicShipInfo() );
        }
    };

    // Responds to the user touching a player's name in the Paused Ships ListView
    AdapterView.OnItemClickListener mPausedShipsListViewItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // Pass selection to MainActivity
            mListener.onImpulseDetailSelected( ((ImpulseDetailsRowView) view).getBasicShipInfo() );
        }
    };

    // Performs database query outside GUI thread
    private class GetImpulseDetailsTask extends AsyncTask<Object, Object, Cursor> {
        DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

        // Open database and return Cursor for all players
        @Override
        protected Cursor doInBackground(Object... params) {
            databaseConnector.open();

            // Get list of ships sorted by Speed and Turn Mode
            // After they are retrieved we will figure out which ones move this Impulse
            return databaseConnector.getShipSortedForMoves(mGameInfo);
        }

        // Use the Cursor returned from the doInBackground method
        @Override
        protected void onPostExecute(Cursor result) {
            super.onPostExecute(result);

            mMovingShipsListAdapter.doingUpdate(true);
            mPausedShipsListAdapter.doingUpdate(true);

            mMovingShipsListData.clear();
            mPausedShipsListData.clear();

            // Use math to calculate which ships move this impulse and which don't
            DatabaseInfoFactory.calculationWhichShipsMoveThisImpulse(mMovingShipsListData, mPausedShipsListData, result, mGameInfo);

            result.close();

            // Put entries in the History table for all ships that are moving this impulse
            databaseConnector.storeShipsMoveInImpulse(mGameInfo, mMovingShipsListData);

            databaseConnector.close();

            mMovingShipsListAdapter.notifyDataSetChanged();
            mPausedShipsListAdapter.notifyDataSetChanged();
        }
    }

    // Save currently displayed info
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_impulse_details_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_players:
                mListener.onShowPlayers();
                return true;
            case R.id.action_ships:
                mListener.onShowShips();
                return true;
            case R.id.action_history:
                mListener.onShowHistory();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Called by Main Activity when the current impulse has changed
    // while the ImpulseDetailsFragment is on screen.
    public void updateDisplayToCurrentImpulse(boolean showProgress) {

        // Show a ProgressDialog spinner for a fraction of a second so the user knows something happened
        // when they clicked the button
        if (showProgress) {
            String title = getActivity().getResources().getString(R.string.spinner_title_phase_details);
            String message = getActivity().getResources().getString(R.string.spinner_msg_phase_details);
            mProgressDialog = ProgressDialog.show(getActivity(), title, message, true, false);
            new CountDownTimer(300, 300) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    mProgressDialog.dismiss();
                }
            }.start();
        }

        if (mGameInfo.getCurrentImpulse() == 0) {
            mNextImpulseButton.setText(getActivity().getResources().getString(R.string.button_resume_planning));
        } else if (mGameInfo.getCurrentImpulse() >= mGameInfo.getImpulsesPerTurn()) {
            mNextImpulseButton.setText(getActivity().getResources().getString(R.string.button_next_turn));
        } else {
            mNextImpulseButton.setText( getActivity().getResources().getString(R.string.button_next_phase) );
        }

        // Set title text indicating what turn it is
        String impulseChartTitle = getActivity().getResources().getString(R.string.fragment_phase_chart);
        mImpulseDetailsTitleTextView.setText(MoveTrackerMessages.getTurnLabelTitleNewGame(impulseChartTitle, mGameInfo, getActivity()));

        // Now retrieve data for both MovingShipsListView and PausedListView
        new GetImpulseDetailsTask().execute((Object[]) null);
    }

}
