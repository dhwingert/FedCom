package com.dhwingert.fedcom.database;

import android.content.res.Resources;

import com.dhwingert.fedcom.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by David Wingert on 1/23/2015.
 */
public class GameInfo {

    // From GAME TYPE table
    private int mGameType;
    private String mGameName;
    private int mImpulsesPerTurn;
    private String mPermLabel;
    private boolean mHasTemp;
    private String mTempLabel;
    private boolean mHasInit;
    private String mInitLabel;
    private boolean mInitIsNumber;
    private String mInitList;
    private String mDefaultInit;
    private int mMoveOrder1;
    private int mMoveOrder2;

    private List<String> mInitListSplit;

    // From GAME INFO table
    private int mGameTypeKey;
    private int mCurrentTurn;
    private int mCurrentImpulse;

    public GameInfo() {
        mGameType = 3; // SFB
        mGameName = "Star Fleet Battles";
        mImpulsesPerTurn = 32;
        mPermLabel = "Ship";
        mHasTemp = true;
        mTempLabel = "Seek";
        mHasInit = true;
        mInitLabel = "Mode";
        mInitIsNumber = false;
        mInitList = "Seek,Shtl,AA,A,B,C,D,E,F";
        mDefaultInit = "D";
        mMoveOrder1 = 0;
        mMoveOrder2 = 3;

        // Default to -1, game not created yet
        mCurrentTurn = -1;
        mCurrentImpulse = -1;

        // Split Init List for convenience
        mInitListSplit = splitInitLabels(mInitList);
    }

    //****  GAME TYPE TABLE ************************************************************************

    // Game Type (0: Custom, 1: Champions, 2: d20, 3: SFB, 4: FC)
    public int getGameType() {
        return mGameType;
    }
    public void setGameType(int gameType) {
        mGameType = gameType;
    }

    public String getGameName() {
        return mGameName;
    }
    public void setGameName(String gameName) {
        mGameName = gameName;
    }

    public int getImpulsesPerTurn() {
        return mImpulsesPerTurn;
    }
    public void setImpulsesPerTurn(int impulsesPerTurn) {
        mImpulsesPerTurn = impulsesPerTurn;
    }

    public String getPermLabel() {
        return mPermLabel;
    }
    public void setPermLabel(String permLabel) {
        mPermLabel = permLabel;
    }

    public boolean isHasTemp() {
        return mHasTemp;
    }
    public void setHasTemp(boolean hasTemp) {
        mHasTemp = hasTemp;
    }

    public String getTempLabel() {
        return mTempLabel;
    }
    public void setTempLabel(String tempLabel) {
        mTempLabel = tempLabel;
    }

    public boolean isHasInit() {
        return mHasInit;
    }
    public void setHasInit(boolean hasInit) {
        mHasInit = hasInit;
    }

    public String getInitLabel() {
        return mInitLabel;
    }
    public void setInitLabel(String initLabel) {
        mInitLabel = initLabel;
    }

    public boolean isInitIsNumber() {
        return mInitIsNumber;
    }
    public void setInitIsNumber(boolean initIsNumber) {
        mInitIsNumber = initIsNumber;
    }

    public String getInitList() {
        return mInitList;
    }
    public List<String> getInitListSplit() {
        return mInitListSplit;
    }
    public void setInitList(String initList) {
        mInitList = initList;

        // Split Init List for convenience
        mInitListSplit = Arrays.asList( initList.split(",") );
    }

    public String getDefaultInit() {
        return mDefaultInit;
    }
    public void setDefaultInit(String defaultInit) {
        mDefaultInit = defaultInit;
    }

    // Move Order Values:
    // 0: Low Speed First
    // 1: High Speed First</item>
    // 2: Low Initiative First</item>
    // 3: High Initiative First</item>
    // 4: None</item>

    public int getMoveOrder1() {
        return mMoveOrder1;
    }
    public void setMoveOrder1(int moveOrder1) {
        mMoveOrder1 = moveOrder1;
    }

    public int getMoveOrder2() {
        return mMoveOrder2;
    }
    public void setMoveOrder2(int moveOrder2) {
        mMoveOrder2 = moveOrder2;
    }

    //**** GAME INFO TABLE *************************************************************************

    public int getGameTypeKey() {
        return mGameTypeKey;
    }
    public void setGameTypeKey(int gameTypeKey) {
        mGameTypeKey = gameTypeKey;
    }

    // Current Turn and Current Impulse have some special meanings
    //      -1, -1   Configuring new game
    //       0,  0   Adding players and ships to new game
    //      1+,  0   Planning stage of turn 1+
    //      1+, 1+   Turn 1+ and Impulse 1+
    public int getCurrentTurn() {
        return mCurrentTurn;
    }
    public void setCurrentTurn(int currentTurn) {
        mCurrentTurn = currentTurn;
    }

