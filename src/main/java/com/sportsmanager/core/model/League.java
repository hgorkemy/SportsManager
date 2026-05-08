package com.sportsmanager.core.model;

import com.sportsmanager.league.Fixture;
import com.sportsmanager.league.MatchDay;
import com.sportsmanager.league.StandingRow;

import java.util.List;
import java.util.Map;


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

    /** Directly sets the current week index (used by save/load to restore position). */
    public abstract void restoreWeekIndex(int index);

    /**
     * Directly imports standings for one team by name (used by save/load).
     * Called after generateSchedule() resets standings to zero.
     */
    public abstract void importStanding(String teamName, int played, int wins, int draws,
                                        int losses, int goalsFor, int goalsAgainst, int points);

    /** Returns all MatchResults from already-played fixtures (used by save). */
    public abstract List<MatchResult> getPlayedResults();

    /** Returns the full list of match days in schedule order. */
    public abstract List<MatchDay> getSchedule();

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getName()         { return name; }
    public List<Team> getTeams()    { return java.util.Collections.unmodifiableList(teams); }
}
