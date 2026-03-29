package com.sportsmanager.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Holds the final result of a played match.
 */
public class MatchResult {

    private final Team homeTeam;
    private final Team awayTeam;
    private int homeScore;
    private int awayScore;
    private final List<MatchEvent> events;
    private final int season;
    private final int week;

    public MatchResult(Team homeTeam, Team awayTeam, int season, int week) {
        this.homeTeam = Objects.requireNonNull(homeTeam, "homeTeam cannot be null");
        this.awayTeam = Objects.requireNonNull(awayTeam, "awayTeam cannot be null");
        if (homeTeam.equals(awayTeam)) {
            throw new IllegalArgumentException("A match requires two different teams");
        }
        if (season < 1) {
            throw new IllegalArgumentException("season must be at least 1");
        }
        if (week < 1) {
            throw new IllegalArgumentException("week must be at least 1");
        }
        this.homeScore = 0;
        this.awayScore = 0;
        this.events = new ArrayList<>();
        this.season = season;
        this.week = week;
    }

    public void addHomeGoal()             { homeScore++; }
    public void addAwayGoal()             { awayScore++; }
    public void addEvent(MatchEvent e)    { events.add(Objects.requireNonNull(e, "event cannot be null")); }

    public boolean isHomeWin()  { return homeScore > awayScore; }
    public boolean isAwayWin()  { return awayScore > homeScore; }
    public boolean isDraw()     { return homeScore == awayScore; }

    public String getResultFor(Team team) {
        if (team.equals(homeTeam)) return isHomeWin() ? "WIN" : isDraw() ? "DRAW" : "LOSS";
        if (team.equals(awayTeam)) return isAwayWin() ? "WIN" : isDraw() ? "DRAW" : "LOSS";
        throw new IllegalArgumentException(team.getName() + " not in this match");
    }

    public Team getWinner() {
        if (isHomeWin()) return homeTeam;
        if (isAwayWin()) return awayTeam;
        return null;
    }

    public Team getHomeTeam()               { return homeTeam; }
    public Team getAwayTeam()               { return awayTeam; }
    public int getHomeScore()               { return homeScore; }
    public int getAwayScore()               { return awayScore; }
    public List<MatchEvent> getEvents()     { return Collections.unmodifiableList(events); }
    public int getSeason()                  { return season; }
    public int getWeek()                    { return week; }

    @Override
    public String toString() {
        return homeTeam.getName() + " " + homeScore + " - " + awayScore + " " + awayTeam.getName();
    }
}
