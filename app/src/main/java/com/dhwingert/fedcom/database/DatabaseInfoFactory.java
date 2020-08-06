package com.dhwingert.fedcom.database;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by David Wingert on 1/2/2015.
 */
public class DatabaseInfoFactory {

    public static HistoryInfo createHistoryInfo(Cursor result) {
        HistoryInfo historyInfo = null;

        if (!result.isAfterLast()) {
            int historyIdIdx = result.getColumnIndex(DatabaseConnector.HISTORY_COL_ID);
            int turnIdx = result.getColumnIndex(DatabaseConnector.HISTORY_COL_TURN);
            int impulseIdx = result.getColumnIndex(DatabaseConnector.HISTORY_COL_IMPULSE);
            int playerIdIdx = result.getColumnIndex(DatabaseConnector.HISTORY_COL_PLAYER_ID);
            int shipIdIdx = result.getColumnIndex(DatabaseConnector.HISTORY_COL_SHIP_ID);
            int playerNameIdx = result.getColumnIndex(DatabaseConnector.SHIPS_COL_PLAYER_NAME);
            int shipTypeIdx = result.getColumnIndex(DatabaseConnector.SHIPS_COL_TYPE);
            int shipNameIdx = result.getColumnIndex(DatabaseConnector.SHIPS_COL_NAME);
            int actionKeyIdx = result.getColumnIndex(DatabaseConnector.HISTORY_COL_KEY);
            int actionValueIdx = result.getColumnIndex(DatabaseConnector.HISTORY_COL_VALUE);

            historyInfo = new HistoryInfo(
                    result.getLong(historyIdIdx),
                    result.getInt(turnIdx),
                    result.getInt(impulseIdx),
                    result.getLong(playerIdIdx),
                    result.getLong(shipIdIdx),
                    result.getInt(shipTypeIdx),
                    result.getString(playerNameIdx),
                    result.getString(shipNameIdx),
                    result.getString(actionKeyIdx),
                    result.getString(actionValueIdx)
            );
        }

        return historyInfo;
    }

    public static void addHistoryInfoToArrayList(ArrayList<HistoryInfo> historyInfos, Cursor result) {

        result.moveToFirst();
        while (!result.isAfterLast()) {
            HistoryInfo historyInfo = DatabaseInfoFactory.createHistoryInfo(result);
            if (historyInfo != null) {
                historyInfos.add(historyInfo);
            }

            result.moveToNext();
        }

    }

    public static PlayerInfo createPlayerInfo(Cursor result) {
        PlayerInfo playerInfo = null;

        if (!result.isAfterLast()) {
            int playerNameIdx = result.getColumnIndex(DatabaseConnector.PLAYERS_COL_NAME);
            int playerIdIdx = result.getColumnIndex(DatabaseConnector.PLAYERS_COL_ID);
            int playerPermCntIdx = result.getColumnIndex(DatabaseConnector.PLAYERS_COL_PERM_CNT);
            int playerTempCntIdx = result.getColumnIndex(DatabaseConnector.PLAYERS_COL_TEMP_CNT);

            playerInfo = new PlayerInfo(
                result.getLong(playerIdIdx),
                result.getString(playerNameIdx),
                result.getInt(playerPermCntIdx),
                result.getInt(playerTempCntIdx)
            );
        }

        return  playerInfo;
    }

    public static void addPlayerInfoToArrayList(ArrayList<PlayerInfo> playerInfos, Cursor result) {

        result.moveToFirst();
        while (!result.isAfterLast()) {
            PlayerInfo playerInfo = DatabaseInfoFactory.createPlayerInfo(result);
            if (playerInfo != null) {
                playerInfos.add(playerInfo);
            }

            result.moveToNext();
        }
    }

    public static void addShipInfoToArrayList(ArrayList<ShipInfo> shipList, Cursor result) {

        result.moveToFirst();
        while (!result.isAfterLast()) {
            ShipInfo newShip = DatabaseInfoFactory.createShipInfo(result);
            if (newShip != null) {
                shipList.add(newShip);
            }

            result.moveToNext();
        }
    }

    public static void addShipInfoToHashMap(HashMap<Long, ShipInfo> shipList, Cursor result) {

        result.moveToFirst();
        while (!result.isAfterLast()) {
            ShipInfo newShip = DatabaseInfoFactory.createShipInfo(result);
            if (newShip != null) {
                shipList.put(newShip.getId(), newShip);
            }

            result.moveToNext();
        }
    }

    public static ShipInfo createShipInfo(Cursor result) {
        ShipInfo shipInfo = null;

        if (!result.isAfterLast()) {
            int shipNameIdx = result.getColumnIndex(DatabaseConnector.SHIPS_COL_NAME);
            int shipIdIdx = result.getColumnIndex(DatabaseConnector.SHIPS_COL_ID);
            int playerIdIdx = result.getColumnIndex(DatabaseConnector.SHIPS_COL_PLAYER_ID);
            int playerNameIdx = result.getColumnIndex(DatabaseConnector.SHIPS_COL_PLAYER_NAME);
            int shipTypeIdx = result.getColumnIndex(DatabaseConnector.SHIPS_COL_TYPE);
            int turnModeIdx = result.getColumnIndex(DatabaseConnector.SHIPS_COL_TURN_MODE);
            int speedIdx = result.getColumnIndex(DatabaseConnector.SHIPS_COL_SPEED);
            int addTurnIdx = result.getColumnIndex(DatabaseConnector.SHIPS_COL_ADD_TURN);
            int addImpulseIdx = result.getColumnIndex(DatabaseConnector.SHIPS_COL_ADD_IMPULSE);

            shipInfo = new ShipInfo(
                    result.getLong(shipIdIdx),
                    result.getLong(playerIdIdx),
                    result.getInt(shipTypeIdx),
                    result.getInt(turnModeIdx),
                    result.getInt(addTurnIdx),
                    result.getInt(addImpulseIdx),
                    result.getInt(speedIdx),
                    result.getString(shipNameIdx),
                    (playerNameIdx >= 0) ? result.getString(playerNameIdx) : ""
            );
        }

        return shipInfo;
    }

