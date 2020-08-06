package com.dhwingert.fedcom.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Connector to Android SQLiteDatabase.
 * All info about current game is stored in this mDatabase
 *
 * Created by David Wingert on 11/28/2014.
 */
public class DatabaseConnector {

    // DATABASE NAME
    private static final String DATABASE_NAME = "Dave's Wargame Imp";

    // TABLE NAMES
    private static final String TABLE_GAME_TYPE = "game_type";
    private static final String TABLE_GAME_INFO = "game_info";
    private static final String TABLE_PLAYERS = "players";
    private static final String TABLE_SHIPS = "ships";
    private static final String TABLE_HISTORY = "history";

    // GAME TYPE TABLE COLUMNS
    public static final String TYPE_COL_ID = "_id";
    public static final String TYPE_COL_TYPE_VAL = "type";
    public static final String TYPE_COL_NAME = "name";
    public static final String TYPE_COL_IMPULSES = "impulses";
    public static final String TYPE_COL_PERM_LBL = "perm_lbl";
    public static final String TYPE_COL_HAS_TEMP = "has_temp";
    public static final String TYPE_COL_TEMP_LBL = "temp_lbl";
    public static final String TYPE_COL_HAS_INIT = "has_init";
    public static final String TYPE_COL_INIT_LBL = "init_lbl";
    public static final String TYPE_COL_DEFAULT_INIT = "default_init";
    public static final String TYPE_COL_ORDER_1 = "order_1";
    public static final String TYPE_COL_ORDER_2 = "order_2";
    public static final String TYPE_COL_INIT_IS_NUM = "init_is_num";
    public static final String TYPE_COL_INIT_LIST = "init_list";

    // GAME INFO TABLE COLUMNS
    public static final String GAME_COL_CURRENT_TURN = "turn";
    public static final String GAME_COL_CURRENT_IMPULSE = "impulse";
    public static final String GAME_COL_TYPE_KEY = "type_key";

    // PLAYERS TABLE COLUMNS
    public static final String PLAYERS_COL_ID = "_id";
    public static final String PLAYERS_COL_ACTIVE = "active";
    public static final String PLAYERS_COL_NAME = "name";
    public static final String PLAYERS_COL_ORDER = "position";
    // Fake columns created in SQL
    public static final String PLAYERS_COL_PERM_CNT = "permcnt";
    public static final String PLAYERS_COL_TEMP_CNT = "tempcnt";

    // SHIPS TABLE COLUMNS
    public static final String SHIPS_COL_ID = "_id";
    public static final String SHIPS_COL_PLAYER_ID = "playerid";
    public static final String SHIPS_COL_ORDER = "position";
    public static final String SHIPS_COL_NAME = "name";
    public static final String SHIPS_COL_TYPE = "type";
    public static final String SHIPS_COL_TURN_MODE = "turnmode";
    public static final String SHIPS_COL_SPEED = "speed";
    public static final String SHIPS_COL_ACTIVE = "active";
    public static final String SHIPS_COL_ADD_TURN = "addturn";
    public static final String SHIPS_COL_ADD_IMPULSE = "addimpulse";
    // Fake columns created in SQL
    public static final String SHIPS_COL_PLAYER_NAME = "playername";
    public static final String SHIPS_COL_PLAYER_ORDER = "playerposition";

    // HISTORY TABLE COLUMNS
    public static final String HISTORY_COL_ID = "_id";
    public static final String HISTORY_COL_SHIP_ID = "shipid";
    public static final String HISTORY_COL_PLAYER_ID = "playerid";
    public static final String HISTORY_COL_TURN = "turn";
    public static final String HISTORY_COL_IMPULSE = "impulse";
    public static final String HISTORY_COL_KEY = "key";
    public static final String HISTORY_COL_VALUE = "value";

    // GAME INFO KEYS
    public static final String GAME_INFO_KEY_CURRENT_TURN = "Current Turn";
    public static final String GAME_INFO_KEY_CURRENT_IMPULSE = "Current Impulse";

    // HISTORY ACTION KEYS
    public static final String HISTORY_KEY_NEW_SPEED = "Set Spd";
    public static final String HISTORY_KEY_MOVE = "Move";
    public static final String HISTORY_KEY_NEW_PLAYER = "Add Player";
    public static final String HISTORY_KEY_NEW_PERM = "Add Perm";
    public static final String HISTORY_KEY_NEW_TEMP = "Add Temp";
    public static final String HISTORY_KEY_REMOVED_PLAYER = "Removed Player";
    public static final String HISTORY_KEY_REMOVED_PERM = "Removed Perm";
    public static final String HISTORY_KEY_REMOVED_TEMP = "Removed Temp";

    // UNIT TYPE VALUES
    public static final int SHIP_TYPE_PERM_KEY = 0;  // Permanent unit (Ship, character, etc.)
    public static final int SHIP_TYPE_TEMP_KEY = 1;  // Temporary unit (Seeking weapon, summoned creature, etc.)

