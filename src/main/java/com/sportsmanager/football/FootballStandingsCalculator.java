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
        // Points desc → goal difference desc → goals for desc
        rows.sort(Comparator.comparingInt(StandingRow::getPoints).reversed()
                .thenComparing(Comparator.comparingInt(StandingRow::getGoalDifference).reversed())
                .thenComparing(Comparator.comparingInt(StandingRow::getGoalsFor).reversed()));
        return rows;
    }
}
