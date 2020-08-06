package com.dhwingert.fedcom.shipStatusDisplay;

/**
 * Usable Item Definition is used to describe one system block on a Ship System Display (SSD),
 * used for ships in Federation Commander.  Usable systems are ones that can be used once per turn,
 * such as power or weapons.
 *
 * A block (group of squares on a SSD) is always a single type of system.
 * (Shield, Hull, Weapon, Power, etc.)
 *
 * So Type = "bridge" and Count = 2 represents a block of 2 bridge squares on the ship's SSD.
 *
 * Created by David Wingert on 11/24/2018.
 */
public class UsableItemDef extends BasicItemDef {

    private Integer usedCount;

    /**
     * The count of squares in the group that have not been used yet this turn.
     *
     * When boxes of a usable system are destroyed, the count of Used or Unused boxes is also reduced.
     * The sum of Unused + Used + Destroyed should always equal the total number of boxes.
     *
     * Used system boxes are destroyed first.  However, if there is more damage than Used system boxes,
     * then Unused system boxes are destroyed to make up the difference.
     */
    public Integer getUsedCount() {
        // To make the code for calculations easier, never return null.  Return 0 instead of null.
        return (usedCount != null) ? usedCount : 0;
    }

    public void setUsedCount(Integer usedCount) {
        this.usedCount = usedCount;
    }
}
