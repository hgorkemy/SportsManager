package com.sportsmanager.core.factory;

import com.sportsmanager.core.engine.MatchEngine;
import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.Sport;
import com.sportsmanager.core.model.Team;

import java.util.List;

/**
 * Factory interface — creates all sport-specific objects.
 * Adding a new sport = new factory implementation only.
 *
 * TODO (Egemen): This interface is defined. No changes needed here.
 */
public interface SportFactory {
    Sport createSport();
    List<Team> generateTeams(int count);
    League createLeague(List<Team> teams);
    MatchEngine createMatchEngine();
}
