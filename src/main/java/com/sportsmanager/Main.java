package com.sportsmanager;

import com.sportsmanager.core.engine.MatchEngine;
import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.MatchResult;
import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Sport;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.football.FootballFactory;
import com.sportsmanager.football.FootballPosition;
import com.sportsmanager.league.Fixture;
import com.sportsmanager.league.MatchDay;
import com.sportsmanager.league.StandingRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for console simulation.
 * Run with: mvn exec:java
 */

public class Main {

    public static void main(String[] args) {
        FootballFactory factory = new FootballFactory();

        Sport sport = factory.createSport();
        List<Team> teams = factory.generateTeams(20);
        League league = factory.createLeague(teams);
        MatchEngine engine = factory.createMatchEngine();

        System.out.println("=== Sports Manager - " + sport.getName() + " Season Simulation ===");
        System.out.println("Teams: " + teams.size() + " | Periods per match: " + sport.getNumberOfPeriods());
        System.out.println();

        int week = 1;

        while (!league.isSeasonOver()) {
            MatchDay matchDay = league.getCurrentMatchDay();
            System.out.println("-- Week " + week + " --");

            for (Fixture fixture : matchDay.getFixtures()) {
                Team home = fixture.getHome();
                Team away = fixture.getAway();

                autoSetLineup(home);
                autoSetLineup(away);

                MatchResult result = engine.simulateFullMatch(home, away);
                league.recordResult(result);

                System.out.printf("  %-20s %d - %d  %s%n",
                        home.getName(),
                        result.getHomeScore(),
                        result.getAwayScore(),
                        away.getName());
            }

            engine.resetMatch();
            league.advanceWeek();
            week++;
            System.out.println();
        }

        // Print final standings table
        System.out.println("-- FINAL STANDINGS --");
        System.out.printf("%-4s %-20s %3s %3s %3s %3s %3s %3s %4s %3s%n",
                "Pos", "Team", "MP", "W", "D", "L", "GF", "GA", "GD", "Pts");
        System.out.println("-".repeat(60));

        List<StandingRow> standings = league.getStandings();
        for (int i = 0; i < standings.size(); i++) {
            StandingRow row = standings.get(i);
            System.out.printf("%-4d %-20s %3d %3d %3d %3d %3d %3d %+4d %3d%n",
                    i + 1,
                    row.getTeam().getName(),
                    row.getMatchesPlayed(),
                    row.getWins(),
                    row.getDraws(),
                    row.getLosses(),
                    row.getGoalsFor(),
                    row.getGoalsAgainst(),
                    row.getGoalDifference(),
                    row.getPoints());
        }

        System.out.println();
        if (!standings.isEmpty()) {
            System.out.println("Champion: " + standings.get(0).getTeam().getName());
        }
    }

    // if lineUp is not set, build a valid starting 11 with 1 GK and 10 outfield
    // players
    private static void autoSetLineup(Team team) {
        if (!team.getLineup().isEmpty())
            return;

        List<Player> healthy = team.getHealthyPlayers();

        // Split goalkeepers and outfield players
        List<Player> gks = new ArrayList<>();
        List<Player> outfield = new ArrayList<>();
        for (Player p : healthy) {
            if (p.getPosition() == FootballPosition.GOALKEEPER) {
                gks.add(p);
            } else {
                outfield.add(p);
            }
        }

        // Need at least 1 GK and 10 outfield
        if (gks.isEmpty() || outfield.size() < 10)
            return;

        List<Player> eleven = new ArrayList<>();
        eleven.add(gks.get(0));
        eleven.addAll(outfield.subList(0, 10));

        team.setLineup(eleven);
    }
}
