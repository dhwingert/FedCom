package com.dhwingert.fedcom.shipStatusDisplay;

import java.math.BigDecimal;
import java.util.List;

/**
 * Ship Systems Display (SSD) are used to describe ship designs in Federation Commander.
 *
 * Each Empire has a number of different ship designs that they have built.
 * Captain Kirk's Enterprise is a particular ship design.  Several ships of that design were built.
 *
 * Created by David Wingert on 11/24/2018.
 */
public class ShipSystemsDisplay {
    private String shipClass;
    private Integer pointValue;
    private String empire;
    private Integer damageControl;
    private Integer probes;
    private Integer marines;
    private Integer frameDamage;
    private String turnMode;
    private BigDecimal moveCost;
    private BigDecimal accelerationCost;
    private BigDecimal decelerationCost;
    private BigDecimal highEnergyTurnCost;
    private BigDecimal evasiveManeuverCost;
    private Integer shuttlecraftCount;
    private BasicItemList<ShieldDef> shields;
    private PowerDef power;
    private BasicItemList<BasicItemDef> hull;
    private BasicItemList<BasicItemDef> controlRooms;
    private UsableItemList<WeaponDef> weapons;
    private BasicItemList<BasicItemDef> systems;

    /**
     * Class of ship this represents (Heavy Cruiser, Destroyer, etc.)
     * Empires often built more than one design of ship for a given class.
     */
    public String getShipClass() {
        return shipClass;
    }

    public void setShipClass(String shipClass) {
        this.shipClass = shipClass;
    }

    /**
     * Point value of this type of ship.  Used with Empire and Ship Class to specify exactly which ship design it is.
     * Empire = "Federation", Ship Class = "Heavy Cruiser", and Point Value = 147 specifies a particular ship design.
     */
    public Integer getPointValue() {
        return pointValue;
    }

    public void setPointValue(Integer pointValue) {
        this.pointValue = pointValue;
    }

    /**
     * Empire that built this type of ship (Federation, Klingons, Romulans, etc.)
     */
    public String getEmpire() {
        return empire;
    }

    public void setEmpire(String empire) {
        this.empire = empire;
    }

    /**
     * Damage Control rating of this type of ship.  Used to determine how much damage can it repair each turn.
     */
    public Integer getDamageControl() {
        return damageControl;
    }

    public void setDamageControl(Integer damageControl) {
        this.damageControl = damageControl;
    }

    /**
     * Count of how many probes does this type of ship carry.
     */
    public Integer getProbes() {
        return probes;
    }

    public void setProbes(Integer probes) {
        this.probes = probes;
    }

    /**
     * Count of how many squads of marines does this type of ship carry.
     */
    public Integer getMarines() {
        return marines;
    }

    public void setMarines(Integer marines) {
        this.marines = marines;
    }

    /**
     * Count of how many squares of frame damage it takes to destroy this type of ship.
     */
    public Integer getFrameDamage() {
        return frameDamage;
    }

    public void setFrameDamage(Integer frameDamage) {
        this.frameDamage = frameDamage;
    }

    /**
     * Turn Mode rating of this ship design.
     * Determines how far the ship must travel straight before it can turn to a new hex facing.
     */
    public String getTurnMode() {
        return turnMode;
    }

    public void setTurnMode(String turnMode) {
        this.turnMode = turnMode;
    }

    /**
     * Cost to move one hex on the map for this type of ship.
     */
    public BigDecimal getMoveCost() {
        return moveCost;
    }

    public void setMoveCost(BigDecimal moveCost) {
        this.moveCost = moveCost;
    }

    /**
     * Cost to speed up (move an extra hex in a given impulse) for this type of ship.
     */
    public BigDecimal getAccelerationCost() {
        return accelerationCost;
    }

    public void setAccelerationCost(BigDecimal accelerationCost) {
        this.accelerationCost = accelerationCost;
    }

    /**
     * Cost to slow down (move one less hex in a given impulse) for this type of ship.
     */
    public BigDecimal getDecelerationCost() {
        return decelerationCost;
    }

    public void setDecelerationCost(BigDecimal decelerationCost) {
        this.decelerationCost = decelerationCost;
    }

    /**
     * Cost to perform a High Energy Turn (HET) maneuver for this type of ship.
     */
    public BigDecimal getHighEnergyTurnCost() {
        return highEnergyTurnCost;
    }

    public void setHighEnergyTurnCost(BigDecimal highEnergyTurnCost) {
        this.highEnergyTurnCost = highEnergyTurnCost;
    }

    /**
     * Cost to perform Evasive Maneuvers for this type of ship.
     */
    public BigDecimal getEvasiveManeuverCost() {
        return evasiveManeuverCost;
    }

    public void setEvasiveManeuverCost(BigDecimal evasiveManeuverCost) {
        this.evasiveManeuverCost = evasiveManeuverCost;
    }

    /**
     * Count of the number of shuttlecraft this type of ship carries.
     */
    public Integer getShuttlecraftCount() {
        return shuttlecraftCount;
    }

    public void setShuttlecraftCount(Integer shuttlecraftCount) {
        this.shuttlecraftCount = shuttlecraftCount;
    }

    /**
     * List of 6 Shields (directions 1 thru 6) for this type of ship.
     */
    public BasicItemList<ShieldDef> getShields() {
        return shields;
    }

    public void setShields(BasicItemList<ShieldDef> shields) {
        this.shields = shields;
    }

    /**
     * List of all the different sources of power for this type of ship.
     */
    public PowerDef getPower() {
        return power;
    }

    public void setPower(PowerDef power) {
        this.power = power;
    }

    /**
     * List of all the different hull blocks for this type of ship.
     */
    public BasicItemList<BasicItemDef> getHull() {
        return hull;
    }

    public void setHull(BasicItemList<BasicItemDef> hull) {
        this.hull = hull;
    }

    /**
     * List of all the different control rooms for this type of ship.
     */
    public BasicItemList<BasicItemDef> getControlRooms() {
        return controlRooms;
    }

    public void setControlRooms(BasicItemList<BasicItemDef> controlRooms) {
        this.controlRooms = controlRooms;
    }

    /**
     * List of all the different weapons carried by this type of ship.
     */
    public UsableItemList<WeaponDef> getWeapons() {
        return weapons;
    }

    public void setWeapons(UsableItemList<WeaponDef> weapons) {
        this.weapons = weapons;
    }

    /**
     * List of all the miscellaneous systems for this type of ship.
     */
    public BasicItemList<BasicItemDef> getSystems() {
        return systems;
    }

    public void setSystems(BasicItemList<BasicItemDef> systems) {
        this.systems = systems;
    }
}
