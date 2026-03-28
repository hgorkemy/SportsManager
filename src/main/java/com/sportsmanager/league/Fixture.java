package com.sportsmanager.league;

import com.sportsmanager.core.model.MatchResult;
import com.sportsmanager.core.model.Team;

/**
 * One match pairing. result is null until the match is played.
 *
 * TODO (Yavuz): Implement this class.
 */
public class Fixture {

    private final Team home;
    private final Team away;
    private final int week;
    private MatchResult result;

    public Fixture(Team home, Team away, int week) {
        this.home = home;
        this.away = away;
        this.week = week;
    }

    public boolean isPlayed()             { return result != null; }
    public void setResult(MatchResult r)  { this.result = r; }

    public Team getHome()           { return home; }
    public Team getAway()           { return away; }
    public int getWeek()            { return week; }
    public MatchResult getResult()  { return result; }

    @Override
    public String toString() {
        if (isPlayed()) return home + " " + result.getHomeScore() + "-" + result.getAwayScore() + " " + away;
        return home + " vs " + away + " (Week " + week + ")";
    }
}
