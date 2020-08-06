package com.dhwingert.fedcom.shipStatusDisplay;

/**
 * Shield Definition is used to describe one shield block on a Ship System Display (SSD),
 * used for ships in Federation Commander.
 *
 * Shields have a direction (1 to 6).  Ships with shields always have shields 1 thru 6.
 * Not all ships have shields.
 *
 * Created by David Wingert on 11/24/2018.
 */
public class ShieldDef extends BasicItemDef {

    private Integer direction;

    /**
     * Direction of this shield.
     */
    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }
}
