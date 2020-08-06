package com.dhwingert.fedcom.shipStatusDisplay;

import java.util.ArrayList;

/**
 * List of Basic Item Definitions.  Basic Item Def is used to describe one system block on a Ship System Display (SSD),
 * used for ships in Federation Commander.
 *
 * A block (group of squares on a SSD) is always a single type of system.
 * (Shield, Hull, Weapon, Power, etc.)
 *
 * So Type = "bridge" and Count = 2 represents a block of 2 bridge squares on the ship's SSD.
 *
 * Created by David Wingert on 12/15/2018.
 */
public class BasicItemList<T extends BasicItemDef> extends ArrayList<T> {

    /**
     * The count of squares of all items in the list (whether destroyed or not).
     */
    public int getCountTotal() {
        int count = 0;
        for (BasicItemDef oneBasicItem : this) {
            count += oneBasicItem.getCount();
        }
        return count;
    }

    /**
     * The count of squares of all items in the list that started the current turn as destroyed.
     */
    public int getDestroyedAtStartOfTurnCount() {
        int count = 0;
        for (BasicItemDef oneBasicItem : this) {
            count += oneBasicItem.getDestroyedAtStartOfTurnCount();
        }
        return count;
    }

    /**
     * The count of squares of all items in the list that were destroyed during the current turn.
     */
    public int getDestroyedThisTurnCount() {
        int count = 0;
        for (BasicItemDef oneBasicItem : this) {
            count += oneBasicItem.getDestroyedThisTurnCount();
        }
        return count;
    }

    /**
     * The count of squares of all items in the list that have been destroyed.
     */
    public int getDestroyedTotalCount() {
        int count = 0;
        for (BasicItemDef oneBasicItem : this) {
            count += oneBasicItem.getDestroyedTotalCount();
        }
        return count;
    }

    /**
     * The count of squares of all items in the list that have NOT been destroyed.
     */
    public int getNotDestroyedTotalCount() {
        int count = getCountTotal() - getDestroyedTotalCount();
        return count;
    }

}
