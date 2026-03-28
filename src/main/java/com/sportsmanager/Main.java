package com.sportsmanager;

import com.sportsmanager.core.engine.MatchEngine;
import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.Sport;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.football.FootballFactory;

import java.util.List;

/**
 * Entry point for console simulation.
 * Run with: mvn exec:java
 *
 * TODO (Berke): Implement full season simulation with console output.
 * - Generate 20 teams
 * - Simulate all 38 weeks
 * - Print match results each week
 * - Print final standings
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Sports Manager — Season Simulation ===");

        FootballFactory factory = new FootballFactory();
        Sport sport      = factory.createSport();
        List<Team> teams = factory.generateTeams(20);
        League league    = factory.createLeague(teams);
        MatchEngine engine = factory.createMatchEngine();

        System.out.println("Sport: " + sport.getName());
        System.out.println("Teams: " + teams.size());
        System.out.println("Season simulation starting...");

        // TODO (Berke): Implement week-by-week simulation loop
        // while (!league.isSeasonOver()) {
        //     MatchDay md = league.getCurrentMatchDay();
        //     ... simulate all fixtures ...
        //     league.advanceWeek();
        // }
        // Print standings

        System.out.println("Simulation complete. (TODO: implement full loop)");
    }
}
