package com.sportsmanager.core.model;

import com.sportsmanager.league.Fixture;
import com.sportsmanager.league.MatchDay;
import com.sportsmanager.league.StandingRow;

import java.util.List;

/**
 * Abstract league class. Handles schedule generation and standings.
 *
 * TODO (Yavuz): Implement this class. FootballLeague extends this.
 */
public abstract class League {

    private final String name;
    private final List<Team> teams;

    protected League(String name, List<Team> teams) {
        this.name = name;
        this.teams = new java.util.ArrayList<>(teams);
    }

    // ── Abstract methods ──────────────────────────────────────────────────────

    /** Tiebreaker logic — compares two teams with equal points. */
    public abstract int compareTeams(StandingRow a, StandingRow b);

    /** Points awarded for WIN, DRAW, LOSS. */
    public abstract int getPointsForResult(String result);

    /** Number of training days between match days. */
    public abstract int getTrainingDaysPerWeek();

    // ── Template Method — generate double round-robin schedule ────────────────

    /** TODO (Yavuz): Implement round-robin schedule generation. */
    public abstract void generateSchedule();

    /** TODO (Yavuz): Record a match result and update standings. */
    public abstract void recordResult(MatchResult result);

    /** TODO (Yavuz): Return sorted standings. */
    public abstract List<StandingRow> getStandings();

    /** TODO (Yavuz): Return current week's MatchDay. */
    public abstract MatchDay getCurrentMatchDay();

    /** TODO (Yavuz): Advance to next week. */
    public abstract void advanceWeek();

    /** TODO (Yavuz): True when all match days are done. */
    public abstract boolean isSeasonOver();

    /** TODO (Yavuz): Reset for new season. */
    public abstract void startNewSeason();

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getName()         { return name; }
    public List<Team> getTeams()    { return java.util.Collections.unmodifiableList(teams); }
}
