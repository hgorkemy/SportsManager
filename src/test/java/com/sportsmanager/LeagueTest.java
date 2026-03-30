package com.sportsmanager;

import com.sportsmanager.core.model.MatchResult;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.football.FootballFactory;
import com.sportsmanager.football.FootballLeague;
import com.sportsmanager.league.Fixture;
import com.sportsmanager.league.MatchDay;
import com.sportsmanager.league.StandingRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FootballLeague schedule generation, result recording, and standings.
 *
 * Implemented by: Yavuz Mete Afsar
 */
class LeagueTest {

    private FootballLeague league;

    @BeforeEach
    void setUp() {
        FootballFactory factory = new FootballFactory();
        List<Team> teams = factory.generateTeams(20);
        league = (FootballLeague) factory.createLeague(teams);
    }

    // ── Test 1 ────────────────────────────────────────────────────────────────

    /** 20 teams in a double round-robin produces exactly 38 match days. */
    @Test
    void generateSchedule_20Teams_creates38MatchDays() {
        assertEquals(38, league.getSchedule().size(),
                "Double round-robin with 20 teams must have 2*(20-1) = 38 weeks");
    }

    // ── Test 2 ────────────────────────────────────────────────────────────────

    /**
     * Each team plays exactly 38 matches across the full schedule
     * (19 home + 19 away = 38 total).
     */
    @Test
    void generateSchedule_eachTeamPlays38Matches() {
        // With 20 teams, 10 fixtures per week × 38 weeks = 380 fixtures total
        int totalFixtures = league.getSchedule().stream()
                .mapToInt(md -> md.getFixtures().size())
                .sum();
        assertEquals(380, totalFixtures,
                "20 teams × 38 matches / 2 teams per fixture = 380 total fixtures");
    }

    // ── Test 3 ────────────────────────────────────────────────────────────────

    /** A WIN awards exactly 3 points to the winning team and 0 to the loser. */
    @Test
    void recordResult_win_gives3PointsToWinner() {
        MatchDay md = league.getCurrentMatchDay();
        Fixture fixture = md.getFixtures().get(0);
        Team home = fixture.getHome();
        Team away = fixture.getAway();

        MatchResult result = new MatchResult(home, away, 1, md.getWeekNumber());
        result.addHomeGoal(); // home wins 1-0

        league.recordResult(result);

        StandingRow homeRow = league.getStandings().stream()
                .filter(r -> r.getTeam().equals(home))
                .findFirst().orElseThrow();
        StandingRow awayRow = league.getStandings().stream()
                .filter(r -> r.getTeam().equals(away))
                .findFirst().orElseThrow();

        assertEquals(3, homeRow.getPoints(), "Winner must receive 3 points");
        assertEquals(0, awayRow.getPoints(), "Loser must receive 0 points");
    }

    // ── Test 4 ────────────────────────────────────────────────────────────────

    /** A DRAW awards exactly 1 point to each team. */
    @Test
    void recordResult_draw_gives1PointToEachTeam() {
        MatchDay md = league.getCurrentMatchDay();
        Fixture fixture = md.getFixtures().get(0);
        Team home = fixture.getHome();
        Team away = fixture.getAway();

        MatchResult result = new MatchResult(home, away, 1, md.getWeekNumber());
        // 0-0 draw — no goals added

        league.recordResult(result);

        StandingRow homeRow = league.getStandings().stream()
                .filter(r -> r.getTeam().equals(home)).findFirst().orElseThrow();
        StandingRow awayRow = league.getStandings().stream()
                .filter(r -> r.getTeam().equals(away)).findFirst().orElseThrow();

        assertEquals(1, homeRow.getPoints(), "Home team must receive 1 point in a draw");
        assertEquals(1, awayRow.getPoints(), "Away team must receive 1 point in a draw");
    }

    // ── Test 5 ────────────────────────────────────────────────────────────────

    /**
     * getStandings() returns rows sorted strictly by points descending;
     * on equal points, the team with the higher goal difference ranks above.
     */
    @Test
    void getStandings_sortedByPointsThenGoalDifference() {
        MatchDay md = league.getCurrentMatchDay();
        List<Fixture> fixtures = md.getFixtures();

        // Fixture 0: home wins 3-0  →  home: 3pts GD+3 / away: 0pts GD-3
        Fixture f0 = fixtures.get(0);
        MatchResult r0 = new MatchResult(f0.getHome(), f0.getAway(), 1, md.getWeekNumber());
        r0.addHomeGoal(); r0.addHomeGoal(); r0.addHomeGoal();
        league.recordResult(r0);

        // Fixture 1: home wins 1-0  →  home: 3pts GD+1 / away: 0pts GD-1
        Fixture f1 = fixtures.get(1);
        MatchResult r1 = new MatchResult(f1.getHome(), f1.getAway(), 1, md.getWeekNumber());
        r1.addHomeGoal();
        league.recordResult(r1);

        List<StandingRow> standings = league.getStandings();

        // All rows must be in non-increasing points order
        for (int i = 0; i + 1 < standings.size(); i++) {
            assertTrue(standings.get(i).getPoints() >= standings.get(i + 1).getPoints(),
                    "Standings not sorted at index " + i);
        }

        // f0.home (3pts, GD+3) must rank above f1.home (3pts, GD+1)
        int posF0Home = indexOf(standings, f0.getHome());
        int posF1Home = indexOf(standings, f1.getHome());
        assertTrue(posF0Home < posF1Home,
                "Team with GD +3 must rank above team with GD +1 when both have 3 points");
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private int indexOf(List<StandingRow> rows, Team team) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).getTeam().equals(team)) return i;
        }
        return -1;
    }
}
