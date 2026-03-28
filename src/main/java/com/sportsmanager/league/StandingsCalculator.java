package com.sportsmanager.league;

import java.util.List;

/**
 * Interface for standings sorting logic.
 *
 * TODO (Yavuz): Implement FootballStandingsCalculator.
 */
public interface StandingsCalculator {
    List<StandingRow> sortStandings(List<StandingRow> rows);
}
