package com.sportsmanager.football;

import com.sportsmanager.core.model.Position;
import com.sportsmanager.core.model.Sport;
import com.sportsmanager.core.model.Tactic;

import java.util.List;
import java.util.Map;

/**
 * Football implementation of Sport interface.
 *
 * TODO (Berke): Implement all methods.
 */
public class FootballSport implements Sport {

    @Override public String getName()              { return "Football"; }
    @Override public int getPlayersPerTeam()       { return 11; }
    @Override public int getSubstitutesPerTeam()   { return 7; }
    @Override public int getNumberOfPeriods()      { return 2; }
    @Override public String getPeriodName()        { return "Half"; }
    @Override public int getMaxSubstitutions()     { return 5; }
    @Override public int getWinPoints()            { return 3; }
    @Override public int getDrawPoints()           { return 1; }
    @Override public int getLossPoints()           { return 0; }

    @Override
    public List<Position> getAvailablePositions() {
        // TODO (Berke): Return FootballPosition values
        return List.of(FootballPosition.values());
    }

    @Override
    public List<Tactic> getAvailableTactics() {
        // TODO (Berke): Return FootballTactic instances
        return List.of();
    }

    @Override
    public List<String> getPlayerAttributes() {
        return List.of("speed", "shooting", "passing", "ballControl", "defending", "physicality");
    }

    @Override
    public Map<String, Double> getAttributeWeights() {
        // TODO (Berke): Return position-agnostic default weights
        return Map.of(
            "speed", 0.15, "shooting", 0.20, "passing", 0.20,
            "ballControl", 0.15, "defending", 0.15, "physicality", 0.15
        );
    }
}
