package com.sportsmanager.football;

import com.sportsmanager.core.model.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Football-specific player with 6 attributes.
 *
 * TODO (Irmak): Implement getOverallRating() using position-based weights.
 * GK: defending heavy, FWD: shooting heavy, MID: passing heavy, DEF: defending heavy.
 */
public class FootballPlayer extends Player {

    private final Map<String, Integer> attributes;

    public FootballPlayer(String firstName, String lastName, int age,
                          FootballPosition position,
                          int speed, int shooting, int passing,
                          int ballControl, int defending, int physicality) {
        super(firstName, lastName, age, position);
        this.attributes = new HashMap<>();
        this.attributes.put("speed", speed);
        this.attributes.put("shooting", shooting);
        this.attributes.put("passing", passing);
        this.attributes.put("ballControl", ballControl);
        this.attributes.put("defending", defending);
        this.attributes.put("physicality", physicality);
    }

    @Override
    public Map<String, Integer> getAttributes() {
        return attributes;
    }

    @Override
    public int getOverallRating() {
        if (getPosition() == FootballPosition.GOALKEEPER) {
            return (int) Math.round(getDefending() * 0.50 + getPhysicality() * 0.20 + getSpeed() * 0.15 + getPassing() * 0.10 + getBallControl() * 0.05);
        } else if (getPosition() == FootballPosition.DEFENDER) {
            return (int) Math.round(getDefending() * 0.40 + getPhysicality() * 0.20 + getSpeed() * 0.20 + getPassing() * 0.10 + getBallControl() * 0.10);
        } else if (getPosition() == FootballPosition.MIDFIELDER) {
            return (int) Math.round(getPassing() * 0.30 + getBallControl() * 0.25 + getSpeed() * 0.20 + getPhysicality() * 0.15 + getShooting() * 0.10);
        } else { // FORWARD
            return (int) Math.round(getShooting() * 0.40 + getSpeed() * 0.25 + getPassing() * 0.15 + getBallControl() * 0.15 + getPhysicality() * 0.05);
        }
    }

    public int getSpeed()       { return attributes.get("speed"); }
    public int getShooting()    { return attributes.get("shooting"); }
    public int getPassing()     { return attributes.get("passing"); }
    public int getBallControl() { return attributes.get("ballControl"); }
    public int getDefending()   { return attributes.get("defending"); }
    public int getPhysicality() { return attributes.get("physicality"); }
}
