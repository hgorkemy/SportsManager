package com.sportsmanager.football;

import com.sportsmanager.league.StandingRow;
import com.sportsmanager.league.StandingsCalculator;

import java.util.Comparator;
import java.util.List;

/**
 * Sorts standings: points → goal difference → goals scored.
 *
 * TODO (Yavuz): Implement fully with head-to-head tiebreaker.
 */
public class FootballStandingsCalculator implements StandingsCalculator {

    @Override
    public List<StandingRow> sortStandings(List<StandingRow> rows) {
        // TODO (Yavuz): Add full tiebreaker logic
        rows.sort(Comparator
                .comparingInt(StandingRow::getPoints).reversed()
                .thenComparingInt(StandingRow::getGoalDifference).reversed()
                .thenComparingInt(StandingRow::getGoalsFor).reversed());
        return rows;
    }
}
