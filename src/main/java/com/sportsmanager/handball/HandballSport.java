package com.sportsmanager.handball;

import com.sportsmanager.core.model.Position;
import com.sportsmanager.core.model.Sport;
import com.sportsmanager.core.model.Tactic;

import java.util.List;
import java.util.Map;

/**
 * Handball implementation of the Sport interface.
 * 7 players per side, 2 halves, WIN=2 DRAW=1 LOSS=0.
 */
public class HandballSport implements Sport {

    @Override public String getName()            { return "Handball"; }
    @Override public int getPlayersPerTeam()     { return 7; }
    @Override public int getSubstitutesPerTeam() { return 9; }
    @Override public int getNumberOfPeriods()    { return 2; }
    @Override public String getPeriodName()      { return "Half"; }
    @Override public int getMaxSubstitutions()   { return -1; } // unlimited in handball
    @Override public int getWinPoints()          { return 2; }
    @Override public int getDrawPoints()         { return 1; }
    @Override public int getLossPoints()         { return 0; }

    @Override
    public List<Position> getAvailablePositions() {
        return List.of(HandballPosition.values());
    }

    @Override
    public List<Tactic> getAvailableTactics() {
        return List.of();
    }

    @Override
    public List<String> getPlayerAttributes() {
        return List.of("throwing", "speed", "agility", "jumping", "defending", "stamina");
    }

    @Override
    public Map<String, Double> getAttributeWeights() {
        return Map.of(
            "throwing", 0.20, "speed",     0.18, "agility", 0.18,
            "jumping",  0.15, "defending", 0.18, "stamina", 0.11
        );
    }
}
