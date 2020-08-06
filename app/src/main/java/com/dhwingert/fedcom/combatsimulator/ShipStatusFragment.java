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
import com.dhwingert.fedcom.shipStatusDisplay.ShipSystemsDisplay;
import com.google.gson.Gson;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Show the current ship status for the player's ships in the game.
 *
 * Created by David Wingert on 12/15/2018.
 */
public class ShipStatusFragment extends Fragment {

    // Callback methods implemented by MainActivity
    public interface ShipStatusFragmentListener {
        // Called when the user clicks the Return to Navigation button
        public void onShipStatusReturnToNavigation();
    }

    // Reference to MainActivity as a listener to this fragment
    private ShipStatusFragmentListener mListener;

    private TextView mShipStatusTitleTextView;
    private Button mShipStatusReturnButton;

    // Set ShipStatusFragmentListener when fragment is attached
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (ShipStatusFragmentListener) activity;
    }

    // Remove ShipStatusFragmentListener when fragment is detached
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
        View view = inflater.inflate(R.layout.fragment_ship_status, container, false);

        // Save fragment across config changes
        // This fragment has menu items to display
        setRetainInstance(true);
//        setHasOptionsMenu(true);

        // Set title text indicating what turn it is
        mShipStatusTitleTextView = (TextView) view.findViewById(R.id.shipStatusdTitleTextView);
        String shipStatusTitle = getActivity().getResources().getString(R.string.fragment_ship_status);
        mShipStatusTitleTextView.setText(shipStatusTitle);

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
        mShipStatusReturnButton = (Button) view.findViewById(R.id.shipStatusReturnButton);
        mShipStatusReturnButton.setOnClickListener(mShipStatusReturnButtonClicked);

//        // To start with none of the buttons are visible until
//        // we find out if there is a game in progress or not
//        mStartTurnButton.setVisibility(View.GONE);
//
//        // Get the current GameInfo to find out if there is a current game and what turn it is in.
//        mGameInfo = null;
//        new GetGameInfoTask().execute();

        // Read in the JSON file containing the definition of a Federation Heavy Cruiser (147 pts)
        InputStream inputStream = this.getResources().openRawResource(R.raw.fed_heavy_cruiser_147pts);
        String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();
        Gson gson = new Gson();
        ShipSystemsDisplay shipSystemsDisplay = gson.fromJson(jsonString, ShipSystemsDisplay.class);

        return view;
    }

    // Return to Navigation when the Return Button is clicked
    View.OnClickListener mShipStatusReturnButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mListener.onShipStatusReturnToNavigation();
        }
    };

}
