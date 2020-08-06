package com.dhwingert.fedcom;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.dhwingert.fedcom.combatsimulator.EnemyStatusFragment;
import com.dhwingert.fedcom.combatsimulator.EnergyAllocationFragment;
import com.dhwingert.fedcom.combatsimulator.NavigationFragment;
import com.dhwingert.fedcom.combatsimulator.ShipStatusFragment;
import com.dhwingert.fedcom.database.DatabaseConnector;
import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.database.ShipInfo;
import com.dhwingert.fedcom.movetracker.ConfigGameFragment;
import com.dhwingert.fedcom.movetracker.HistoryListFragment;
import com.dhwingert.fedcom.movetracker.ImpulseDetailsFragment;
import com.dhwingert.fedcom.movetracker.PlayerDetailsFragment;
import com.dhwingert.fedcom.movetracker.PlayerListFragment;
import com.dhwingert.fedcom.movetracker.ShipListFragment;


/**
 * SFB-32: Star Fleet Battles 32 Impulse Chart Utility
 *
 * MainActivity is the container that displays all of the individual Fragments
 * that make up this utility.
 *
 * Created by David Wingert on 11/28/2014.
 * dhwingert@gmail.com
 *
 * All rights to the actual Star Fleet Battles board game are owned
 * by Amarillo Design Bureau at http://www.starfleetgames.com/
 */
