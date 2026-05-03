package com.sportsmanager.handball;

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
 * Creates all Handball-specific objects.
 * Team and player names are loaded from src/main/resources/data/handball_teams.json.
 *
 * Implemented by: Yavuz Mete Afsar
 */
public class HandballFactory implements SportFactory {

    private static final String DATA_PATH = "/data/handball_teams.json";

    private static final String[] COACH_SPECIALTIES = {
        "Attack", "Defense", "Goalkeeping", "Fitness"
    };

    @Override
    public Sport createSport() {
        return new HandballSport();
    }

    @Override
    public List<Team> generateTeams(int count) {
        List<String> teamNames  = TeamDataLoader.loadField(DATA_PATH, "teams");
        List<String> firstNames = TeamDataLoader.loadField(DATA_PATH, "firstNames");
        List<String> lastNames  = TeamDataLoader.loadField(DATA_PATH, "lastNames");
        List<String> coachFirst = TeamDataLoader.loadField(DATA_PATH, "coachFirstNames");
        List<String> coachLast  = TeamDataLoader.loadField(DATA_PATH, "coachLastNames");

        List<Team> teams = new ArrayList<>();
        Random rng = new Random();

        int limit = Math.min(count, teamNames.size());
        for (int i = 0; i < limit; i++) {
            HandballTeam team = new HandballTeam(teamNames.get(i), null);

            // Squad: 2 GK, 4 WING, 6 BACK, 4 PIVOT = 16 players
            addPlayers(team, HandballPosition.GOALKEEPER, 2, firstNames, lastNames, rng);
            addPlayers(team, HandballPosition.WING,       4, firstNames, lastNames, rng);
            addPlayers(team, HandballPosition.BACK,       6, firstNames, lastNames, rng);
            addPlayers(team, HandballPosition.PIVOT,      4, firstNames, lastNames, rng);

            // One coach
            String cf = coachFirst.get(rng.nextInt(coachFirst.size()));
            String cl = coachLast.get(rng.nextInt(coachLast.size()));
            String sp = COACH_SPECIALTIES[rng.nextInt(COACH_SPECIALTIES.length)];
            team.addCoach(new HandballCoach(cf, cl, 38 + rng.nextInt(20), 5 + rng.nextInt(20), sp));

            teams.add(team);
        }
        return teams;
    }

    private void addPlayers(HandballTeam team, HandballPosition position,
                            int count, List<String> firstNames,
                            List<String> lastNames, Random rng) {
        for (int i = 0; i < count; i++) {
            String first = firstNames.get(rng.nextInt(firstNames.size()));
            String last  = lastNames.get(rng.nextInt(lastNames.size()));
            int age  = 18 + rng.nextInt(18);
            int base = 50 + rng.nextInt(25);
            team.addPlayer(new HandballPlayer(
                first, last, age, position,
                clamp(base + rng.nextInt(20) - 10),  // throwing
                clamp(base + rng.nextInt(20) - 10),  // speed
                clamp(base + rng.nextInt(20) - 10),  // agility
                clamp(base + rng.nextInt(20) - 10),  // jumping
                clamp(base + rng.nextInt(20) - 10),  // defending
                clamp(base + rng.nextInt(20) - 10)   // stamina
            ));
        }
    }

    private int clamp(int v) { return Math.max(40, Math.min(90, v)); }

    @Override
    public League createLeague(List<Team> teams) {
        HandballLeague league = new HandballLeague("Handball Champions League", teams);
        league.generateSchedule();
        return league;
    }

    @Override
    public MatchEngine createMatchEngine() {
        // TODO: implement HandballMatchEngine (separate task)
        throw new UnsupportedOperationException("HandballMatchEngine not yet implemented");
    }
}
