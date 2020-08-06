package com.dhwingert.fedcom.movetracker;

import android.content.Context;

import com.dhwingert.fedcom.database.GameInfo;
import com.dhwingert.fedcom.R;

/**
 * Class to generate messages used in several places in app.
 *
 * Created by David Wingert on 12/21/2014.
 */
public class MoveTrackerMessages {

    // Generic method to get label to display current Turn and Impulse on TextViews, Buttons, etc.
    public static String getTurnLabelGeneric(String configGameLabel, String addPlayersLabel, String inGameImp0Label, String inGameMovingLabel, String planningLabel, GameInfo gameInfo, Context context) {

        // Default to having not even configured the game
        String turnLabel = configGameLabel;

        if (inGameImp0Label.length() > 0) { inGameImp0Label += " "; }
        if (inGameMovingLabel.length() > 0) { inGameMovingLabel += " "; }
        if (planningLabel.length() > 0) { planningLabel = " " + planningLabel; }

        // Turn 0 is the Add Players step
        if (gameInfo.getCurrentTurn() == 0) {
            turnLabel = addPlayersLabel;
        } else if (gameInfo.getCurrentTurn() > 0) {
            String turnPrefix = context.getResources().getString(R.string.turn_number_prefix) + " ";

            if (gameInfo.getCurrentImpulse() > 0) {
                String impulsePrefix = context.getResources().getString(R.string.turn_phase_prefix) + " ";

                turnLabel = inGameMovingLabel + turnPrefix + Integer.toString(gameInfo.getCurrentTurn()) + "  " + impulsePrefix + Integer.toString(gameInfo.getCurrentImpulse());
            } else {
                turnLabel = inGameImp0Label + turnPrefix + Integer.toString(gameInfo.getCurrentTurn()) + planningLabel;
            }
        }

        return  turnLabel;
    }

    public static String getTurnLabelTitleNewGame(String title, GameInfo gameInfo, Context context) {
        String newGameLabel = context.getResources().getString(R.string.turn_new_game);
        String addPlayersLabel = context.getResources().getString(R.string.turn_add_players);
        String planningLabel = context.getResources().getString(R.string.turn_planning_suffix);

        return title + " " + getTurnLabelGeneric(newGameLabel, addPlayersLabel, "", "", planningLabel, gameInfo, context);
    }

//DHW
//    public static String getTurnLabelTitleStartGame(String title, GameInfo gameInfo, Context context) {
//        String newGameLabel = context.getResources().getString(R.string.turn_start_game);
//        String addPlayersLabel = context.getResources().getString(R.string.turn_add_players);
//        String planningLabel = context.getResources().getString(R.string.turn_planning_suffix);
//
//        return title + " " + getTurnLabelGeneric(newGameLabel, addPlayersLabel, "", "", planningLabel, gameInfo, context);
//    }

    public static String getShipAddedLabel(int turn, int impulse, Context context) {
        GameInfo gameInfo = new GameInfo();
        gameInfo.setCurrentMove(turn, impulse);

        return getGameAddedLabel(gameInfo, context);
    }

    // Label for when unit was added to game
    //      "Add Players" stage and "Planning" stage are both labeled as "Start Game"
    public static String getGameAddedLabel(GameInfo gameInfo, Context context) {
        String title = context.getResources().getString(R.string.label_added);
        String newGameLabel = context.getResources().getString(R.string.turn_start_game);
        String planningLabel = context.getResources().getString(R.string.turn_planning_suffix);

        return title + " " + getTurnLabelGeneric(newGameLabel, newGameLabel, "", "", planningLabel, gameInfo, context);
    }

    public static String getStartGameWPlanningLabelButton(String startTitle, String addPlayersTitle, GameInfo gameInfo, Context context) {
        String startGameLabel = context.getResources().getString(R.string.turn_start_prefix);
        String resumeGameLabel = context.getResources().getString(R.string.turn_resume_prefix);
        String planningLabel = context.getResources().getString(R.string.turn_planning_suffix);
        return getTurnLabelGeneric(startTitle, addPlayersTitle, startGameLabel, resumeGameLabel, planningLabel, gameInfo, context);
    }

    public static String getStartGameWoPlanningLabelButton(String startTitle, String addPlayersTitle, GameInfo gameInfo, Context context) {
        String startGameLabel = context.getResources().getString(R.string.turn_start_prefix);
        String resumeGameLabel = context.getResources().getString(R.string.turn_resume_prefix);
        return getTurnLabelGeneric(startTitle, addPlayersTitle, startGameLabel, resumeGameLabel, "", gameInfo, context);
    }
}
