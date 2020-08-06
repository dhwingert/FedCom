package com.dhwingert.fedcom.combatsimulator;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dhwingert.fedcom.R;
import com.dhwingert.fedcom.shipStatusDisplay.ShipSystemsDisplay;
import com.google.gson.Gson;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Show the current energy allocation for one ship in the game.
 *
 * Created by David Wingert on 11/23/2018.
 */
public class EnergyAllocationFragment extends Fragment {

    // Callback methods implemented by MainActivity
    public interface EnergyAllocationFragmentListener {
        // Called when the user clicks the Return to Navigation button
        public void onEnergyAllocationReturnToNavigation();
    }

    // Reference to MainActivity as a listener to this fragment
    private EnergyAllocationFragmentListener mListener;

    private TextView mEnergyAllocationTitleTextView;
    private Button mEnergyAllocationReturnButton;

    // Individual power systems
    private EditText mWarpDestroyedEdit;
    private EditText mWarpRemainingEdit;

    private EditText mImpulseDestroyedEdit;
    private EditText mImpulseRemainingEdit;

    private EditText mReactorDestroyedEdit;
    private EditText mReactorRemainingEdit;

    private EditText mBatteryDestroyedEdit;
    private EditText mBatteryRemainingEdit;

    // Power Pool for the turn
    private EditText mPowerUsedEdit;
    private EditText mPowerUnusedEdit;

    // Set ShipListFragmentListener when fragment is attached
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (EnergyAllocationFragmentListener) activity;
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
        View view = inflater.inflate(R.layout.fragment_energy_allocation, container, false);

        // Save fragment across config changes
        // This fragment has menu items to display
        setRetainInstance(true);
//        setHasOptionsMenu(true);

        // Set title text indicating what turn it is
        mEnergyAllocationTitleTextView = (TextView) view.findViewById(R.id.energyAllocationTitleTextView);
        String energyAllocationTitle = getActivity().getResources().getString(R.string.fragment_energy_allocation);
        mEnergyAllocationTitleTextView.setText(energyAllocationTitle);

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

        // Get a reference to all the power display controls.
        mWarpDestroyedEdit = (EditText) view.findViewById(R.id.warpDestroyedEditText);
        mWarpRemainingEdit = (EditText) view.findViewById(R.id.warpRemainingEditText);

        mImpulseDestroyedEdit = (EditText) view.findViewById(R.id.impulseDestroyedEditText);
        mImpulseRemainingEdit = (EditText) view.findViewById(R.id.impulseRemainingEditText);

        mReactorDestroyedEdit = (EditText) view.findViewById(R.id.reactorDestroyedEditText);
        mReactorRemainingEdit = (EditText) view.findViewById(R.id.reactorRemainingEditText);

        mBatteryDestroyedEdit = (EditText) view.findViewById(R.id.batteryDestroyedEditText);
        mBatteryRemainingEdit = (EditText) view.findViewById(R.id.batteryRemainingEditText);

        mPowerUsedEdit = (EditText) view.findViewById(R.id.powerUsedEditText);
        mPowerUnusedEdit = (EditText) view.findViewById(R.id.powerUnusedEditText);

        // Setup Return To Navigation button
        mEnergyAllocationReturnButton = (Button) view.findViewById(R.id.energyAllocationReturnButton);
        mEnergyAllocationReturnButton.setOnClickListener(mEnergyAllocationReturnButtonClicked);

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

        mWarpDestroyedEdit.setText( Integer.toString( shipSystemsDisplay.getPower().getWarpPower().getDestroyedTotalCount() ) );
        mWarpRemainingEdit.setText( Integer.toString( shipSystemsDisplay.getPower().getWarpPower().getNotDestroyedTotalCount() ) );

        mImpulseDestroyedEdit.setText( Integer.toString( shipSystemsDisplay.getPower().getImpulsePower().getDestroyedTotalCount() ) );
        mImpulseRemainingEdit.setText( Integer.toString( shipSystemsDisplay.getPower().getImpulsePower().getNotDestroyedTotalCount() ) );

        mReactorDestroyedEdit.setText( Integer.toString( shipSystemsDisplay.getPower().getReactorPower().getDestroyedTotalCount() ) );
        mReactorRemainingEdit.setText( Integer.toString( shipSystemsDisplay.getPower().getReactorPower().getNotDestroyedTotalCount() ) );

        mBatteryDestroyedEdit.setText( Integer.toString( shipSystemsDisplay.getPower().getBatteryPower().getDestroyedTotalCount() ) );
        mBatteryRemainingEdit.setText( Integer.toString( shipSystemsDisplay.getPower().getBatteryPower().getNotDestroyedTotalCount() ) );

        mPowerUsedEdit.setText( Integer.toString( shipSystemsDisplay.getPower().getPowerPoolUsedCount() ) );
        mPowerUnusedEdit.setText( Integer.toString( shipSystemsDisplay.getPower().getPowerPoolUnusedCount() ) );

        return view;
    }

    // Return to Navigation when the Return Button is clicked
    View.OnClickListener mEnergyAllocationReturnButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mListener.onEnergyAllocationReturnToNavigation();
        }
    };

}
