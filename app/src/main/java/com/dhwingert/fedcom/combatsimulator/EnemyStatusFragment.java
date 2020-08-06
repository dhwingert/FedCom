package com.dhwingert.fedcom.combatsimulator;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dhwingert.fedcom.R;

/**
 * Show the current ship status for the enemy ships in the game.
 *
 * Created by David Wingert on 12/15/2018.
 */
public class EnemyStatusFragment extends Fragment {

    // Callback methods implemented by MainActivity
    public interface EnemyStatusFragmentListener {
        // Called when the user clicks the Return to Navigation button
        public void onEnemyStatusReturnToNavigation();
    }

    // Reference to MainActivity as a listener to this fragment
    private EnemyStatusFragmentListener mListener;

    private TextView mEnemyStatusTitleTextView;
    private Button mEnemyStatusReturnButton;

    // Set EnemyStatusFragmentListener when fragment is attached
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (EnemyStatusFragmentListener) activity;
    }

    // Remove EnemyStatusFragmentListener when fragment is detached
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
        View view = inflater.inflate(R.layout.fragment_enemy_status, container, false);

        // Save fragment across config changes
        // This fragment has menu items to display
        setRetainInstance(true);
//        setHasOptionsMenu(true);

        // Set title text indicating what turn it is
        mEnemyStatusTitleTextView = (TextView) view.findViewById(R.id.enemyStatusTitleTextView);
        String shipStatusTitle = getActivity().getResources().getString(R.string.fragment_enemy_status);
        mEnemyStatusTitleTextView.setText(shipStatusTitle);

//        // Create ShipListView
//        mShipListView = (ListView) view.findViewById(R.id.shipListView);
//        mShipListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//
//        // Get data array for ShipListView
//        mShipListData = new ArrayList<ShipInfo>();
//        mShipListAdapter = new ShipListAdapter(getActivity(), mShipListData);
//        mShipListView.setAdapter(mShipListAdapter);
//
//        // Set list empty message for ShipListView
//        TextView shipListEmptyText = (TextView) view.findViewById(R.id.shipListEmptyView);
//        mShipListView.setEmptyView(shipListEmptyText);

        // Setup Return To Navigation button
        mEnemyStatusReturnButton = (Button) view.findViewById(R.id.enemyStatusReturnButton);
        mEnemyStatusReturnButton.setOnClickListener(mEnemyStatusReturnButtonClicked);

//        // To start with none of the buttons are visible until
//        // we find out if there is a game in progress or not
//        mStartTurnButton.setVisibility(View.GONE);
//
//        // Get the current GameInfo to find out if there is a current game and what turn it is in.
//        mGameInfo = null;
//        new GetGameInfoTask().execute();

        return view;
    }

    // Return to Navigation when the Return Button is clicked
    View.OnClickListener mEnemyStatusReturnButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mListener.onEnemyStatusReturnToNavigation();
        }
    };

}
