package com.sportsmanager.league;

import java.util.List;


public interface StandingsCalculator {
    List<StandingRow> sortStandings(List<StandingRow> rows);
}
