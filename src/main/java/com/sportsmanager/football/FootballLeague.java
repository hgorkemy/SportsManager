package com.sportsmanager.football;

import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.MatchResult;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.league.MatchDay;
import com.sportsmanager.league.StandingRow;

import java.util.List;

/**
 * Football league — double round-robin, WIN=3 DRAW=1 LOSS=0.
 *
 * TODO (Yavuz): Implement all abstract methods.
 */
public class FootballLeague extends League {

    public FootballLeague(String name, List<Team> teams) {
        super(name, teams);
    }

    @Override
    public int compareTeams(StandingRow a, StandingRow b) {
        // TODO (Yavuz): head-to-head → goal difference → coin toss
        return b.getGoalDifference() - a.getGoalDifference();
    }

    @Override
    public int getPointsForResult(String result) {
        return switch (result) {
            case "WIN"  -> 3;
            case "DRAW" -> 1;
            default     -> 0;
        };
    }

    @Override public int getTrainingDaysPerWeek() { return 5; }

    @Override public void generateSchedule()             { /* TODO (Yavuz) */ }
    @Override public void recordResult(MatchResult r)    { /* TODO (Yavuz) */ }
    @Override public List<StandingRow> getStandings()    { return List.of(); /* TODO (Yavuz) */ }
    @Override public MatchDay getCurrentMatchDay()       { return null; /* TODO (Yavuz) */ }
    @Override public void advanceWeek()                  { /* TODO (Yavuz) */ }
    @Override public boolean isSeasonOver()              { return false; /* TODO (Yavuz) */ }
    @Override public void startNewSeason()               { /* TODO (Yavuz) */ }
}