    public static void calculationWhichShipsMoveThisImpulse(ArrayList<ShipInfo> movingShips, ArrayList<ShipInfo> pausedShips, Cursor result, GameInfo gameInfo) {

        result.moveToFirst();
        while (!result.isAfterLast()) {
            ShipInfo shipInfo = DatabaseInfoFactory.createShipInfo(result);

            // Add to appropriate list depending on whether the ship moves this impulse or not
            if (shipInfo != null) {
                if (shipInfo.doesShipMoveThisImpulse(gameInfo)) {
                    movingShips.add(shipInfo);
                } else {
                    pausedShips.add(shipInfo);
                }
            }

            result.moveToNext();
        }
    }

    public static GameInfo createGameInfo(Cursor result) {
        GameInfo gameInfo = new GameInfo();

        result.moveToFirst();

        if (!result.isAfterLast()) {
            int gameTypeIdx = result.getColumnIndex(DatabaseConnector.TYPE_COL_TYPE_VAL);
            int gameNameIdx = result.getColumnIndex(DatabaseConnector.TYPE_COL_NAME);
            int numImpulsesIdx = result.getColumnIndex(DatabaseConnector.TYPE_COL_IMPULSES);
            int permLblIdx = result.getColumnIndex(DatabaseConnector.TYPE_COL_PERM_LBL);
            int hasTempIdx = result.getColumnIndex(DatabaseConnector.TYPE_COL_HAS_TEMP);
            int tempLblIdx = result.getColumnIndex(DatabaseConnector.TYPE_COL_TEMP_LBL);
            int hasInitIdx = result.getColumnIndex(DatabaseConnector.TYPE_COL_HAS_INIT);
            int initLblIdx = result.getColumnIndex(DatabaseConnector.TYPE_COL_INIT_LBL);
            int initIsNumIdx = result.getColumnIndex(DatabaseConnector.TYPE_COL_INIT_IS_NUM);
            int initListIdx = result.getColumnIndex(DatabaseConnector.TYPE_COL_INIT_LIST);
            int defaultInitIdx = result.getColumnIndex(DatabaseConnector.TYPE_COL_DEFAULT_INIT);
            int moveOrder1Idx = result.getColumnIndex(DatabaseConnector.TYPE_COL_ORDER_1);
            int moveOrder2Idx = result.getColumnIndex(DatabaseConnector.TYPE_COL_ORDER_2);

            int curTurnIdx = result.getColumnIndex(DatabaseConnector.GAME_COL_CURRENT_TURN);
            int curImpulseIdx = result.getColumnIndex(DatabaseConnector.GAME_COL_CURRENT_IMPULSE);
            int typeKeyIdx = result.getColumnIndex(DatabaseConnector.GAME_COL_TYPE_KEY);

            int gameType = result.getInt(gameTypeIdx);
            if (gameType >= 0) {
                gameInfo.setGameType(gameType);
            }

            String gameName = result.getString(gameNameIdx);
            if (gameName != null && gameName.length() > 0) {
                gameInfo.setGameName(gameName);
            }

            int numImpulses = result.getInt(numImpulsesIdx);
            if (numImpulses >= 1) {
                gameInfo.setImpulsesPerTurn(numImpulses);
            }

            String permLbl = result.getString(permLblIdx);
            if (permLbl != null && permLbl.length() > 0) {
                gameInfo.setPermLabel(permLbl);
            }

            boolean hasTemp = (result.getInt(hasTempIdx) == 1);
            gameInfo.setHasTemp(hasTemp);

            String tempLbl = result.getString(tempLblIdx);
            if (tempLbl != null && tempLbl.length() > 0) {
                gameInfo.setTempLabel(tempLbl);
            }

            boolean hasInit = (result.getInt(hasInitIdx) == 1);
            gameInfo.setHasInit(hasInit);

            String initLbl = result.getString(initLblIdx);
            if (initLbl != null && initLbl.length() > 0) {
                gameInfo.setInitLabel(initLbl);
            }

            boolean initIsNum = (result.getInt(initIsNumIdx) == 1);
            gameInfo.setInitIsNumber(initIsNum);

            String initList = result.getString(initListIdx);
            if (initList != null && initList.length() > 0) {
                gameInfo.setInitList(initList);
            }

            String defaultInit = result.getString(defaultInitIdx);
            if (defaultInit != null && defaultInit.length() > 0) {
                gameInfo.setDefaultInit(defaultInit);
            }

            int moveOrder1 = result.getInt(moveOrder1Idx);
            if (moveOrder1 >= 0) {
                gameInfo.setMoveOrder1(moveOrder1);
            }

            int moveOrder2 = result.getInt(moveOrder2Idx);
            if (moveOrder2 >= 0) {
                gameInfo.setMoveOrder2(moveOrder2);
            }

            int currentTurn = result.getInt(curTurnIdx);
            if (currentTurn >= 0) {
                gameInfo.setCurrentTurn(currentTurn);
            }

            int currentImpulse = result.getInt(curImpulseIdx);
            if (currentImpulse >= 0) {
                gameInfo.setCurrentImpulse(currentImpulse);
            }

        }

        return gameInfo;
    }

}
