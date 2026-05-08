package com.sportsmanager.core.model;

import java.util.List;
import java.util.Map;


public interface Sport {
    String getName();
    int getPlayersPerTeam();
    int getSubstitutesPerTeam();
    int getNumberOfPeriods();
    String getPeriodName();
    int getMaxSubstitutions();
    int getWinPoints();
    int getDrawPoints();
    int getLossPoints();
    List<Position> getAvailablePositions();
    List<Tactic> getAvailableTactics();
    List<String> getPlayerAttributes();
    Map<String, Double> getAttributeWeights();


    default String getTacticsViewName() { return "TacticsLineupView"; }
}
