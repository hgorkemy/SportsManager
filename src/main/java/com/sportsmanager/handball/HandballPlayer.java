package com.sportsmanager.handball;

import com.sportsmanager.core.model.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Handball-specific player with 6 attributes (all 40-90).
 * Implemented by: Irmak Önder
 */
public class HandballPlayer extends Player {

    private int throwing;
    private int speed;
    private int agility;
    private int jumping;
    private int defending;
    private int stamina;

    public HandballPlayer(String firstName, String lastName, int age,
                          HandballPosition position,
                          int throwing, int speed, int agility,
                          int jumping, int defending, int stamina) {
        super(firstName, lastName, age, position);
        this.throwing  = throwing;
        this.speed     = speed;
        this.agility   = agility;
        this.jumping   = jumping;
        this.defending = defending;
        this.stamina   = stamina;
    }

    @Override
    public Map<String, Integer> getAttributes() {
        Map<String, Integer> attrs = new HashMap<>();
        attrs.put("throwing",  throwing);
        attrs.put("speed",     speed);
        attrs.put("agility",   agility);
        attrs.put("jumping",   jumping);
        attrs.put("defending", defending);
        attrs.put("stamina",   stamina);
        return attrs;
    }

    @Override
    public int getOverallRating() {
        HandballPosition pos = (HandballPosition) getPosition();
        return switch (pos) {
            case GOALKEEPER -> (int) Math.round(
                    defending * 0.50 + agility * 0.20 + jumping * 0.15 + stamina * 0.10 + speed * 0.05);
            case WING       -> (int) Math.round(
                    speed    * 0.35 + throwing * 0.35 + agility * 0.15 + stamina * 0.10 + defending * 0.05);
            case BACK       -> (int) Math.round(
                    throwing * 0.35 + jumping  * 0.30 + speed   * 0.15 + agility * 0.10 + stamina   * 0.10);
            case PIVOT      -> (int) Math.round(
                    defending * 0.35 + jumping * 0.30 + stamina * 0.20 + throwing * 0.10 + agility  * 0.05);
        };
    }

    public int getThrowing()  { return throwing; }
    public int getSpeed()     { return speed; }
    public int getAgility()   { return agility; }
    public int getJumping()   { return jumping; }
    public int getDefending() { return defending; }
    public int getStamina()   { return stamina; }
}
