package com.sportsmanager.football;

import com.sportsmanager.core.engine.MatchEngine;
import com.sportsmanager.core.factory.SportFactory;
import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.Sport;
import com.sportsmanager.core.model.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates all Football-specific objects.
 * This is the only class the UI uses to bootstrap a new football game.
 *
 * TODO (Irmak ): Implement generateTeams() with realistic random players.
 */
public class FootballFactory implements SportFactory {

    private static final String[] TEAM_NAMES = {
        "Galatasaray", "Fenerbahce", "Besiktas", "Trabzonspor",
        "Basaksehir", "Sivasspor", "Alanyaspor", "Antalyaspor",
        "Kasimpasa", "Konyaspor", "Gaziantep FK", "Hatayspor",
        "Rizespor", "Pendikspor", "Kayserispor", "Ankaragücü",
        "Samsunspor", "Fatih Karagümrük", "Istanbulspor", "Adana Demirspor"
    };

    @Override
    public Sport createSport() {
        return new FootballSport();
    }

    @Override
    public List<Team> generateTeams(int count) {
        // TODO : Generate teams with full squads of FootballPlayers
        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < Math.min(count, TEAM_NAMES.length); i++) {
            FootballTeam team = new FootballTeam(TEAM_NAMES[i], null);
            // TODO : Add players to each team
            teams.add(team);
        }
        return teams;
    }

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
