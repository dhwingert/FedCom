package com.dhwingert.fedcom.hexgridmap;

/**
 * Created by David Wingert on 3/3/2015.
 */
public class HexGridPosition {

    // Ship position
    private long mSquareGridPosX;
    private long mSquareGridPosY;
    private int mHeading;

    private int mMoveSinceLastTurn = 100;
    private int mMoveSinceLastSlip = 100;

    public HexGridPosition(long x, long y, int heading) {
        mSquareGridPosX = x;
        mSquareGridPosY = y;
        mHeading = heading;
    }

    public long getSquareGridPosX() {
        return mSquareGridPosX;
    }
    public void setSquareGridPosX(long squareGridPosX) {
        mSquareGridPosX = squareGridPosX;
    }

    public long getSquareGridPosY() {
        return mSquareGridPosY;
    }
    public void setSquareGridPosY(long squareGridPosY) {
        mSquareGridPosY = squareGridPosY;
    }

    public int getHeading() {
        return mHeading;
    }
    public void setHeading(int heading) {
        mHeading = heading;
    }

    public int getMoveSinceLastTurn() { return mMoveSinceLastTurn; }
    public void setMoveSinceLastTurn(int mMoveSinceLastTurn) { this.mMoveSinceLastTurn = mMoveSinceLastTurn; }

    public int getMoveSinceLastSlip() { return mMoveSinceLastSlip; }
    public void setMoveSinceLastSlip(int mMoveSinceLastSlip) { this.mMoveSinceLastSlip = mMoveSinceLastSlip; }

    private void moveOneHex(int dir) {

        boolean evenX = ((mSquareGridPosX & 1) == 0);

        int offsetX = 0;
        int offsetY = 0;

        if (evenX) {
            // Even X have neighbors on rows 0 and 1 except direction 4
            // 6: 0,0   1: 1,0   2: 2,0
            // 5: 0,1      1,1   3: 2,1
            //          4: 1,2
            switch (dir) {
                case 1:
                    offsetX = 0;
                    offsetY = -1;
                    break;
                case 2:
                    offsetX = 1;
                    offsetY = -1;
                    break;
                case 3:
                    offsetX = 1;
                    offsetY = 0;
                    break;
                case 4:
                    offsetX = 0;
                    offsetY = 1;
                    break;
                case 5:
                    offsetX = -1;
                    offsetY = 0;
                    break;
                case 6:
                    offsetX = -1;
                    offsetY = -1;
                    break;
            }
        } else {
            // Odd X have neighbors on rows 1 amd 2 except direction 1
            //           1: 1,0
            //  6: 0,1      1,1   2: 2,1
            //  5: 0,2   4: 1,2   3: 2,2
            switch (dir) {
                case 1:
                    offsetX = 0;
                    offsetY = -1;
                    break;
                case 2:
                    offsetX = 1;
                    offsetY = 0;
                    break;
                case 3:
                    offsetX = 1;
                    offsetY = 1;
                    break;
                case 4:
                    offsetX = 0;
                    offsetY = 1;
                    break;
                case 5:
                    offsetX = -1;
                    offsetY = 1;
                    break;
                case 6:
                    offsetX = -1;
                    offsetY = 0;
                    break;
            }
        }

        mSquareGridPosX += offsetX;
        mSquareGridPosY += offsetY;
    }

    private int getHeadingRight(int heading) {
        int newHeading = heading + 1;
        if (newHeading > 6) {
            newHeading = 1;
        }
        return newHeading;
    }

    private int getHeadingLeft(int heading) {
        int newHeading = heading - 1;
        if (newHeading < 1) {
            newHeading = 6;
        }
        return newHeading;
    }

    private void setHeadingRightTurn() {
        mHeading = getHeadingRight(mHeading);
    }

    private void setHeadingLeftTurn() {
        mHeading = getHeadingLeft(mHeading);
    }

    //**********************************************************************************************
    //** NOTE: THESE ROUTINES ALSO UPDATE TURN AND SLIP MODE COUNTS
    //**********************************************************************************************
    public void moveLeftTurn() {
        setHeadingLeftTurn();
        moveOneHex(mHeading);

        // Turns reset both Turn and Slip move count to 1
        // (The ship just moved straight one hex after turning.)
        mMoveSinceLastTurn = 1;
        mMoveSinceLastSlip = 1;
    }

    public void moveRightTurn() {
        setHeadingRightTurn();
        moveOneHex(mHeading);

        // Turns reset both Turn and Slip move count to 1
        // (The ship just moved straight one hex after turning.)
        mMoveSinceLastTurn = 1;
        mMoveSinceLastSlip = 1;
    }

    public void moveLeftSideslip() {
        moveOneHex( getHeadingLeft(mHeading) );

        // Slip reset Slip move count to 0 and increments Turn move count
        // (It counts as moving straight as far Turn mode.)
        mMoveSinceLastTurn++;
        mMoveSinceLastSlip = 0;
    }

    public void moveRightSideslip() {
        moveOneHex( getHeadingRight(mHeading) );

        // Slip reset Slip move count to 0 and increments Turn move count
        // (It counts as moving straight as far Turn mode.)
        mMoveSinceLastTurn++;
        mMoveSinceLastSlip = 0;
    }

    public void moveStraight() {
        moveOneHex(mHeading);

        // Moving straight increments both Turn and Slip move count
        mMoveSinceLastTurn++;
        mMoveSinceLastSlip++;
    }
}
