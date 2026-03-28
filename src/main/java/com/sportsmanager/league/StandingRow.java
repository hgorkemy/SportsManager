package com.sportsmanager.league;

import com.sportsmanager.core.model.Team;

/**
 * One team's row in the league standings table.
 *
 * TODO (Yavuz): Implement this class.
 */
public class StandingRow {

    private final Team team;
    private int matchesPlayed;
    private int wins;
    private int draws;
    private int losses;
    private int goalsFor;
    private int goalsAgainst;
    private int points;

    public StandingRow(Team team) {
        this.team = team;
    }

    public void addMatchPlayed()       { matchesPlayed++; }
    public void addWin()               { wins++; }
    public void addDraw()              { draws++; }
    public void addLoss()              { losses++; }
    public void addGoalsFor(int g)     { goalsFor += g; }
    public void addGoalsAgainst(int g) { goalsAgainst += g; }
    public void addPoints(int p)       { points += p; }

    public int getGoalDifference()  { return goalsFor - goalsAgainst; }

    public Team getTeam()           { return team; }
    public int getMatchesPlayed()   { return matchesPlayed; }
    public int getWins()            { return wins; }
    public int getDraws()           { return draws; }
    public int getLosses()          { return losses; }
    public int getGoalsFor()        { return goalsFor; }
    public int getGoalsAgainst()    { return goalsAgainst; }
    public int getPoints()          { return points; }

    @Override
    public String toString() {
        return String.format("%-20s P:%2d W:%2d D:%2d L:%2d GF:%2d GA:%2d GD:%+2d Pts:%2d",
                team.getName(), matchesPlayed, wins, draws, losses,
                goalsFor, goalsAgainst, getGoalDifference(), points);
    }
}