    public int getCurrentImpulse() {
        return mCurrentImpulse;
    }
    public void setCurrentImpulse(int currentImpulse) {
        mCurrentImpulse = currentImpulse;
    }

    //**** HELPER METHODS **************************************************************************

    // Check if the Game Info parameters provided are valid
    //      If they are, update this object with those values
    //      If not, return an error message string indicating the first invalid value encountered
    public String setGameInfoIfValid(
            String gameConfigName,
            String impulsesString,
            String permUnitLabel,
            boolean hasTempUnits,
            String tempUnitLabel,
            boolean hasInitOrder,
            String initLabel,
            boolean initIsNumeric,
            String initList,
            String defaultInit,
            int moveOrder1,
            int moveOrder2,
            Resources resources) {

        String errorMsg = null;
        int impulsesPerTurn = ShipInfo.INVALID_SPEED;

        List<String> initListSplit = null;

        // *** VALIDATE EVERYTHING UNTIL WE FIND SOMETHING THAT IS WRONG
        //      Stop validating at the first error message we find.

        // Game Name cannot be blank
        if (gameConfigName.length() == 0) {
            errorMsg = resources.getString(R.string.config_error_game_name);
        }

        // Impulses per turn must be 1 to 99
        if (errorMsg == null) {
            if (impulsesString.length() > 0) {
                int tempImpulses = Integer.parseInt(impulsesString);
                if (tempImpulses >= 1 && tempImpulses <= 99) {
                    impulsesPerTurn = tempImpulses;
                } else {
                    errorMsg = resources.getString(R.string.config_error_phases);
                }
            } else {
                errorMsg = resources.getString(R.string.config_error_phases);
            }
        }

        // Permanent unit label cannot be blank
        if (errorMsg == null) {
            if (permUnitLabel.length() == 0) {
                errorMsg = resources.getString(R.string.config_error_perm_label);
            }
        }

        // If there are temp units, the temp unit label cannot be blank
        if (errorMsg == null) {
            if (hasTempUnits) {
                if (tempUnitLabel.length() == 0) {
                    errorMsg = resources.getString(R.string.config_error_temp_label);
                }
            }
        }

        // If there is an Initiative then initiative label cannot be blank
        if (errorMsg == null) {
            if (hasInitOrder && initLabel.length() == 0) {
                errorMsg = resources.getString(R.string.config_error_init_label);
            }
        }

        // If there is an Initiative and it is not numeric, then it must be a CSV list of values
        if (errorMsg == null) {
            if (hasInitOrder && ! initIsNumeric) {
                if (initList == null || initList.length() == 0) {
                    errorMsg = resources.getString(R.string.config_error_init_list);
                } else {
                    initListSplit = GameInfo.splitInitLabels(initList);
                    if (initListSplit == null || initListSplit.size() == 0) {
                        errorMsg = resources.getString(R.string.config_error_init_list);
                    } else {
                        for(String oneLabel : initListSplit) {
                            oneLabel = oneLabel.trim();
                            if (oneLabel.length() < 1) {
                                errorMsg = resources.getString(R.string.config_error_init_list_len_0);
                            }
                            if (oneLabel.length() > 4) {
                                errorMsg = resources.getString(R.string.config_error_init_list_len_4);
                            }
                        }
                    }
                }
            }
        }

        // If there is an Initiative then default initiative cannot be blank
        if (errorMsg == null) {
            if (hasInitOrder && defaultInit.length() == 0) {
                errorMsg = resources.getString(R.string.config_error_def_init_blank);
            } else {
                if (initIsNumeric) {
                    int defInit = -1;
                    try {
                        defInit = Integer.parseInt(defaultInit);
                    } catch (Exception e) {
                        defInit = -1;
                    }
                    if (defInit < 0) {
                        errorMsg = resources.getString(R.string.config_error_def_init_num);
                    }
                } else {
                    if ( ! initListSplit.contains(defaultInit) ) {
                        errorMsg = resources.getString(R.string.config_error_def_init_not_in_list);
                    }
                }
            }
        }

        // *** ALL CONFIG IS VALID
        if (errorMsg == null) {
            setGameName(gameConfigName);
            setImpulsesPerTurn(impulsesPerTurn);
            setPermLabel(permUnitLabel);
            setHasTemp(hasTempUnits);
            setTempLabel(tempUnitLabel);
            setHasInit(hasInitOrder);
            setInitLabel(initLabel);
            setInitIsNumber(initIsNumeric);
            setInitList(initList);
            setDefaultInit(defaultInit);
            setMoveOrder1(moveOrder1);
            setMoveOrder2(moveOrder2);
        }
        
        return errorMsg;
    }
    
    
    public void setCurrentMove(int currentTurn, int currentImpulse) {
        mCurrentTurn = currentTurn;
        mCurrentImpulse = currentImpulse;
    }

