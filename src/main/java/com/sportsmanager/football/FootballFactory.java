package com.sportsmanager.football;

import com.sportsmanager.core.engine.MatchEngine;
import com.sportsmanager.core.factory.SportFactory;
import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.Sport;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.util.TeamDataLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Creates all Football-specific objects.
 * Team, player and coach names are loaded from src/main/resources/data/football_teams.json.
 */
public class FootballFactory implements SportFactory {

    private static final String DATA_PATH = "/data/football_teams.json";

    private static final String[] SPECIALTIES = {"Attack", "Defense", "Fitness", "Goalkeeping"};

    @Override
    public Sport createSport() {
        return new FootballSport();
    }

    @Override
    public List<Team> generateTeams(int count) {
        List<String> teamNames  = TeamDataLoader.loadField(DATA_PATH, "teams");
        List<String> logos      = TeamDataLoader.loadField(DATA_PATH, "logos");
        List<String> firstNames = TeamDataLoader.loadField(DATA_PATH, "firstNames");
        List<String> lastNames  = TeamDataLoader.loadField(DATA_PATH, "lastNames");
        List<String> coachFirst = TeamDataLoader.loadField(DATA_PATH, "coachFirstNames");
        List<String> coachLast  = TeamDataLoader.loadField(DATA_PATH, "coachLastNames");

        List<Team> teams = new ArrayList<>();
        Random rng = new Random();

        int limit = Math.min(count, teamNames.size());
        for (int i = 0; i < limit; i++) {
            String logoPath = i < logos.size() ? logos.get(i) : null;
            FootballTeam team = new FootballTeam(teamNames.get(i), logoPath);

            // Squad: 2 GK, 5 DEF, 5 MID, 5 FWD = 17 players
            addPlayers(team, FootballPosition.GOALKEEPER, 2, firstNames, lastNames, rng);
            addPlayers(team, FootballPosition.DEFENDER,   5, firstNames, lastNames, rng);
            addPlayers(team, FootballPosition.MIDFIELDER, 5, firstNames, lastNames, rng);
            addPlayers(team, FootballPosition.FORWARD,    5, firstNames, lastNames, rng);

            // Default tactic
            team.setCurrentTactic(FootballTactic.balanced());

            // One coach
            String cf = coachFirst.get(rng.nextInt(coachFirst.size()));
            String cl = coachLast.get(rng.nextInt(coachLast.size()));
            String sp = SPECIALTIES[rng.nextInt(SPECIALTIES.length)];
            team.addCoach(new FootballCoach(cf, cl, 38 + rng.nextInt(20), 5 + rng.nextInt(20), sp));

            teams.add(team);
        }
        return teams;
    }

    private void addPlayers(FootballTeam team, FootballPosition position,
                            int count, List<String> firstNames,
                            List<String> lastNames, Random rng) {
        for (int i = 0; i < count; i++) {
            String first = firstNames.get(rng.nextInt(firstNames.size()));
            String last  = lastNames.get(rng.nextInt(lastNames.size()));
            int age  = 18 + rng.nextInt(18);
            int base = 50 + rng.nextInt(25);
            team.addPlayer(new FootballPlayer(
                first, last, age, position,
                clamp(base + rng.nextInt(20) - 10),  // speed
                clamp(base + rng.nextInt(20) - 10),  // shooting
                clamp(base + rng.nextInt(20) - 10),  // passing
                clamp(base + rng.nextInt(20) - 10),  // ballControl
                clamp(base + rng.nextInt(20) - 10),  // defending
                clamp(base + rng.nextInt(20) - 10)   // physicality
            ));
        }
    }

    private int clamp(int v) { return Math.max(40, Math.min(90, v)); }

    @Override
    public League createLeague(List<Team> teams) {
        FootballLeague league = new FootballLeague("Super League", teams);
        league.generateSchedule();
        return league;
    }

    @Override
    public MatchEngine createMatchEngine() {
        return new FootballMatchEngine();
    }
}
