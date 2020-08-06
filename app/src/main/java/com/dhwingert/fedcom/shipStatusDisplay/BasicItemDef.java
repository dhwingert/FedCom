package com.dhwingert.fedcom.shipStatusDisplay;

/**
 * Basic Item Definition is used to describe one system block on a Ship System Display (SSD),
 * used for ships in Federation Commander.
 *
 * A block (group of squares on a SSD) is always a single type of system.
 * (Shield, Hull, Weapon, Power, etc.)
 *
 * So Type = "bridge" and Count = 2 represents a block of 2 bridge squares on the ship's SSD.
 *
 * Created by David Wingert on 11/24/2018.
 */
public class BasicItemDef {
    private String type;
    private Integer count;
    private Integer destroyedCount;
    private Integer destroyedAtStartOfTurnCount;
    private Integer destroyedThisTurnCount;

    /**
     * The system's type.
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The count of squares in the group.
     */
    public Integer getCount() {
        // To make the code for calculations easier, never return null.  Return 0 instead of null.
        return (count != null) ? count : 0;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * The count of how many squares in this group were destroyed (not working) at the start of the current turn.
     */
    public Integer getDestroyedAtStartOfTurnCount() {
        // To make the code for calculations easier, never return null.  Return 0 instead of null.
        return (destroyedAtStartOfTurnCount != null) ? destroyedAtStartOfTurnCount : 0;
    }

    public void setDestroyedAtStartOfTurnCount(Integer destroyedAtStartOfTurnCount) {
        this.destroyedAtStartOfTurnCount = destroyedAtStartOfTurnCount;
    }

    /**
     * The count of how many squares in this group were destroyed in the current turn.
     */
    public Integer getDestroyedThisTurnCount() {
        // To make the code for calculations easier, never return null.  Return 0 instead of null.
        return (destroyedThisTurnCount != null) ? destroyedThisTurnCount : 0;
    }

    public void setDestroyedThisTurnCount(Integer destroyedThisTurnCount) {
        this.destroyedThisTurnCount = destroyedThisTurnCount;
    }

    /**
     * Count of how many squares in the group have been destroyed in combat.
     */
    public Integer getDestroyedTotalCount() {
        return getDestroyedAtStartOfTurnCount() + getDestroyedThisTurnCount();
    }

}
