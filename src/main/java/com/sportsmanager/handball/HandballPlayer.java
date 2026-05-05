package com.sportsmanager.handball;

import com.sportsmanager.core.model.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Handball-specific player with 6 attributes (all 40-90).
 * Implemented by: Irmak Önder
 */
public class HandballPlayer extends Player {

    private final Map<String, Integer> attributes;

    public HandballPlayer(String firstName, String lastName, int age,
                          HandballPosition position,
                          int throwing, int speed, int agility,
                          int jumping, int defending, int stamina) {
        super(firstName, lastName, age, position);
        this.attributes = new HashMap<>();
        this.attributes.put("throwing",  throwing);
        this.attributes.put("speed",     speed);
        this.attributes.put("agility",   agility);
        this.attributes.put("jumping",   jumping);
        this.attributes.put("defending", defending);
        this.attributes.put("stamina",   stamina);
    }

    @Override
    public Map<String, Integer> getAttributes() {
        return attributes;
    }

    @Override
    public int getOverallRating() {
        HandballPosition pos = (HandballPosition) getPosition();
        return switch (pos) {
            case GOALKEEPER -> (int) Math.round(
                    getDefending() * 0.50 + getAgility() * 0.20 + getJumping() * 0.15 + getStamina() * 0.10 + getSpeed() * 0.05);
            case WING       -> (int) Math.round(
                    getSpeed()    * 0.35 + getThrowing() * 0.35 + getAgility() * 0.15 + getStamina() * 0.10 + getDefending() * 0.05);
            case BACK       -> (int) Math.round(
                    getThrowing() * 0.35 + getJumping()  * 0.30 + getSpeed()   * 0.15 + getAgility() * 0.10 + getStamina()   * 0.10);
            case PIVOT      -> (int) Math.round(
                    getDefending() * 0.35 + getJumping() * 0.30 + getStamina() * 0.20 + getThrowing() * 0.10 + getAgility()  * 0.05);
        };
    }

    public int getThrowing()  { return attributes.get("throwing"); }
    public int getSpeed()     { return attributes.get("speed"); }
    public int getAgility()   { return attributes.get("agility"); }
    public int getJumping()   { return attributes.get("jumping"); }
    public int getDefending() { return attributes.get("defending"); }
    public int getStamina()   { return attributes.get("stamina"); }
}
