package com.dhwingert.fedcom.shipStatusDisplay;

/**
 * List of Usable Item Definitions.  Usable Item Def is used to describe one system block on a Ship System Display (SSD)
 * used for ships in Federation Commander.  Usable Systems can be used once per turn.
 *
 * A block (group of squares on a SSD) is always a single type of system.
 * (Shield, Hull, Weapon, Power, etc.)
 *
 * So Type = "bridge" and Count = 2 represents a block of 2 bridge squares on the ship's SSD.
 *
 * Created by David Wingert on 12/15/2018.
 */
public class UsableItemList<T extends UsableItemDef> extends BasicItemList<T> {

    /**
     * The count of squares of all items in the list that have been used this turn.
     */
    public int getBoxCountUsed() {
        int count = 0;
        for (UsableItemDef oneUsableItem : this) {
            count += oneUsableItem.getUsedCount();
        }
        return count;
    }

    /**
     * The count of squares of all items in the list that have NOT been used yet this turn.
     * (They also have not been destroyed.)
     */
    public int getBoxCountUnused() {
        int count = getCountTotal() - getBoxCountUsed();
        return count;
    }

}
