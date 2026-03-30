package com.sportsmanager.football;

import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.MatchResult;
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
 * Football league — double round-robin, WIN=3 DRAW=1 LOSS=0.
 *
 * Implemented by: Yavuz Mete Afsar
 */
public class FootballLeague extends League {

    private final List<MatchDay> schedule = new ArrayList<>();
    private final Map<Team, StandingRow> standingsMap = new HashMap<>();
    private final FootballStandingsCalculator calculator = new FootballStandingsCalculator();
    private int currentWeekIndex = 0;

    public FootballLeague(String name, List<Team> teams) {
        super(name, teams);
    }

    // ── Tiebreaker & points ───────────────────────────────────────────────────

    @Override
    public int compareTeams(StandingRow a, StandingRow b) {
        int cmp = Integer.compare(b.getPoints(), a.getPoints());
        if (cmp != 0) return cmp;
        cmp = Integer.compare(b.getGoalDifference(), a.getGoalDifference());
        if (cmp != 0) return cmp;
        return Integer.compare(b.getGoalsFor(), a.getGoalsFor());
    }

    @Override
    public int getPointsForResult(String result) {
        return switch (result) {
            case "WIN"  -> 3;
            case "DRAW" -> 1;
            default     -> 0;
        };
    }

    @Override
    public int getTrainingDaysPerWeek() { return 5; }

    // ── Schedule generation ───────────────────────────────────────────────────

    /**
     * Generates a double round-robin schedule using the circle method.
     * For n teams: (n-1) rounds first leg + (n-1) rounds second leg = 2*(n-1) total weeks.
     * With 20 teams: 38 weeks, 10 fixtures per week.
     */
    @Override
    public void generateSchedule() {
        List<Team> teams = new ArrayList<>(getTeams());
        int n = teams.size();

        schedule.clear();
        standingsMap.clear();
        for (Team t : teams) standingsMap.put(t, new StandingRow(t));

        // Circle method: fix teams.get(0) at slot 0, rotate the rest
        List<Team> circle = new ArrayList<>(teams);

        // First leg
        for (int round = 0; round < n - 1; round++) {
            MatchDay md = new MatchDay(round + 1);
            for (int i = 0; i < n / 2; i++) {
                Team home = circle.get(i);
                Team away = circle.get(n - 1 - i);
                // Alternate home advantage for the fixed team (index 0) each round
                if (i == 0 && round % 2 != 0) {
                    md.addFixture(new Fixture(away, home, round + 1));
                } else {
                    md.addFixture(new Fixture(home, away, round + 1));
                }
            }
            schedule.add(md);
            // Rotate: move last element to position 1 (keeping position 0 fixed)
            Team last = circle.remove(n - 1);
            circle.add(1, last);
        }

        // Second leg: swap home/away from first leg
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

        // Find the unplayed fixture matching home/away in the correct week
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
        return calculator.sortStandings(rows);
    }

    // ── Week navigation ───────────────────────────────────────────────────────

    @Override
    public MatchDay getCurrentMatchDay() {
        if (currentWeekIndex < schedule.size()) return schedule.get(currentWeekIndex);
        return null;
    }

    @Override
    public void advanceWeek() {
        if (currentWeekIndex < schedule.size()) currentWeekIndex++;
    }

    @Override
    public boolean isSeasonOver() {
        return currentWeekIndex >= schedule.size();
    }

    @Override
    public void startNewSeason() {
        standingsMap.clear();
        for (Team t : getTeams()) standingsMap.put(t, new StandingRow(t));
        generateSchedule();
    }

    // ── Extra accessor ────────────────────────────────────────────────────────

    /** Returns an unmodifiable view of all match days (used by ScheduleController). */
    public List<MatchDay> getSchedule() {
        return Collections.unmodifiableList(schedule);
    }
}