    public boolean isSpeedValid(int speed) {
        return ( Math.abs(speed) <= getImpulsesPerTurn());
    }

    // Get list of unit types to use in Spinners
    public List<String> getListOfUnitTypeLabels() {

        List<String> unitTypeLabels = new ArrayList<String>();

        unitTypeLabels.add(getPermLabel() + ":");
        if (isHasTemp()) {
            unitTypeLabels.add(getTempLabel() + ":");
        }

        return unitTypeLabels;
    }

    // Get the text label for the specified unit type
    public String getUnitTypeLabel(int unitType) {
        if (isHasTemp() && unitType == 1) {
            return getTempLabel();
        } else {
            return  getPermLabel();
        }
    }

    // Get label for the initiative value (when initiative is not numeric)
    public String getInitListLabel(int initVal) {
        if (!isInitIsNumber() && initVal >= 0 && initVal < mInitListSplit.size()) {
            return mInitListSplit.get(initVal);
        } else {
            return "";
        }
    }

    //**** STATIC HELPER METHODS *******************************************************************

    // Split Initiative Order Labels comma separated string into an array
    public static List<String> splitInitLabels(String initLabels) {
        return Arrays.asList( initLabels.split(",") );
    }
    
    // Get the default settings for the specified Game Type
    public static GameInfo getGameTypeDefaultSettings(int gameType, Resources resources) {
        GameInfo gameInfo = new GameInfo();
        switch (gameType) {
            case 0: // Custom - Leave user's current settings
                gameInfo.setGameName( resources.getString(R.string.game_type_custom) );
                break;
            case 1: // Champions
                gameInfo.setGameName( resources.getString(R.string.game_type_hero) );
                gameInfo.setImpulsesPerTurn(12);
                gameInfo.setHasTemp(false);
                gameInfo.setPermLabel( resources.getString(R.string.hero_perm_label) );
                gameInfo.setTempLabel( resources.getString(R.string.hero_temp_label) );
                gameInfo.setHasInit(true);
                gameInfo.setInitLabel( resources.getString(R.string.hero_move_order_label) );
                gameInfo.setInitIsNumber(true);
                gameInfo.setInitList( resources.getString(R.string.hero_init_list) );
                gameInfo.setDefaultInit( Integer.toString(10) );
                gameInfo.setMoveOrder1(3);
                gameInfo.setMoveOrder2(4);
                break;
            case 2: // d20 RPG
                gameInfo.setGameName( resources.getString(R.string.game_type_d20) );
                gameInfo.setImpulsesPerTurn(1);
                gameInfo.setHasTemp(true);
                gameInfo.setPermLabel( resources.getString(R.string.d20_perm_label) );
                gameInfo.setTempLabel( resources.getString(R.string.d20_temp_label) );
                gameInfo.setHasInit(true);
                gameInfo.setInitLabel( resources.getString(R.string.d20_move_order_label) );
                gameInfo.setInitIsNumber(true);
                gameInfo.setInitList( resources.getString(R.string.d20_init_list) );
                gameInfo.setDefaultInit( Integer.toString(10) );
                gameInfo.setMoveOrder1(3);
                gameInfo.setMoveOrder2(4);
                break;
            case 3: // Star Fleet Battles
                gameInfo.setGameName( resources.getString(R.string.game_type_sfb) );
                gameInfo.setImpulsesPerTurn(32);
                gameInfo.setHasTemp(true);
                gameInfo.setPermLabel( resources.getString(R.string.sfb_perm_label) );
                gameInfo.setTempLabel( resources.getString(R.string.sfb_temp_label) );
                gameInfo.setHasInit(true);
                gameInfo.setInitLabel( resources.getString(R.string.sfb_move_order_label) );
                gameInfo.setInitIsNumber(false);
                gameInfo.setInitList( resources.getString(R.string.sfb_init_list) );
                gameInfo.setDefaultInit( resources.getString(R.string.sfb_default_init) );
                gameInfo.setMoveOrder1(0);
                gameInfo.setMoveOrder2(3);
                break;
            case 4: // Federation Commander
                gameInfo.setGameName( resources.getString(R.string.game_type_fc) );
                gameInfo.setImpulsesPerTurn(32);
                gameInfo.setHasTemp(true);
                gameInfo.setPermLabel( resources.getString(R.string.sfb_perm_label) );
                gameInfo.setTempLabel( resources.getString(R.string.sfb_temp_label) );
                gameInfo.setHasInit(true);
                gameInfo.setInitLabel( resources.getString(R.string.sfb_move_order_label) );
                gameInfo.setInitIsNumber(false);
                gameInfo.setInitList( resources.getString(R.string.sfb_init_list) );
                gameInfo.setDefaultInit( resources.getString(R.string.sfb_default_init) );
                gameInfo.setMoveOrder1(0);
                gameInfo.setMoveOrder2(3);
                break;
        }

        return gameInfo;
    }

}
