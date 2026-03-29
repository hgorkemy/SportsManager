package com.sportsmanager.football;

import com.sportsmanager.core.engine.MatchEngine;
import com.sportsmanager.core.factory.SportFactory;
import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.Sport;
import com.sportsmanager.core.model.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Creates all Football-specific objects.
 * This is the only class the UI uses to bootstrap a new football game.
 */
public class FootballFactory implements SportFactory {

    private static final String[] TEAM_NAMES = {
        "Galatasaray", "Fenerbahce", "Besiktas", "Trabzonspor",
        "Basaksehir", "Sivasspor", "Alanyaspor", "Antalyaspor",
        "Kasimpasa", "Konyaspor", "Gaziantep FK", "Hatayspor",
        "Rizespor", "Pendikspor", "Kayserispor", "Ankaragücü",
        "Samsunspor", "Fatih Karagümrük", "Istanbulspor", "Adana Demirspor"
    };

    private static final String[] FIRST_NAMES = {
        "Carlos", "Marco", "Lucas", "Pedro", "Joao", "Luis", "Antonio", "Roberto", "Sergio", "Diego",
        "Ahmet", "Mehmet", "Mustafa", "Ibrahim", "Hasan", "Ali", "Kemal", "Burak", "Arda", "Emre",
        "James", "Thomas", "Oliver", "Harry", "George", "Noah", "Liam", "Mason", "Jack", "Charlie"
    };

    private static final String[] LAST_NAMES = {
        "Silva", "Santos", "Costa", "Ferreira", "Oliveira", "Souza", "Lima", "Pereira", "Alves", "Carvalho",
        "Yilmaz", "Kaya", "Demir", "Sahin", "Celik", "Arslan", "Aydin", "Dogan", "Polat", "Cetin",
        "Smith", "Jones", "Brown", "Taylor", "Davies", "Wilson", "Evans", "Thomas", "Roberts", "Lewis"
    };

    private static final String[] COACH_FIRST = {
        "Jose", "Pep", "Carlo", "Jurgen", "Diego", "Fatih", "Aykut", "Okan", "Abdullah", "Erik"
    };

    private static final String[] COACH_LAST = {
        "Mourinho", "Guardiola", "Ancelotti", "Klopp", "Simeone", "Terim", "Kocaman", "Buruk", "Avci", "ten Hag"
    };

    private static final String[] SPECIALTIES = {"Attack", "Defense", "Fitness", "Goalkeeping"};

    @Override
    public Sport createSport() {
        return new FootballSport();
    }

    @Override
    public List<Team> generateTeams(int count) {
        List<Team> teams = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < Math.min(count, TEAM_NAMES.length); i++) {
            FootballTeam team = new FootballTeam(TEAM_NAMES[i], null);

            // Squad: 2 GK, 5 DEF, 5 MID, 5 FWD = 17 players
            addPlayers(team, FootballPosition.GOALKEEPER, 2, random);
            addPlayers(team, FootballPosition.DEFENDER,   5, random);
            addPlayers(team, FootballPosition.MIDFIELDER, 5, random);
            addPlayers(team, FootballPosition.FORWARD,    5, random);

            // Default tactic
            team.setCurrentTactic(FootballTactic.balanced());

            // One coach
            String cf = COACH_FIRST[random.nextInt(COACH_FIRST.length)];
            String cl = COACH_LAST[random.nextInt(COACH_LAST.length)];
            String sp = SPECIALTIES[random.nextInt(SPECIALTIES.length)];
            team.addCoach(new FootballCoach(cf, cl, 38 + random.nextInt(20), 5 + random.nextInt(20), sp));

            teams.add(team);
        }
        return teams;
    }

    private void addPlayers(FootballTeam team, FootballPosition position, int count, Random rng) {
        for (int i = 0; i < count; i++) {
            String first = FIRST_NAMES[rng.nextInt(FIRST_NAMES.length)];
            String last  = LAST_NAMES[rng.nextInt(LAST_NAMES.length)];
            int age = 18 + rng.nextInt(18);
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
