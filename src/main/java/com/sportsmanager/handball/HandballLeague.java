package com.sportsmanager.handball;

import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.MatchResult;
import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.league.Fixture;
import com.sportsmanager.league.MatchDay;
import com.sportsmanager.league.StandingRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handball league — double round-robin, WIN=2 DRAW=1 LOSS=0.
 *
 * Tiebreaker order (when points are equal):
 *   1. Head-to-head points
 *   2. Head-to-head goal difference
 *   3. Head-to-head goals scored
 *   4. Overall goal difference
 *   5. Overall goals scored
 *
 * Implemented by: Yavuz Mete Afsar
 */
public class HandballLeague extends League {

    private final List<MatchDay> schedule = new ArrayList<>();
    private final Map<Team, StandingRow> standingsMap = new HashMap<>();

    /** Stores every recorded MatchResult keyed by each participating team. */
    private final Map<Team, List<MatchResult>> matchHistory = new HashMap<>();

    private int currentWeekIndex = 0;

    public HandballLeague(String name, List<Team> teams) {
        super(name, teams);
    }

    // ── Tiebreaker & points ───────────────────────────────────────────────────

    @Override
    public int compareTeams(StandingRow a, StandingRow b) {
        // 1. Overall points
        int cmp = Integer.compare(b.getPoints(), a.getPoints());
        if (cmp != 0) return cmp;

        // 2–4. Head-to-head
        cmp = compareHeadToHead(a.getTeam(), b.getTeam());
        if (cmp != 0) return cmp;

        // 5. Overall goal difference
        cmp = Integer.compare(b.getGoalDifference(), a.getGoalDifference());
        if (cmp != 0) return cmp;

        // 6. Overall goals scored
        return Integer.compare(b.getGoalsFor(), a.getGoalsFor());
    }

    /**
     * Returns a negative value when team a should rank above team b based on
     * their direct encounters (h2h points → h2h GD → h2h goals scored).
     * Returns 0 when they have not yet played each other.
     */
    private int compareHeadToHead(Team a, Team b) {
        List<MatchResult> history = matchHistory.getOrDefault(a, Collections.emptyList());

        int aH2HPoints = 0, bH2HPoints = 0;
        int aH2HGF = 0, aH2HGA = 0;

        for (MatchResult r : history) {
            boolean aIsHome = r.getHomeTeam().equals(a);
            Team opponent = aIsHome ? r.getAwayTeam() : r.getHomeTeam();
            if (!opponent.equals(b)) continue;

            aH2HPoints += getPointsForResult(r.getResultFor(a));
            bH2HPoints += getPointsForResult(r.getResultFor(b));

            if (aIsHome) {
                aH2HGF += r.getHomeScore();
                aH2HGA += r.getAwayScore();
            } else {
                aH2HGF += r.getAwayScore();
                aH2HGA += r.getHomeScore();
            }
        }

        // h2h points — more is better (descending: compare b vs a)
        int cmp = Integer.compare(bH2HPoints, aH2HPoints);
        if (cmp != 0) return cmp;

        // h2h goal difference
        int aH2HGD = aH2HGF - aH2HGA;
        int bH2HGD = aH2HGA - aH2HGF;
        cmp = Integer.compare(bH2HGD, aH2HGD);
        if (cmp != 0) return cmp;

        // h2h goals scored (b's GF = a's GA)
        return Integer.compare(aH2HGA, aH2HGF);
    }

    @Override
    public int getPointsForResult(String result) {
        return switch (result) {
            case "WIN"  -> 2;
            case "DRAW" -> 1;
            default     -> 0;
        };
    }

    @Override
    public int getTrainingDaysPerWeek() { return 4; }

    // ── Schedule generation ───────────────────────────────────────────────────

