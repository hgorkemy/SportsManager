package com.sportsmanager.core.model;

import com.sportsmanager.core.engine.MatchEngine;

/**
 * Singleton holding the active game state.
 * All UI controllers read from this class.
 *
 * Implemented by: Halil Görkem Yiğit
 */
public class GameSession {

    private static GameSession instance;

    private Sport sport;
    private League league;
    private Team userTeam;
    private MatchEngine matchEngine;
    private int currentSeason;
    private String saveName;
    private String selectedSportName;

    private GameSession() {}

    public static GameSession getInstance() {
        if (instance == null) {
            instance = new GameSession();
        }
        return instance;
    }

    public void startNewGame(Sport sport, League league, Team userTeam,
                             MatchEngine matchEngine, String saveName) {
        this.sport = sport;
        this.league = league;
        this.userTeam = userTeam;
        this.matchEngine = matchEngine;
        this.currentSeason = 1;
        this.saveName = saveName;
        userTeam.setUserTeam(true);

        for (Team t : league.getTeams()) {
            for (Player p : t.getSquad()) {
                p.snapshotInitialOverall();
            }
        }
    }

    public void advanceSeason() {
        currentSeason++;
        league.startNewSeason();
    }

    public boolean isActive() {
        return sport != null && league != null && userTeam != null;
    }

    public void reset() {
        sport = null;
        league = null;
        userTeam = null;
        matchEngine = null;
        currentSeason = 0;
        saveName = null;
        selectedSportName = null;
    }

    public Sport getSport()           { return sport; }
    public League getLeague()         { return league; }
    public Team getUserTeam()         { return userTeam; }
    public MatchEngine getMatchEngine(){ return matchEngine; }
    public int getCurrentSeason()     { return currentSeason; }
    public String getSaveName()       { return saveName; }
    public String getSelectedSportName() { return selectedSportName; }
    public void setSaveName(String n) { this.saveName = n; }
    public void setSelectedSportName(String selectedSportName) { this.selectedSportName = selectedSportName; }
}
