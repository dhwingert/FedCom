package com.dhwingert.fedcom.database;

/**
 * Contains all the info for the Impulse Chart for one Player's ship.
 *
 * Created by David Wingert on 11/29/2014.
 */
public class ShipInfo extends BaseInfo {

    public static int INVALID_SPEED = -999;

    private int mCurrentSpeed = INVALID_SPEED;
    private int mType;
    private int mInit;
    private int mAddTurn;
    private int mAddImpulse;

    private long mPlayerId = -1;
    private String mPlayerName = "";

    public ShipInfo(long id, long playerId, String name, int type, int init, int addTurn, int addImpulse, int currentSpeed) {
        mId = id;
        mName = name;
        mType = type;
        mInit = init;
        mAddTurn = addTurn;
        mAddImpulse = addImpulse;
        mCurrentSpeed = currentSpeed;

        mPlayerId = playerId;
        mPlayerName = "";
    }

    public ShipInfo(long id, long playerId, int type, int init, int addTurn, int addImpulse, int currentSpeed, String name, String playerName) {
        mId = id;
        mName = name;
        mType = type;
        mInit = init;
        mAddTurn = addTurn;
        mAddImpulse = addImpulse;
        mCurrentSpeed = currentSpeed;

        mPlayerId = playerId;
        mPlayerName = playerName;
    }

    // Speed in the current turn and impulse
    public int getCurrentSpeed() { return mCurrentSpeed; }
    public void setCurrentSpeed(int currentSpeed) {
        this.mCurrentSpeed = currentSpeed;
    }

    // ID of the Player this ship belongs to
    public long getPlayerId() {
        return mPlayerId;
    }

    // Name of the Player this ship belongs to
    public String getPlayerName() {
        return mPlayerName;
    }

    // Is this a Ship, Shuttle, Drone, or Torpedo
    public int getType() {
        return mType;
    }
    public void setType(int type) {
        mType = type;
    }

    // What is its Init (or SFB Turn Mode)
    public int getInit() {
        return mInit;
    }
    public void setInit(int init) {
        mInit = init;
    }

    // What turn was this ship added?
    public int getAddTurn() {
        return mAddTurn;
    }

    // What impulse was this ship added?
    public int getAddImpulse() {
        return mAddImpulse;
    }

    // Does this ship move this impulse?
    public boolean doesShipMoveThisImpulse(GameInfo gameInfo) {
        boolean moves = false;

        // Movement only happens during the Impulses
        //      Impulse 0 of each turn is the planning stage - nothing moves
        if (gameInfo.getCurrentImpulse() >= 1 && gameInfo.getCurrentImpulse() <= gameInfo.getImpulsesPerTurn()) {

            // Ships, Shuttles, Drones, and Torpedoes do not move
            // in the turn they are launched (i.e. "added" to the game).
            //      Launch step happens after Move step in an Impulse
            if (gameInfo.getCurrentTurn() != this.getAddTurn() || gameInfo.getCurrentImpulse() != this.getAddImpulse()) {

                // Finally check if this ship moves in this impulse at its current speed.
                Integer moveAtImpulse = getMoveAtImpulse(gameInfo, gameInfo.getCurrentImpulse());
                moves = (moveAtImpulse != null && moveAtImpulse > 0);
            }
        }

        return moves;
    }

    // Get the number of the last move this ship made.
    //      It will be a number from 1 to Current Speed.
    //      The last move may not be in the current impulse.
    public int getLastMove(GameInfo gameInfo) {
        int lastMove = 0;

        if (gameInfo.getCurrentImpulse() >= 1 && gameInfo.getCurrentImpulse() <= gameInfo.getImpulsesPerTurn()) {

            Integer moveAtImpulse = getMoveAtImpulse(gameInfo, gameInfo.getCurrentImpulse());
            if (moveAtImpulse == null || moveAtImpulse == 0) {

                for (int impulse = gameInfo.getCurrentImpulse() - 1; impulse > 0; impulse--) {
                    moveAtImpulse = getMoveAtImpulse(gameInfo, impulse);
                    if (lastMove == 0 && moveAtImpulse != null && moveAtImpulse > 0) {
                        lastMove = moveAtImpulse;
                        break;
                    }
                }

            } else {
                lastMove = moveAtImpulse;
            }
        }

        return lastMove;
    }

    // Get the text to show on the Impulse Chart and in the History table
    // for this move.  It is in the form of {latest move}/{current speed}
    public String getMoveText(GameInfo gameInfo) {
        if (getCurrentSpeed() != INVALID_SPEED) {
            return Integer.toString(getLastMove(gameInfo)) + " / " + Integer.toString(getCurrentSpeed());
        } else {
            return "- / -";
        }
    }

    // Get the entry in the Impulse Chart for this ship for this impulse.
    //      The ship may or may not move.
    //      The move will be a number from 1 to Current Speed if the ship moved this impulse.
    private Integer getMoveAtImpulse(GameInfo gameInfo, int impulse) {

        int moveThisImpulse = getMovesSoFar(gameInfo, getCurrentSpeed(), impulse);
        int movePrevImpulse = getMovesSoFar(gameInfo, getCurrentSpeed(), impulse - 1);

        return (moveThisImpulse > movePrevImpulse) ? moveThisImpulse : null;
    }

    private int getMovesSoFar(GameInfo gameInfo, int speed, int impulse) {
        return (Math.abs(speed) * impulse) / gameInfo.getImpulsesPerTurn();
    }

    // Get basic info about this ship
    public ShipInfo getBasicShipInfo() {
        return new ShipInfo(getId(), getPlayerId(), getType(), getInit(), getAddTurn(), getAddImpulse(), getCurrentSpeed(), getName(), getPlayerName());
    }

}
