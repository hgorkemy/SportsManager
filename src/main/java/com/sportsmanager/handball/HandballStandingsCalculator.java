package com.sportsmanager.handball;

import com.sportsmanager.league.StandingRow;
import com.sportsmanager.league.StandingsCalculator;

import java.util.Comparator;
import java.util.List;

/**
 * Basic handball standings sort: points → goal difference → goals scored.
 * Head-to-head tiebreaker is applied by HandballLeague.compareTeams() before
 * this fallback order is reached.
 *
 * Implemented by: Yavuz Mete Afsar
 */
public class HandballStandingsCalculator implements StandingsCalculator {

    @Override
    public List<StandingRow> sortStandings(List<StandingRow> rows) {
        rows.sort(Comparator.comparingInt(StandingRow::getPoints).reversed()
                .thenComparing(Comparator.comparingInt(StandingRow::getGoalDifference).reversed())
                .thenComparing(Comparator.comparingInt(StandingRow::getGoalsFor).reversed()));
        return rows;
    }
}