    //**** Create Table Queries ********************************************************************
    private static final String CREATE_GAME_TYPE_QUERY =
            "CREATE TABLE " + TABLE_GAME_TYPE + " (" +
                    TYPE_COL_ID + " integer primary key autoincrement, " +
                    TYPE_COL_TYPE_VAL + " integer, " +
                    TYPE_COL_NAME + " TEXT, " +
                    TYPE_COL_IMPULSES + " integer, " +
                    TYPE_COL_PERM_LBL + " TEXT, " +
                    TYPE_COL_HAS_TEMP + " boolean, " +
                    TYPE_COL_TEMP_LBL + " TEXT, " +
                    TYPE_COL_HAS_INIT + " boolean, " +
                    TYPE_COL_INIT_LBL + " TEXT, " +
                    TYPE_COL_INIT_IS_NUM + " boolean, " +
                    TYPE_COL_INIT_LIST + " TEXT, " +
                    TYPE_COL_DEFAULT_INIT + " TEXT, " +
                    TYPE_COL_ORDER_1 + " integer, " +
                    TYPE_COL_ORDER_2 + " integer" +
                    ");";

    private static final String CREATE_GAME_INFO_TABLE_QUERY =
            "CREATE TABLE " + TABLE_GAME_INFO + " (" +
                    GAME_COL_TYPE_KEY + " integer, " +
                    GAME_COL_CURRENT_TURN + " integer, " +
                    GAME_COL_CURRENT_IMPULSE + " integer" +
                    ");";

    private static final String CREATE_PLAYERS_TABLE_QUERY =
            "CREATE TABLE " + TABLE_PLAYERS + " (" +
                    PLAYERS_COL_ID + " integer primary key autoincrement, " +
                    PLAYERS_COL_NAME + " TEXT, " +
                    PLAYERS_COL_ACTIVE + " boolean, " +
                    PLAYERS_COL_ORDER + " integer" +
                    ");";

    private static final String CREATE_SHIPS_TABLE_QUERY =
            "CREATE TABLE " + TABLE_SHIPS + " (" +
                    SHIPS_COL_ID + " integer primary key autoincrement, " +
                    SHIPS_COL_NAME + " TEXT, " +
                    SHIPS_COL_PLAYER_ID + " integer, " +
                    SHIPS_COL_TYPE + " integer, " +
                    SHIPS_COL_TURN_MODE + " integer, " +
                    SHIPS_COL_SPEED + " integer, " +
                    SHIPS_COL_ACTIVE + " boolean, " +
                    SHIPS_COL_ADD_TURN + " integer, " +
                    SHIPS_COL_ADD_IMPULSE + " integer, " +
                    SHIPS_COL_ORDER + " integer" +
                    ");";

    private static final String CREATE_HISTORY_TABLE_QUERY =
            "CREATE TABLE " + TABLE_HISTORY + " (" +
                    HISTORY_COL_ID + " integer primary key autoincrement, " +
                    HISTORY_COL_SHIP_ID + " integer, " +
                    HISTORY_COL_PLAYER_ID + " integer, " +
                    HISTORY_COL_TURN + " integer, " +
                    HISTORY_COL_IMPULSE + " integer, " +
                    HISTORY_COL_KEY + " TEXT, " +
                    HISTORY_COL_VALUE + " TEXT" +
                    ");";

    //**** SQL Queries *****************************************************************************

    private static final String GAME_INFO_QUERY = "SELECT * FROM " + TABLE_GAME_INFO + " LEFT OUTER JOIN " + TABLE_GAME_TYPE;

    private static final String PLAYER_LIST_QUERY = "SELECT a." + PLAYERS_COL_ORDER + ", a." + PLAYERS_COL_ID + ", a." + PLAYERS_COL_NAME +
            ", SUM(CASE WHEN b." + SHIPS_COL_TYPE + " = 0 AND b." + SHIPS_COL_ACTIVE + " = 1 THEN 1 ELSE 0 END) AS " + PLAYERS_COL_PERM_CNT +
            ", SUM(CASE WHEN b." + SHIPS_COL_TYPE + " = 1 AND b." + SHIPS_COL_ACTIVE + " = 1 THEN 1 ELSE 0 END) AS " + PLAYERS_COL_TEMP_CNT +
            " FROM " + TABLE_PLAYERS + " a LEFT OUTER JOIN " + TABLE_SHIPS + " b" +
            " ON a." + PLAYERS_COL_ID + " = b." + SHIPS_COL_PLAYER_ID +
            " WHERE a." + PLAYERS_COL_ACTIVE + " = 1" +
            " GROUP BY a." + PLAYERS_COL_ORDER  +
            " ORDER BY a." + PLAYERS_COL_ORDER;

    private static final String MAX_PLAYER_ORDER_QUERY = "SELECT MAX(" + PLAYERS_COL_ORDER + ") as " + PLAYERS_COL_ORDER + " FROM " + TABLE_PLAYERS;

