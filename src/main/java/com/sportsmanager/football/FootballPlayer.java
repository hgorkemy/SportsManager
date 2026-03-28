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

    private int speed;
    private int shooting;
    private int passing;
    private int ballControl;
    private int defending;
    private int physicality;

    public FootballPlayer(String firstName, String lastName, int age,
                          FootballPosition position,
                          int speed, int shooting, int passing,
                          int ballControl, int defending, int physicality) {
        super(firstName, lastName, age, position);
        this.speed = speed;
        this.shooting = shooting;
        this.passing = passing;
        this.ballControl = ballControl;
        this.defending = defending;
        this.physicality = physicality;
    }

    @Override
    public Map<String, Integer> getAttributes() {
        Map<String, Integer> attrs = new HashMap<>();
        attrs.put("speed", speed);
        attrs.put("shooting", shooting);
        attrs.put("passing", passing);
        attrs.put("ballControl", ballControl);
        attrs.put("defending", defending);
        attrs.put("physicality", physicality);
        return attrs;
    }

    @Override
    public int getOverallRating() {
        // TODO (Görkem): Implement position-based weighted average
        return (speed + shooting + passing + ballControl + defending + physicality) / 6;
    }

    public int getSpeed()       { return speed; }
    public int getShooting()    { return shooting; }
    public int getPassing()     { return passing; }
    public int getBallControl() { return ballControl; }
    public int getDefending()   { return defending; }
    public int getPhysicality() { return physicality; }
}
