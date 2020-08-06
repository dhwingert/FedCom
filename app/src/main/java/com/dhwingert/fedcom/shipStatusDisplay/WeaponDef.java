package com.dhwingert.fedcom.shipStatusDisplay;

/**
 * Weapon Definition is used to describe one weapon block on a Ship System Display (SSD),
 * used for ships in Federation Commander.
 *
 * A block (group of squares on a SSD) is always a single type of system.
 * (Shield, Hull, Weapon, Power, etc.)
 *
 * Type = "phasor", SubType = "1", Count = 2, and Arcs = ["fa"] indicates
 * a block of 2 Phasor 1s with an arc of FA.
 *
 * Created by David Wingert on 11/24/2018.
 */
public class WeaponDef extends UsableItemDef {
    private String subType;
    private String[] arcs;

    /**
     * SubType is additional info to describe exactly what type of weapon.
     * Not all weapons have a sub-type.
     *
     * For phasors it indicates what type of phasor the block is (1, 2, 3, or 4)
     * For plasma torpedoes it indicates the torpedo type (S, R, G, F, etc.)
     */
    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    /**
     * The firing arcs of the weapon.
     * Not all weapons specify firing arcs.
     */
    public String[] getArcs() {
        return arcs;
    }

    public void setArcs(String[] arcs) {
        this.arcs = arcs;
    }
}