    private static final String SHIP_LIST_QUERY = "SELECT a.*, b." + PLAYERS_COL_NAME + " AS " + SHIPS_COL_PLAYER_NAME + ", b." + PLAYERS_COL_ORDER + " AS " + SHIPS_COL_PLAYER_ORDER +
            " FROM " + TABLE_SHIPS + " a LEFT OUTER JOIN " + TABLE_PLAYERS + " b" +
            " ON a." + SHIPS_COL_PLAYER_ID + " = b." + PLAYERS_COL_ID +
            " WHERE a." + SHIPS_COL_ACTIVE + " = 1" +
            " AND b." + PLAYERS_COL_ACTIVE + " = 1" +
            " ORDER BY b." + PLAYERS_COL_ORDER + ", " + SHIPS_COL_ORDER;

//    private static final String SHIP_MOVE_QUERY = "SELECT a.*, b." + PLAYERS_COL_NAME + " AS " + SHIPS_COL_PLAYER_NAME +
//            " FROM " + TABLE_SHIPS + " a" +
//            " LEFT OUTER JOIN " + TABLE_PLAYERS + " b ON a." + SHIPS_COL_PLAYER_ID + " = b." + PLAYERS_COL_ID +
//            " WHERE a." + SHIPS_COL_ACTIVE + " = 1" +
//            " AND b." + PLAYERS_COL_ACTIVE + " = 1" +
//            " ORDER BY ABS(a." + SHIPS_COL_SPEED + "), a." + SHIPS_COL_TURN_MODE + " DESC";

private static final String SHIP_MOVE_QUERY = "SELECT a.*, b." + PLAYERS_COL_NAME + " AS " + SHIPS_COL_PLAYER_NAME +
        " FROM " + TABLE_SHIPS + " a" +
        " LEFT OUTER JOIN " + TABLE_PLAYERS + " b ON a." + SHIPS_COL_PLAYER_ID + " = b." + PLAYERS_COL_ID +
        " WHERE a." + SHIPS_COL_ACTIVE + " = 1" +
        " AND b." + PLAYERS_COL_ACTIVE + " = 1";

    private static final String HISTORY_LIST_QUERY = "SELECT a.*," +
            " b." + PLAYERS_COL_NAME + " AS " + SHIPS_COL_PLAYER_NAME + ", b." + PLAYERS_COL_ORDER + " AS " + SHIPS_COL_PLAYER_ORDER + "," +
            " c." + SHIPS_COL_NAME + ", c." + SHIPS_COL_ORDER + ", c." + SHIPS_COL_TYPE +
            " FROM " + TABLE_HISTORY + " a" +
            " LEFT OUTER JOIN " + TABLE_PLAYERS + " b ON a." + HISTORY_COL_PLAYER_ID + " = b." + PLAYERS_COL_ID +
            " LEFT OUTER JOIN " + TABLE_SHIPS + " c ON a." + HISTORY_COL_SHIP_ID + " = c." + SHIPS_COL_ID +
            " ORDER BY a." + HISTORY_COL_TURN + " DESC, a." + HISTORY_COL_IMPULSE + " DESC, b." + PLAYERS_COL_ORDER + ", c." + SHIPS_COL_ORDER + ", a." + HISTORY_COL_ID + " DESC";

    // For interacting with the database
    private SQLiteDatabase mDatabase;

    // Creates the database
    private DatabaseOpenHelper mDatabaseOpenHelper;

    public DatabaseConnector(Context context) {
        mDatabaseOpenHelper = new DatabaseOpenHelper(context, DATABASE_NAME, null, 1);
    }

    // Open the database connection
    public void open() throws SQLException {
        mDatabase = mDatabaseOpenHelper.getWritableDatabase();
    }

