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

import com.dhwingert.fedcom.database.DatabaseConnector;
import com.dhwingert.fedcom.database.DatabaseInfoFactory;
import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.database.HistoryInfo;
import com.dhwingert.fedcom.R;

import java.util.ArrayList;

/**
 * A filterable list of the activities that have happened
 * on all the impulses in the game.
 *
 * Created by David Wingert on 12/21/2014.
 */
public class HistoryListFragment extends Fragment {

    // Callback methods implemented by MainActivity
    public interface HistoryListFragmentListener {
        // Called when user clicks return button
        public void onReturnToImpulseChart();
    }

    private HistoryListFragmentListener mListener;

    // This ListActivity's ListView
    // Adapter for ListView
    private ListView mHistoryListView;

    private ArrayList<HistoryInfo> mHistoryListData;
    private HistoryListAdapter mHistoryListAdapter;

    private TextView mHistoryListTitleTextView;
    private Button mReturnButton;

    private GameInfo mGameInfo;

    // Set HistoryListFragmentListener to MainActivity when fragment is attached
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (HistoryListFragmentListener) activity;
    }

    // Remove HistoryListFragmentListener when fragment is detached
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
        View view = inflater.inflate(R.layout.fragment_history_list, container, false);

        // Save fragment across config changes
        // This fragment has menu items to display
        setRetainInstance(true);
        setHasOptionsMenu(true);

        // Set title text indicating what turn it is
        mHistoryListTitleTextView = (TextView) view.findViewById(R.id.historyListTitleTextView);

        // Create HistoryListView
        mHistoryListView = (ListView) view.findViewById(R.id.historyListView);
        mHistoryListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Set data for HistoryListView
        mHistoryListData = new ArrayList<HistoryInfo>();
        mHistoryListAdapter = new HistoryListAdapter(getActivity(), mHistoryListData);
        mHistoryListView.setAdapter(mHistoryListAdapter);

        // Set list empty message for HistoryListView
        TextView historyListEmptyText = (TextView) view.findViewById(R.id.historyListEmptyView);
        mHistoryListView.setEmptyView(historyListEmptyText);

        // Create return to impulse chart button
        mReturnButton = (Button) view.findViewById(R.id.historyReturnButton);
        mReturnButton.setOnClickListener(mReturnButtonClicked);

        // To start with none of the buttons are visible until
        // we find out if there is a game in progress or not
        mReturnButton.setVisibility(View.GONE);

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

            // Tell the history list the Game Info so it knows the labels for the unit types
            mHistoryListAdapter.setGameInfo(mGameInfo);

            // Now that we know if there is a game in progress, make the buttons visible
            mReturnButton.setVisibility(View.VISIBLE);

            String resumeDefaultTitle = getActivity().getResources().getString(R.string.button_start_turn_1);
            mReturnButton.setText( MoveTrackerMessages.getStartGameWoPlanningLabelButton(resumeDefaultTitle, resumeDefaultTitle, mGameInfo, getActivity()) );
            String historyTitle = getActivity().getResources().getString(R.string.fragment_history);
            mHistoryListTitleTextView.setText(MoveTrackerMessages.getTurnLabelTitleNewGame(historyTitle, mGameInfo, getActivity()));

            // Now retrieve data for HistoryListView
            new GetHistoryListTask().execute((Object[]) null);
        }
    }

    View.OnClickListener mReturnButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mListener.onReturnToImpulseChart();
        }
    };

    // Performs database query outside GUI thread
    private class GetHistoryListTask extends AsyncTask<Object, Object, Cursor> {
        DatabaseConnector databaseConnector = new DatabaseConnector(getActivity());

        // Open database and return Cursor for all players
        @Override
        protected Cursor doInBackground(Object... params) {
            databaseConnector.open();
            return databaseConnector.getAllHistory();
        }

        // Use the Cursor returned from the doInBackground method
        @Override
        protected void onPostExecute(Cursor result) {
            super.onPostExecute(result);

            mHistoryListAdapter.doingUpdate(true);

            mHistoryListData.clear();
            DatabaseInfoFactory.addHistoryInfoToArrayList(mHistoryListData, result);

            result.close();
            databaseConnector.close();

            mHistoryListAdapter.notifyDataSetChanged();
        }
    }

}