    /**
     * Generates a double round-robin schedule using the circle method.
     * For n teams: (n-1) first-leg rounds + (n-1) second-leg rounds = 2*(n-1) total weeks.
     */
    @Override
    public void generateSchedule() {
        List<Team> teams = new ArrayList<>(getTeams());
        int n = teams.size();

        schedule.clear();
        standingsMap.clear();
        matchHistory.clear();
        for (Team t : teams) standingsMap.put(t, new StandingRow(t));

        List<Team> circle = new ArrayList<>(teams);

        // First leg
        for (int round = 0; round < n - 1; round++) {
            MatchDay md = new MatchDay(round + 1);
            for (int i = 0; i < n / 2; i++) {
                Team home = circle.get(i);
                Team away = circle.get(n - 1 - i);
                if (i == 0 && round % 2 != 0) {
                    md.addFixture(new Fixture(away, home, round + 1));
                } else {
                    md.addFixture(new Fixture(home, away, round + 1));
                }
            }
            schedule.add(md);
            Team last = circle.remove(n - 1);
            circle.add(1, last);
        }

        // Second leg: swap home/away
        for (int round = 0; round < n - 1; round++) {
            int weekNum = (n - 1) + round + 1;
            MatchDay md = new MatchDay(weekNum);
            for (Fixture f : schedule.get(round).getFixtures()) {
                md.addFixture(new Fixture(f.getAway(), f.getHome(), weekNum));
            }
            schedule.add(md);
        }

        currentWeekIndex = 0;
    }

    // ── Result recording ──────────────────────────────────────────────────────

    @Override
    public void recordResult(MatchResult r) {
        Team home = r.getHomeTeam();
        Team away = r.getAwayTeam();

        // Mark fixture as played
        int week = r.getWeek();
        if (week >= 1 && week <= schedule.size()) {
            MatchDay md = schedule.get(week - 1);
            for (Fixture f : md.getFixtures()) {
                if (f.getHome().equals(home) && f.getAway().equals(away) && !f.isPlayed()) {
                    f.setResult(r);
                    break;
                }
            }
        }

        // Store for head-to-head lookups
        matchHistory.computeIfAbsent(home, k -> new ArrayList<>()).add(r);
        matchHistory.computeIfAbsent(away, k -> new ArrayList<>()).add(r);

        // Update standings rows
        StandingRow homeRow = standingsMap.get(home);
        StandingRow awayRow = standingsMap.get(away);
        if (homeRow == null || awayRow == null) return;

        homeRow.addMatchPlayed();
        awayRow.addMatchPlayed();
        homeRow.addGoalsFor(r.getHomeScore());
        homeRow.addGoalsAgainst(r.getAwayScore());
        awayRow.addGoalsFor(r.getAwayScore());
        awayRow.addGoalsAgainst(r.getHomeScore());

        String homeResult = r.getResultFor(home);
        String awayResult = r.getResultFor(away);

        switch (homeResult) {
            case "WIN"  -> { homeRow.addWin();  awayRow.addLoss(); }
            case "DRAW" -> { homeRow.addDraw(); awayRow.addDraw(); }
            default     -> { homeRow.addLoss(); awayRow.addWin(); }
        }

        homeRow.addPoints(getPointsForResult(homeResult));
        awayRow.addPoints(getPointsForResult(awayResult));
    }

    // ── Standings ─────────────────────────────────────────────────────────────

    @Override
    public List<StandingRow> getStandings() {
        List<StandingRow> rows = new ArrayList<>(standingsMap.values());
        rows.sort(this::compareTeams);
        return rows;
    }

    // ── Week navigation ───────────────────────────────────────────────────────

    @Override
    public MatchDay getCurrentMatchDay() {
        if (currentWeekIndex < schedule.size()) return schedule.get(currentWeekIndex);
        return null;
    }

    @Override
    public void advanceWeek() {
        if (currentWeekIndex < schedule.size()) {
            for (Team t : getTeams()) {
                t.conductWeeklyTraining();
                for (Player p : t.getSquad()) {
                    p.recoverOneGame();
                }
            }
            currentWeekIndex++;
        }
    }

    @Override
    public boolean isSeasonOver() {
        return currentWeekIndex >= schedule.size();
    }

    @Override
    public void startNewSeason() {
        standingsMap.clear();
        matchHistory.clear();
        for (Team t : getTeams()) standingsMap.put(t, new StandingRow(t));
        generateSchedule();
    }

    // ── Extra accessor ────────────────────────────────────────────────────────

    /** Returns an unmodifiable view of all match days (used by UI schedule view). */
    public List<MatchDay> getSchedule() {
        return Collections.unmodifiableList(schedule);
    }
}
