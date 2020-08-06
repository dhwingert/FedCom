package com.dhwingert.fedcom.shipStatusDisplay;

/**
 * Power Definition is used to describe all of the powers systems on a Ship System Display (SSD),
 * used for ships in Federation Commander.
 *
 * Power Definition is divided into separate lists of power systems by type (warp, impulse, etc.)
 *
 * Created by David Wingert on 11/24/2018.
 */
public class PowerDef {
    private BasicItemList<BasicItemDef> warpPower;
    private BasicItemList<BasicItemDef> impulsePower;
    private BasicItemList<BasicItemDef> reactorPower;
    private BasicItemList<BasicItemDef> batteryPower;

    // At the beginning of each turn all the existing power systems put power into a "power pool"
    // that can then be used throughout the turn to power systems.
    //      The power is available for the turn even if the power system warp, etc.) that generated the power
    //      is destroyed during the turn.
    private int powerPoolSize;
    private int powerPoolUsed;

    /**
     * The list of all Warp Power systems on this ship.
     */
    public BasicItemList<BasicItemDef> getWarpPower() {
        // If the list does not exist when it is retrieved, set it to an empty list.
        // This way the calling code does can always assume the list exists instead of always checking for null first.
        if (this.warpPower == null) {
            this.warpPower = new BasicItemList<>();
        }
        return warpPower;
    }

    public void setWarpPower(BasicItemList<BasicItemDef> warpPower) {
        this.warpPower = warpPower;
    }

    /**
     * The list of all Impulse Power systems on this ship.
     */
    public BasicItemList<BasicItemDef> getImpulsePower() {
        // If the list does not exist when it is retrieved, set it to an empty list.
        // This way the calling code does can always assume the list exists instead of always checking for null first.
        if (this.impulsePower == null) {
            this.impulsePower = new BasicItemList<>();
        }
        return impulsePower;
    }

    public void setImpulsePower(BasicItemList<BasicItemDef> impulsePower) {
        this.impulsePower = impulsePower;
    }

    /**
     * The list of all Reactor Power systems on this ship.
     */
    public BasicItemList<BasicItemDef> getReactorPower() {
        // If the list does not exist when it is retrieved, set it to an empty list.
        // This way the calling code does can always assume the list exists instead of always checking for null first.
        if (this.reactorPower == null) {
            this.reactorPower = new BasicItemList<>();
        }
        return reactorPower;
    }

    public void setReactorPower(BasicItemList<BasicItemDef> reactorPower) {
        this.reactorPower = reactorPower;
    }

    /**
     * The list of all Battery Power systems on this ship.
     */
    public BasicItemList<BasicItemDef> getBatteryPower() {
        // If the list does not exist when it is retrieved, set it to an empty list.
        // This way the calling code does can always assume the list exists instead of always checking for null first.
        if (this.batteryPower == null) {
            this.batteryPower = new BasicItemList<>();
        }
        return batteryPower;
    }

    public void setBatteryPower(BasicItemList<BasicItemDef> batteryPower) {
        this.batteryPower = batteryPower;
    }

    /**
     * The total number of power points in the Power Pool at the start of this turn.
     */
    public int getPowerPoolTotalCount() {
        return powerPoolSize;
    }

    /**
     * The number of power points from the Power Pool that have NOT been used this turn.
     */
    public int getPowerPoolUnusedCount() {
        return powerPoolSize - powerPoolUsed;
    }

    /**
     * The number of power points from the Power Pool that HAVE been used this turn.
     */
    public int getPowerPoolUsedCount() {
        return powerPoolUsed;
    }

}