    // Close the database connection
    public void close() {
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    //**** Restart Game ****************************************************************************

    // Deletes all data about this game and recreates the database as empty
    //      USED BY ASYNC TASK
    public void restartGame() {

        open();

        mDatabaseOpenHelper.recreateDB(mDatabase);

        close();
    }

    //**** Game Info *******************************************************************************

    // Returns a Cursor with all Game Info settings in the database
    //      USED BY ASYNC TASK
    //
    //      NOTE: Because a Cursor is returned, caller must open and close the database themselves.
    public Cursor getAllGameInfo() {

//DHW - For blitzing db during development only
//        mDatabaseOpenHelper.recreateDB(mDatabase);

        Cursor results;

        try {
            results = mDatabase.rawQuery(GAME_INFO_QUERY, new String[]{});
        } catch (Exception e) {
            mDatabaseOpenHelper.recreateDB(mDatabase);
            results = mDatabase.rawQuery(GAME_INFO_QUERY, new String[]{});
        }

        return results;
    }

    // Store the configuration for a new game
    public void storeGameConfig(GameInfo gameInfo) {

        open();

        // Clear out existing Game Type and Game Info
        //      There should only be one row in each table.

        mDatabase.delete(TABLE_GAME_TYPE, null, null);
        mDatabase.delete(TABLE_GAME_INFO, null, null);

        ContentValues gameTypeContent = new ContentValues();
        gameTypeContent.put(TYPE_COL_TYPE_VAL, gameInfo.getGameType());
        gameTypeContent.put(TYPE_COL_NAME, gameInfo.getGameName());
        gameTypeContent.put(TYPE_COL_IMPULSES, gameInfo.getImpulsesPerTurn());
        gameTypeContent.put(TYPE_COL_PERM_LBL, gameInfo.getPermLabel());
        gameTypeContent.put(TYPE_COL_HAS_TEMP, gameInfo.isHasTemp());
        gameTypeContent.put(TYPE_COL_TEMP_LBL, gameInfo.getTempLabel());
        gameTypeContent.put(TYPE_COL_HAS_INIT, gameInfo.isHasInit());
        gameTypeContent.put(TYPE_COL_INIT_LBL, gameInfo.getInitLabel());
        gameTypeContent.put(TYPE_COL_INIT_IS_NUM, gameInfo.isInitIsNumber());
        gameTypeContent.put(TYPE_COL_INIT_LIST, gameInfo.getInitList());
        gameTypeContent.put(TYPE_COL_DEFAULT_INIT, gameInfo.getDefaultInit());
        gameTypeContent.put(TYPE_COL_ORDER_1, gameInfo.getMoveOrder1());
        gameTypeContent.put(TYPE_COL_ORDER_2, gameInfo.getMoveOrder2());
        mDatabase.insert(TABLE_GAME_TYPE, null, gameTypeContent);

        ContentValues gameInfoContent = new ContentValues();
        gameInfoContent.put(GAME_COL_TYPE_KEY, gameInfo.getGameTypeKey());
        gameInfoContent.put(GAME_COL_CURRENT_TURN, gameInfo.getCurrentTurn());
        gameInfoContent.put(GAME_COL_CURRENT_IMPULSE, gameInfo.getCurrentImpulse());
        mDatabase.insert(TABLE_GAME_INFO, null, gameInfoContent);

        close();
    }

    // Store the current Turn and Impulse in the Game Info table
    public void storeCurrentTurnAndImpulse(GameInfo gameInfo) {

        open();

        // Check if there is already a Game Info entry for the key: current turn
        Cursor existingTurnCursor = mDatabase.query(TABLE_GAME_INFO, null, null, null, null, null, null);
        existingTurnCursor.moveToFirst();

        // Either insert a new entry into the Game Info table or update the existing one
        if (existingTurnCursor.isAfterLast()) {
            ContentValues insertContent = new ContentValues();
            insertContent.put(GAME_COL_TYPE_KEY, gameInfo.getGameType());
            insertContent.put(GAME_COL_CURRENT_TURN, gameInfo.getCurrentTurn());
            insertContent.put(GAME_COL_CURRENT_IMPULSE, gameInfo.getCurrentImpulse());
            mDatabase.insert(TABLE_GAME_INFO, null, insertContent);
        } else {
            ContentValues updateContent = new ContentValues();
            updateContent.put(GAME_COL_CURRENT_TURN, gameInfo.getCurrentTurn());
            updateContent.put(GAME_COL_CURRENT_IMPULSE, gameInfo.getCurrentImpulse());
            mDatabase.update(TABLE_GAME_INFO, updateContent, null, null);
        }

        close();
    }

    //**** Player List *****************************************************************************

    // Returns a Cursor with all player names in the database
    //      USED BY ASYNC TASK
    //
    //      NOTE: Because a Cursor is returned, caller must open and close the database themselves.
    public Cursor getAllPlayers() {
        return mDatabase.rawQuery(PLAYER_LIST_QUERY, new String[]{});
    }

    // Sets the order of the Players whenever they are rearranged
    public void updatePlayerListOrder(ArrayList<PlayerInfo> playerList) {

        open();

        // Loop through all the added/updated ships
        for (PlayerInfo onePlayer: playerList) {

            Long onePlayerId = onePlayer.getId();
            Long onePlayerOrder = onePlayer.getOrder();

            ContentValues playerContent = new ContentValues();
            playerContent.put(PLAYERS_COL_ORDER, onePlayerOrder);

            mDatabase.update(TABLE_PLAYERS, playerContent, PLAYERS_COL_ID + "=" + onePlayerId, null);
        }

        close();

    }

    //**** Individual Player ***********************************************************************

    // Inserts a new Player Name
    //      USED BY ASYNC TASK
    public long insertPlayerName(GameInfo gameInfo, String name) {

        open();

        long order = 1;
        Cursor maxOrderCursor = mDatabase.rawQuery(MAX_PLAYER_ORDER_QUERY, new String[]{});

        if (maxOrderCursor != null &&  maxOrderCursor.moveToFirst()) {
            order = maxOrderCursor.getLong( maxOrderCursor.getColumnIndex(PLAYERS_COL_ORDER) );
            order++;
        }

        ContentValues newPlayerContent = new ContentValues();
        newPlayerContent.put(PLAYERS_COL_NAME, name.trim());
        newPlayerContent.put(PLAYERS_COL_ORDER, order);
        newPlayerContent.put(PLAYERS_COL_ACTIVE, true);

        long playerID = mDatabase.insert(TABLE_PLAYERS, null, newPlayerContent);

        // Now log adding the player
        logInsertPlayer(gameInfo.getCurrentTurn(), gameInfo.getCurrentImpulse(), playerID, name);

        close();
        return playerID;
    }

    // Updates an existing Player in the database
    //      USED BY ASYNC TASK
    public void updatePlayerAndTheirShips(GameInfo gameInfo, long playerID, String playerName, ArrayList<ShipInfo> shipData) {

        open();


        // Get player's list of existing ships in table
        Cursor result = getOnePlayersShipList(playerID);

        HashMap<Long, ShipInfo> existingShips = new HashMap<Long, ShipInfo>();
        DatabaseInfoFactory.addShipInfoToHashMap(existingShips, result);

        // Loop through all the added/updated ships
        for (ShipInfo oneShip: shipData) {
            String oneShipName = oneShip.getName();

            if (oneShipName != null && oneShipName.trim().length() > 0) {
                Long oneShipID = oneShip.getId();

                ContentValues shipContent = new ContentValues();
                shipContent.put(SHIPS_COL_NAME, oneShipName.trim());
                shipContent.put(SHIPS_COL_ORDER, oneShip.getOrder());

                // If ship already exists in table
                //      Update it name and order - leave all other info alone
                if (oneShipID != null && oneShipID > -1 && existingShips.containsKey(oneShipID) == true) {
                    mDatabase.update(TABLE_SHIPS, shipContent, SHIPS_COL_ID + "=" + oneShipID, null);
                    existingShips.remove(oneShipID);
                }
                // Else ship does not exist in table
                //      Insert it as a new ship
                else {
                    shipContent.put(SHIPS_COL_PLAYER_ID, playerID);
                    shipContent.put(SHIPS_COL_TURN_MODE, oneShip.getInit());
                    shipContent.put(SHIPS_COL_TYPE, oneShip.getType());
                    shipContent.put(SHIPS_COL_SPEED, oneShip.getCurrentSpeed());
                    shipContent.put(SHIPS_COL_ACTIVE, true);
                    shipContent.put(SHIPS_COL_ADD_TURN, gameInfo.getCurrentTurn());
                    shipContent.put(SHIPS_COL_ADD_IMPULSE, gameInfo.getCurrentImpulse());
                    long newShipID = mDatabase.insert(TABLE_SHIPS, null, shipContent);

                    // Log adding a ship for this player
                    oneShip.setId(newShipID);
                    logInsertShip(gameInfo.getCurrentTurn(), gameInfo.getCurrentImpulse(), newShipID, oneShip);
                }
            }
        }

        // Delete any original ships that were not passed in updated list
        // User must have deleted them on edit screen
        for (Long deletedShipID : existingShips.keySet()) {

            // During pre-game planning actually delete the ship
            //      Don't bother to log it.  Assume the player didn't want that ship after all
            if (gameInfo.getCurrentTurn() == 0 && gameInfo.getCurrentTurn() == 0) {
                mDatabase.delete(TABLE_SHIPS, SHIPS_COL_ID + "=" + deletedShipID, null);
            }

            // Otherwise, ship deleted during game
            //      Just mark it inactive in the Ship's table
            //      That way we retain all of it's info for the History log
            //      Log that the ship was removed
            else {
                ContentValues editShip = new ContentValues();
                editShip.put(SHIPS_COL_ACTIVE, false);
                mDatabase.update(TABLE_SHIPS, editShip, SHIPS_COL_ID + "=" + deletedShipID, null);

                // Log that the ship has been removed from the game
                ShipInfo shipInfo = existingShips.get(deletedShipID);
                logRemovedShip(gameInfo.getCurrentTurn(), gameInfo.getCurrentImpulse(), deletedShipID, playerID, shipInfo.getType());
            }
        }

        // Finally, update the player's name
        ContentValues editPlayer = new ContentValues();
        editPlayer.put(PLAYERS_COL_NAME, playerName);
        mDatabase.update(TABLE_PLAYERS, editPlayer, PLAYERS_COL_ID + "=" + playerID, null);
        close();
    }

    // Return a Cursor containing specified player's list of ships
    //      USED BY ASYNC TASK
    //
    //      NOTE: Because a Cursor is returned, caller must open and close the database themselves.
    public Cursor getOnePlayersShipList(long id) {
        return mDatabase.query(TABLE_SHIPS, null, SHIPS_COL_PLAYER_ID + "=" + id + " AND " + SHIPS_COL_ACTIVE + " = 1", null, null, null, SHIPS_COL_ORDER);
    }

    // Delete the player specified by their ID.  Also deletes all of their ships and any entries in game history
    //      USED BY ASYNC TASK
    public void deletePlayerAndTheirShips(GameInfo gameInfo, long playerID) {
        open();

        // During pre-game planning actually delete the player and their ships
        //      Don't bother to log it.  Assume the player was removed because they are not in this game
        if (gameInfo.getCurrentTurn() == 0 && gameInfo.getCurrentImpulse() == 0) {
            mDatabase.delete(TABLE_PLAYERS, PLAYERS_COL_ID + "=" + playerID, null);
            mDatabase.delete(TABLE_SHIPS, SHIPS_COL_PLAYER_ID + "=" + playerID, null);
            mDatabase.delete(TABLE_HISTORY, HISTORY_COL_PLAYER_ID + "=" + playerID, null);
        }
        // During the game just mark the player and all of their ships as inactive
        else {
            // Mark the player as inactive
            ContentValues editPlayer = new ContentValues();
            editPlayer.put(PLAYERS_COL_ACTIVE, false);
            mDatabase.update(TABLE_SHIPS, editPlayer, PLAYERS_COL_ID + "=" + playerID, null);

            logRemovedPlayer(gameInfo.getCurrentTurn(), gameInfo.getCurrentImpulse(), playerID);

            // Mark of the player's ships as inactive
            Cursor result = getOnePlayersShipList(playerID);

            int shipIdIdx = result.getColumnIndex(DatabaseConnector.SHIPS_COL_ID);
            int shipTypeIdx = result.getColumnIndex(DatabaseConnector.SHIPS_COL_TYPE);

            result.moveToFirst();
            while ( !result.isAfterLast() ) {

                long shipID = result.getLong(shipIdIdx);
                int shipType = result.getInt(shipTypeIdx);

                // Mark the ship as inactive.  It will no longer show up
                // anywhere but in the History log
                ContentValues editShip = new ContentValues();
                editShip.put(SHIPS_COL_ACTIVE, false);
                mDatabase.update(TABLE_SHIPS, editShip, SHIPS_COL_ID + "=" + shipID, null);

                logRemovedShip(gameInfo.getCurrentTurn(), gameInfo.getCurrentImpulse(), shipID, playerID, shipType);
                result.moveToNext();
            }

        }

        close();
    }

    //**** All Ships List **************************************************************************

    // Returns a Cursor with all ship names in the database
    //      USED BY ASYNC TASK
    //
    //      NOTE: Because a Cursor is returned, caller must open and close the database themselves.
    public Cursor getAllShips() {

        return mDatabase.rawQuery(SHIP_LIST_QUERY, new String[]{});
    }

    // Returns a Cursor with all of the Ships sorted by the order they move in (Speed ASC, TM ASC)
    //      USED BY ASYNC TASK
    //
    //      NOTE: Because a Cursor is returned, caller must open and close the database themselves.
    public Cursor getShipSortedForMoves(GameInfo gameInfo) {
        String moveQuery = SHIP_MOVE_QUERY;

        // Order by depends on Game Info
        String orderBy1 = getMoveOrderSort(gameInfo.getMoveOrder1());
        String orderBy2 = getMoveOrderSort(gameInfo.getMoveOrder2());

        // Combine two order by clauses
        //      If they both have a value, need to comma separate them
        String orderByBoth = "";
        if (orderBy1.length() > 0 && orderBy2.length() > 0) {
            orderByBoth = orderBy1 + ", " + orderBy2;
        } else {
            orderByBoth = orderBy1 + orderBy2;
        }

        // If there was at least one order by clause, add it to sql
        if (orderByBoth.length() > 0) {
            moveQuery = moveQuery + " ORDER BY " + orderByBoth;
        }

        // Now do move query with game's sort order applied
        return mDatabase.rawQuery(moveQuery, new String[]{});
    }

    private String getMoveOrderSort(int moveOrder) {
        String orderBy = "";
        switch (moveOrder) {
            case 0: // Low Speed First
                orderBy = "ABS(a." + SHIPS_COL_SPEED + ")";
                break;
            case 1: // High Speed First
                orderBy = "ABS(a." + SHIPS_COL_SPEED + ") DESC";
                break;
            case 2: // Low Initiative First
                orderBy = "a." + SHIPS_COL_TURN_MODE;
                break;
            case 3: // High Initiative First
                orderBy = "a." + SHIPS_COL_TURN_MODE + " DESC";
                break;
        }
        return orderBy;
    }

    // Set the ship's new current speed in both the Ships table and the History table
    public void saveCurrentShipSpeed(long shipId, long playerId, int currentSpeed) {

        open();

        // First set the current speed in the Ships table
        ContentValues shipListContent = new ContentValues();
        shipListContent.put(SHIPS_COL_SPEED, currentSpeed);

        mDatabase.update(TABLE_SHIPS, shipListContent, SHIPS_COL_ID + "=" + shipId, null);

        // Get current turn and impulse
        int currentTurn = 0;
        int currentImpulse = 0;
        Cursor gameInfoCursor = mDatabase.rawQuery(GAME_INFO_QUERY, new String[]{});
        if (gameInfoCursor != null &&  gameInfoCursor.moveToFirst()) {
            currentTurn = gameInfoCursor.getInt( gameInfoCursor.getColumnIndex(GAME_COL_CURRENT_TURN) );
            currentImpulse = gameInfoCursor.getInt( gameInfoCursor.getColumnIndex(GAME_COL_CURRENT_IMPULSE) );
        }

        // Log new speed into the History table
        logShipSpeedChange(currentTurn, currentImpulse, shipId, playerId, currentSpeed);

        close();
    }

    // Update the ship's init (SFB turn mode).
    public void updateShipInit(long shipId, int shipInit) {

        open();

        // Update the ship's turn mode in the Ships table
        ContentValues shipListContent = new ContentValues();
        shipListContent.put(SHIPS_COL_TURN_MODE, shipInit);

        mDatabase.update(TABLE_SHIPS, shipListContent, SHIPS_COL_ID + "=" + shipId, null);

        close();
    }

    // Update the ship's type.
    //      A ship's Type should not change during the game.
    //      Assume that if the user is changing it they are correcting a mistake on initial entry.
    //      So just update the Type in the Ships Table and call it good.
    public void updateShipType(long shipId, int type) {

        open();

        // Update the ship's turn mode in the Ships table
        ContentValues shipListContent = new ContentValues();
        shipListContent.put(SHIPS_COL_TYPE, type);

        mDatabase.update(TABLE_SHIPS, shipListContent, SHIPS_COL_ID + "=" + shipId, null);

        close();
    }

    //**** History Table ***************************************************************************

    // Returns a Cursor with all History table entries in the database
    //      USED BY ASYNC TASK
    //
    //      NOTE: Because a Cursor is returned, caller must open and close the database themselves.
    public Cursor getAllHistory() {
        return mDatabase.rawQuery(HISTORY_LIST_QUERY, new String[]{});
    }

    public void storeShipsMoveInImpulse(GameInfo gameInfo, ArrayList<ShipInfo> shipInfos) {

        // Loop through the ships and put entries in the history table
        for (ShipInfo oneShipInfo : shipInfos) {

            // Only bother if the ship moves this impulse
            //      Ships that don't move in a given impulse are not logged in that impulse
            if (oneShipInfo.doesShipMoveThisImpulse(gameInfo)) {

                // Find out if the ship already has a "Move" entry in this turn/impulse
                String where =  HISTORY_COL_SHIP_ID + "=" + oneShipInfo.getId() + " AND " +
                        HISTORY_COL_PLAYER_ID + "=" + oneShipInfo.getPlayerId() + " AND " +
                        HISTORY_COL_TURN + "=" + gameInfo.getCurrentTurn() + " AND " +
                        HISTORY_COL_IMPULSE + "=" + gameInfo.getCurrentImpulse() + " AND " +
                        HISTORY_COL_KEY + "=\"" + HISTORY_KEY_MOVE + "\"";

                Cursor existingMoveCursor = mDatabase.query(TABLE_HISTORY, null, where, null, null, null, null);
                existingMoveCursor.moveToFirst();

                if (existingMoveCursor.isAfterLast()) {
                    ContentValues historyInsertContent = new ContentValues();
                    historyInsertContent.put(HISTORY_COL_SHIP_ID, oneShipInfo.getId());
                    historyInsertContent.put(HISTORY_COL_PLAYER_ID, oneShipInfo.getPlayerId());
                    historyInsertContent.put(HISTORY_COL_TURN, gameInfo.getCurrentTurn());
                    historyInsertContent.put(HISTORY_COL_IMPULSE, gameInfo.getCurrentImpulse());
                    historyInsertContent.put(HISTORY_COL_KEY, HISTORY_KEY_MOVE);
                    historyInsertContent.put(HISTORY_COL_VALUE, oneShipInfo.getMoveText(gameInfo));
                    mDatabase.insert(TABLE_HISTORY, null, historyInsertContent);
                } else {
                    ContentValues historyUpdateContent = new ContentValues();
                    historyUpdateContent.put(HISTORY_COL_VALUE, oneShipInfo.getMoveText(gameInfo));
                    mDatabase.update(TABLE_HISTORY, historyUpdateContent, where, null);
                }
            }
        }

    }

    //**** History Table logging helper methods ****************************************************

    private void logInsertPlayer(int currentTurn, int currentImpulse, long playerId, String playerName) {

        ContentValues historyInsertContent = new ContentValues();
        historyInsertContent.put(HISTORY_COL_SHIP_ID, -1);
        historyInsertContent.put(HISTORY_COL_PLAYER_ID, playerId);
        historyInsertContent.put(HISTORY_COL_TURN, currentTurn);
        historyInsertContent.put(HISTORY_COL_IMPULSE, currentImpulse);
        historyInsertContent.put(HISTORY_COL_KEY, HISTORY_KEY_NEW_PLAYER);
        historyInsertContent.put(HISTORY_COL_VALUE, playerName.trim());
        mDatabase.insert(TABLE_HISTORY, null, historyInsertContent);
    }

    private void logInsertShip(int currentTurn, int currentImpulse, long shipID, ShipInfo shipInfo) {

        String historyKey = "";
        switch (shipInfo.getType()) {
            case SHIP_TYPE_PERM_KEY:
                historyKey = HISTORY_KEY_NEW_PERM;
                break;
            case SHIP_TYPE_TEMP_KEY:
                historyKey = HISTORY_KEY_NEW_TEMP;
                break;
        }

        if (historyKey.length() > 0) {
            ContentValues historyInsertContent = new ContentValues();
            historyInsertContent.put(HISTORY_COL_SHIP_ID, shipID);
            historyInsertContent.put(HISTORY_COL_PLAYER_ID, shipInfo.getPlayerId());
            historyInsertContent.put(HISTORY_COL_TURN, currentTurn);
            historyInsertContent.put(HISTORY_COL_IMPULSE, currentImpulse);
            historyInsertContent.put(HISTORY_COL_KEY, historyKey);
            historyInsertContent.put(HISTORY_COL_VALUE, "");
            mDatabase.insert(TABLE_HISTORY, null, historyInsertContent);
        }

        // If the ship being added has a valid speed already
        //      Then log its initial speed when it was added.
        if (shipInfo.getCurrentSpeed() != ShipInfo.INVALID_SPEED) {
            logShipSpeedChange(currentTurn, currentImpulse, shipID, shipInfo.getPlayerId(), shipInfo.getCurrentSpeed());
        }
    }

    private void logRemovedShip(int currentTurn, int currentImpulse, long shipID, long playerID, int shipType) {

        String historyKey = "";
        switch (shipType) {
            case SHIP_TYPE_PERM_KEY:
                historyKey = HISTORY_KEY_REMOVED_PERM;
                break;
            case SHIP_TYPE_TEMP_KEY:
                historyKey = HISTORY_KEY_REMOVED_TEMP;
                break;
        }

        if (historyKey.length() > 0) {
            ContentValues historyInsertContent = new ContentValues();
            historyInsertContent.put(HISTORY_COL_SHIP_ID, shipID);
            historyInsertContent.put(HISTORY_COL_PLAYER_ID, playerID);
            historyInsertContent.put(HISTORY_COL_TURN, currentTurn);
            historyInsertContent.put(HISTORY_COL_IMPULSE, currentImpulse);
            historyInsertContent.put(HISTORY_COL_KEY, historyKey);
            historyInsertContent.put(HISTORY_COL_VALUE, "");
            mDatabase.insert(TABLE_HISTORY, null, historyInsertContent);
        }
    }

    private void logRemovedPlayer(int currentTurn, int currentImpulse, long playerID) {
        ContentValues historyInsertContent = new ContentValues();
        historyInsertContent.put(HISTORY_COL_SHIP_ID, -1);
        historyInsertContent.put(HISTORY_COL_PLAYER_ID, playerID);
        historyInsertContent.put(HISTORY_COL_TURN, currentTurn);
        historyInsertContent.put(HISTORY_COL_IMPULSE, currentImpulse);
        historyInsertContent.put(HISTORY_COL_KEY, HISTORY_KEY_REMOVED_PLAYER);
        historyInsertContent.put(HISTORY_COL_VALUE, "");
        mDatabase.insert(TABLE_HISTORY, null, historyInsertContent);
    }

    private void logShipSpeedChange(int currentTurn, int currentImpulse, long shipId, long playerId, int currentSpeed) {

        String where =  HISTORY_COL_SHIP_ID + "=" + shipId + " AND " +
                HISTORY_COL_PLAYER_ID + "=" + playerId + " AND " +
                HISTORY_COL_TURN + "=" + currentTurn + " AND " +
                HISTORY_COL_IMPULSE + "=" + currentImpulse + " AND " +
                HISTORY_COL_KEY + "=\"" + HISTORY_KEY_NEW_SPEED + "\"";

        // Check if there is already a Speed entry for this ship in this turn / impulse.
        Cursor existingSpeedCursor = mDatabase.query(TABLE_HISTORY, null, where, null, null, null, null);
        existingSpeedCursor.moveToFirst();

        // Either insert a new speed into the History table or update the existing one
        if (existingSpeedCursor.isAfterLast()) {
            ContentValues historyInsertContent = new ContentValues();
            historyInsertContent.put(HISTORY_COL_SHIP_ID, shipId);
            historyInsertContent.put(HISTORY_COL_PLAYER_ID, playerId);
            historyInsertContent.put(HISTORY_COL_TURN, currentTurn);
            historyInsertContent.put(HISTORY_COL_IMPULSE, currentImpulse);
            historyInsertContent.put(HISTORY_COL_KEY, HISTORY_KEY_NEW_SPEED);
            historyInsertContent.put(HISTORY_COL_VALUE, currentSpeed);
            mDatabase.insert(TABLE_HISTORY, null, historyInsertContent);
        } else {
            ContentValues historyUpdateContent = new ContentValues();
            historyUpdateContent.put(HISTORY_COL_VALUE, currentSpeed);
            mDatabase.update(TABLE_HISTORY, historyUpdateContent, where, null);
        }

    }

    //**** Supporting Methods **********************************************************************

    private class DatabaseOpenHelper extends SQLiteOpenHelper {
        public DatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // Creates the tables when the database is created
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_GAME_TYPE_QUERY);
            db.execSQL(CREATE_GAME_INFO_TABLE_QUERY);
            db.execSQL(CREATE_PLAYERS_TABLE_QUERY);
            db.execSQL(CREATE_SHIPS_TABLE_QUERY);
            db.execSQL(CREATE_HISTORY_TABLE_QUERY);
        }

        // On certain version upgrades the database may need to be upgraded
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            if (oldVersion != newVersion) {
                recreateDB(db);
            }
        }

        public void recreateDB(SQLiteDatabase db) {
            // on upgrade drop older tables
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAME_TYPE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAME_INFO);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHIPS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);

            // create new tables
            onCreate(db);
        }

    }
}
