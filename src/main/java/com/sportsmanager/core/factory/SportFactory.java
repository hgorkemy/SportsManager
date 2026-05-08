package com.sportsmanager.core.factory;

import com.sportsmanager.core.engine.MatchEngine;
import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.Sport;
import com.sportsmanager.core.model.Team;

import java.util.List;


public interface SportFactory {
    Sport createSport();
    List<Team> generateTeams(int count);
    League createLeague(List<Team> teams);
    MatchEngine createMatchEngine();
}