public class MainActivity extends Activity implements
        ConfigGameFragment.ConfigGameListener,
        PlayerListFragment.PlayerListFragmentListener,
        PlayerDetailsFragment.PlayerDetailsFragmentListener,
        ShipListFragment.ShipListFragmentListener,
        ImpulseDetailsFragment.ImpulseDetailsFragmentListener,
        HistoryListFragment.HistoryListFragmentListener,
        AboutFragment.AboutFragmentListener,
        NavigationFragment.NavigationFragmentListener,
        EnergyAllocationFragment.EnergyAllocationFragmentListener,
        ShipStatusFragment.ShipStatusFragmentListener,
        EnemyStatusFragment.EnemyStatusFragmentListener {

    // Keys for storing info in Bundle passed to a fragment
    public static final String BUNDLE_PLAYER_ID = "player_id";
    public static final String BUNDLE_PLAYER_NAME = "player_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // return if Activity is being restored, no need to recreate GUI
        if (savedInstanceState != null) {
            return;
        }

        // Determine if current orientation is portrait or landscape
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Landscape
        }
        else {
            // Portrait
        }

        // Start out by showing the AboutFragment.
        // Do NOT put it in the BackStack.  Hitting back from here exits the game
        AboutFragment aboutFragment = new AboutFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.fragmentContainer, aboutFragment);
        transaction.commit();
    }

    // Called when MainActivity resume
    @Override
    protected void onResume() {
        super.onResume();
    }

    //**** AboutFragment.AboutFragmentListener *****************************************************

    // Player clicked Resume button on AboutFragment
    @Override
    public void onAboutFragmentResumeGame(GameInfo gameInfo) {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {

            // Turn -1: There is no game at all, go to ConfigGameFragment
            if (gameInfo.getCurrentTurn() < 0) {
                onShowConfigGame();
            }
            // Turn 0: There is an existing game configuration but the game has not started
            //      Go to PlayerListFragment to add players and ships
            else if (gameInfo.getCurrentTurn() == 0) {
                onShowPlayers();
            }
            // Turn 1+: Game has started
            //      Impulse 0:  Turn planning
            //      Impulse 1+: Turn is in progress
            else if (gameInfo.getCurrentTurn() > 0) {
                if (gameInfo.getCurrentImpulse() == 0) {
                    onStartTurnPlanning(gameInfo);
                } else {
                    onShowImpulseDetails();
                }
            }

        }
    }

    // User deleted the current game, wants to start a new one
    @Override
    public void onDeleteGame() {

        final DatabaseConnector databaseConnector = new DatabaseConnector(this);

        // Set the current turn back to the beginning of the game
        // Display the PlayerListFragment

        // AsyncTask restarts game and notifies Listener
        AsyncTask<Object, Object, Object> deleteGameTask = new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                databaseConnector.restartGame();
                return null;
            }

            @Override
            protected void onPostExecute(Object result) {
                onShowConfigGame();
            }
        };

        deleteGameTask.execute();
    }

    // Called when user clicks the Goto Hex Map button
    @Override
    public void onAboutFragmentGotoHexMap() {
        onGotoNavigation();
    }

    private void onShowConfigGame() {
        ConfigGameFragment configGameFragment = new ConfigGameFragment();
        displayFragment(configGameFragment);

        // Hide the keyboard
        hideKeyboard();
    }

    private void onGotoNavigation() {
        NavigationFragment navigationFragment = new NavigationFragment();
        displayFragment(navigationFragment);

        // Hide the keyboard
        hideKeyboard();
    }

    //**** ConfigGameFragment.FragmentListener *****************************************************

    @Override
    public void gameSettingsSelected(GameInfo gameInfo) {

        // Put game in setup planning stage
        gameInfo.setCurrentMove(0, 0);

        final DatabaseConnector databaseConnector = new DatabaseConnector(this);

        // AsyncTask restarts game and notifies Listener
        AsyncTask<Object, Object, Object> startAddingPlayersTask = new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                databaseConnector.storeCurrentTurnAndImpulse((GameInfo) params[0]);
                return null;
            }

            @Override
            protected void onPostExecute(Object result) {

                // Pop the AboutFragment off the back stack if it was on screen
                getFragmentManager().popBackStack();

                // Now show the players list to add players and their ships
                onShowPlayers();
            }
        };

        startAddingPlayersTask.execute(gameInfo);
    }

    //**** PlayerListFragment.PlayerListFragmentListener *******************************************

    // Display DetailsFragment for selected player
    @Override
    public void onPlayerSelected(long playerID, String playerName) {
        displayPlayerDetails(playerID, playerName);
    }

    // Display the details for a Player
    private void displayPlayerDetails(long playerID, String playerName) {
        PlayerDetailsFragment playerDetailsFragment = new PlayerDetailsFragment();

        // Specify playerID as an argument to the DetailsFragment
        Bundle arguments = new Bundle();
        arguments.putLong(BUNDLE_PLAYER_ID, playerID);
        arguments.putString(BUNDLE_PLAYER_NAME, playerName);
        playerDetailsFragment.setArguments(arguments);

        displayFragment(playerDetailsFragment);
    }

    // Display the ShipsListFragment
    @Override
    public void onStartTurnPlanning(GameInfo gameInfo) {

        // If game hasn't started yet, move to turn 1 planning impulse
        // and then update the screen
        if (gameInfo.getCurrentTurn() == 0) {
            gameInfo.setCurrentMove(1, 0);
        }

        final DatabaseConnector databaseConnector = new DatabaseConnector(this);

        // AsyncTask restarts game and notifies Listener
        AsyncTask<Object, Object, Object> startTurnPlanningTask = new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                databaseConnector.storeCurrentTurnAndImpulse((GameInfo) params[0]);
                return null;
            }

            @Override
            protected void onPostExecute(Object result) {
                PlayerListFragment playerListFragment = getPlayerListFragment();
                if (playerListFragment != null) {
                    playerListFragment.updateGameTurn();
                }

                // Pop the PlayerDetailsFragment off the back stack if it was on screen
                getFragmentManager().popBackStack();

                // Now show the ship list to let the players set speeds
                onShowShips();
            }
        };

        startTurnPlanningTask.execute(gameInfo);
    }

    // Display ImpulseDetailsFragment for current impulse in progress
    @Override
    public void onShowImpulseDetails() {
        ImpulseDetailsFragment impulseDetailsFragment = new ImpulseDetailsFragment();
        displayFragment(impulseDetailsFragment);

        // Hide the keyboard since it is of no use in ImpulseDetailsFragment
        hideKeyboard();
    }

    //**** PlayerDetailsFragment.PlayerDetailsFragmentListener *************************************

    // Return to Player List when displayed player deleted
    @Override
    public void onPlayerDeleted() {

        // removes top of back stack
        getFragmentManager().popBackStack();

        // Update the Player list
        onPlayerChanged();
    }

    // Return to Player List when displayed player deleted
    @Override
    public void onPlayerChanged() {
        PlayerListFragment playerListFragment = getPlayerListFragment();
        if (playerListFragment != null) {
            playerListFragment.updatePlayerList();
        }
    }

    // User clicks return button in PlayerDetailsFragment
    //      Do the same thing that we do for the resume button on AboutFragment
    @Override
    public void onPlayerDetailsReturn(GameInfo gameInfo) {
        onAboutFragmentResumeGame(gameInfo);
    }

    //**** ShipListFragment.ShipListFragmentListener ***********************************************

    // Start the next turn by displaying the ImpulseDetailsFragment
    @Override
    public void onStartTurn(GameInfo gameInfo) {

        int currentTurn = gameInfo.getCurrentTurn();
        int currentImpulse = gameInfo.getCurrentImpulse();

        // Go to first impulse in current turn
        if (currentImpulse == 0) {
            currentImpulse = 1;
            gameInfo.setCurrentMove(currentTurn, currentImpulse);
        }

        final DatabaseConnector databaseConnector = new DatabaseConnector(this);

        // AsyncTask restarts game and notifies Listener
        AsyncTask<Object, Object, Object> startTurnTask = new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                databaseConnector.storeCurrentTurnAndImpulse((GameInfo) params[0]);
                return null;
            }

            @Override
            protected void onPostExecute(Object result) {
                // Player List Fragment may also be on screen (if tablet)
                // Tell it to update what game turn it is displaying
                PlayerListFragment playerListFragment = getPlayerListFragment();
                if (playerListFragment != null) {
                    playerListFragment.updateGameTurn();
                }

                // Take the ShipListFragment off of the BackStack first
                // Show the ImpulseDetailsFragment to start the turn
                getFragmentManager().popBackStack();
                onShowImpulseDetails();
            }
        };

        startTurnTask.execute(gameInfo);

    }

    // Display ImpulseDetailsFragment for current impulse in progress
    // Return to ImpulseDetailsFragment when user clicks button in ShipListFragment
    @Override
    public void onReturnToImpulseDetails() {
        onReturnToImpulseChart();
    }

    //**** ImpulseDetailsFragment.ImpulseDetailsFragmentListener ***********************************

    // Display HistoryListFragment to display the history of the game so far
    @Override
    public void onShowHistory() {
        HistoryListFragment historyListFragment = new HistoryListFragment();
        displayFragment(historyListFragment);

        // Hide the keyboard since it is of no use in HistoryListFragment
        hideKeyboard();
    }

    // Display ShipListFragment to display the history of the game so far
    @Override
    public void onShowShips() {
        ShipListFragment shipListFragment = new ShipListFragment();
        displayFragment(shipListFragment);
    }

    // Display PlayerListFragment to display a list of all the players in the game.
    @Override
    public void onShowPlayers() {

        // If the PlayerListFragment exists, just put it back on the screen
        // Otherwise make a new one and display it.
        PlayerListFragment playerListFragment = getPlayerListFragment();
        if (playerListFragment == null) {
            playerListFragment = new PlayerListFragment();
        }
        displayFragment(playerListFragment);
    }

    // Display details for selected Player/Ship
    @Override
    public void onImpulseDetailSelected(ShipInfo shipInfo) {
        PlayerDetailsFragment playerDetailsFragment = new PlayerDetailsFragment();

        // Specify playerID as an argument to the DetailsFragment
        Bundle arguments = new Bundle();
        arguments.putLong(BUNDLE_PLAYER_ID, shipInfo.getPlayerId());
        arguments.putString(BUNDLE_PLAYER_NAME, shipInfo.getPlayerName());
        playerDetailsFragment.setArguments(arguments);

        displayFragment(playerDetailsFragment);
    }

    // Advance to next impulse (or next turn if currently at impulse 32)
    @Override
    public void onNextImpulse(final GameInfo gameInfo) {

        int currentTurn = gameInfo.getCurrentTurn();
        int currentImpulse = gameInfo.getCurrentImpulse();

        // If currently at impulse 32, wrap around to planning stage of next impulse
        //      Otherwise just go to the next impulse
        if (currentImpulse == gameInfo.getImpulsesPerTurn()) {
            currentImpulse = 0;
            currentTurn++;
        } else {
            currentImpulse++;
        }

        gameInfo.setCurrentMove(currentTurn, currentImpulse);

        final DatabaseConnector databaseConnector = new DatabaseConnector(this);

        // AsyncTask restarts game and notifies Listener
        AsyncTask<Object, Object, Object> nextImpulseTask = new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                databaseConnector.storeCurrentTurnAndImpulse((GameInfo) params[0]);
                return null;
            }

            @Override
            protected void onPostExecute(Object result) {

                // If we are at the planning impulse, show the ShipListFragment
                //      So user can set the new ships speeds
                if (gameInfo.getCurrentImpulse() == 0) {

                    // Take the ImpulseDetailsFragment off of the BackStack first
                    // Then show the ShipListFragment
                    getFragmentManager().popBackStack();
                    displayFragment(new ShipListFragment());
                }
                // Otherwise, tell the ImpulseDetailsFragment to update to the next impulse
                else {
                    ImpulseDetailsFragment fragment = (ImpulseDetailsFragment) getFragmentManager().findFragmentById(R.id.fragmentContainer);
                    if (fragment != null) {
                        fragment.updateDisplayToCurrentImpulse(true);
                    }
                }

                // Player List Fragment may also be on screen (if tablet)
                // Tell it to update what game turn it is displaying
                PlayerListFragment playerListFragment = getPlayerListFragment();
                if (playerListFragment != null) {
                    playerListFragment.updateGameTurn();
                }
            }
        };

        nextImpulseTask.execute(gameInfo);
    }

    //**** HistoryListFragment.HistoryListFragmentListener *****************************************

    // Return to the Impulse Chart
    //      Pop the HistoryListFragment off the BackStack as we don't want the user
    //      to go back to it if they hit the back button on the ImpulseDetailsFragment.
    @Override
    public void onReturnToImpulseChart() {
        getFragmentManager().popBackStack();

        // Hide the keyboard since it is of no use in ImpulseDetailsFragment
        hideKeyboard();
    }

    //**** NavigationFragment.NavigationFragmentListener *******************************************
    @Override
    public void onEnergyAllocation() {
        EnergyAllocationFragment energyAllocationFragment = new EnergyAllocationFragment();
        displayFragment(energyAllocationFragment);

        // Hide the keyboard
        hideKeyboard();
    }

    @Override
    public void onShipStatus() {
        ShipStatusFragment shipStatusFragment = new ShipStatusFragment();
        displayFragment(shipStatusFragment);

        // Hide the keyboard
        hideKeyboard();
    }

    @Override
    public void onEnemyStatus() {
        EnemyStatusFragment enemyStatusFragment = new EnemyStatusFragment();
        displayFragment(enemyStatusFragment);

        // Hide the keyboard
        hideKeyboard();
    }

    //**** EnergyAllocationFragment.EnergyAllocationFragmentListener *******************************
    @Override
    public void onEnergyAllocationReturnToNavigation() {
        getFragmentManager().popBackStack();

        // Hide the keyboard since it is of no use in ImpulseDetailsFragment
        hideKeyboard();
    }

    //**** ShipStatusFragment.ShipStatusFragmentListener *******************************************
    @Override
    public void onShipStatusReturnToNavigation() {
        getFragmentManager().popBackStack();

        // Hide the keyboard since it is of no use in ImpulseDetailsFragment
        hideKeyboard();
    }

    //**** EnemyStatusFragment.EnemyStatusFragmentListener *******************************************
    @Override
    public void onEnemyStatusReturnToNavigation() {
        getFragmentManager().popBackStack();

        // Hide the keyboard since it is of no use in ImpulseDetailsFragment
        hideKeyboard();
    }

    //**** Shared methods for displaying a Fragment ************************************************

    // Use a FragmentTransaction to display the specified fragment
    private void displayFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Find the PlayerListFragment if it exists
    //      The PlayerListFragment is special because it is always on screen for large devices,
    //      but comes and goes like the other fragments on small devices.
    private PlayerListFragment getPlayerListFragment() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.fragmentContainer);
        
        if (fragment != null && fragment instanceof PlayerListFragment) {
            return (PlayerListFragment) fragment;
        } else {
            return null;
        }
    }

    // Hide the software keyboard
    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
