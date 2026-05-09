package com.sportsmanager.football;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
 * Each team's real squad is loaded from src/main/resources/data/football_teams.json.
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
        List<String> coachFirst = TeamDataLoader.loadField(DATA_PATH, "coachFirstNames");
        List<String> coachLast  = TeamDataLoader.loadField(DATA_PATH, "coachLastNames");
        JsonArray    squads     = TeamDataLoader.loadRawArray(DATA_PATH, "squads");

        List<Team> teams = new ArrayList<>();
        Random rng = new Random();

        int limit = Math.min(count, teamNames.size());
        for (int i = 0; i < limit; i++) {
            String logoPath = i < logos.size() ? logos.get(i) : null;
            FootballTeam team = new FootballTeam(teamNames.get(i), logoPath);

            // Load this team's real squad from the JSON squads array
            JsonObject squad = squads.get(i).getAsJsonObject();
            List<String> gkFirst  = jsonArrayToList(squad.getAsJsonArray("gkFirst"));
            List<String> gkLast   = jsonArrayToList(squad.getAsJsonArray("gkLast"));
            List<String> defFirst = jsonArrayToList(squad.getAsJsonArray("defFirst"));
            List<String> defLast  = jsonArrayToList(squad.getAsJsonArray("defLast"));
            List<String> midFirst = jsonArrayToList(squad.getAsJsonArray("midFirst"));
            List<String> midLast  = jsonArrayToList(squad.getAsJsonArray("midLast"));
            List<String> fwdFirst = jsonArrayToList(squad.getAsJsonArray("fwdFirst"));
            List<String> fwdLast  = jsonArrayToList(squad.getAsJsonArray("fwdLast"));

            // Squad: 2 GK, 7 DEF, 6 MID, 5 FWD = 20 players — names taken in order
            addPlayersOrdered(team, FootballPosition.GOALKEEPER, gkFirst,  gkLast,  rng);
            addPlayersOrdered(team, FootballPosition.DEFENDER,   defFirst, defLast, rng);
            addPlayersOrdered(team, FootballPosition.MIDFIELDER, midFirst, midLast, rng);
            addPlayersOrdered(team, FootballPosition.FORWARD,    fwdFirst, fwdLast, rng);

            // Default tactic
            team.setCurrentTactic(FootballTactic.balanced());

            // One coach — real coach matched to team by index
            String cf = i < coachFirst.size() ? coachFirst.get(i) : coachFirst.get(rng.nextInt(coachFirst.size()));
            String cl = i < coachLast.size()  ? coachLast.get(i)  : coachLast.get(rng.nextInt(coachLast.size()));
            String sp = SPECIALTIES[rng.nextInt(SPECIALTIES.length)];
            team.addCoach(new FootballCoach(cf, cl, 38 + rng.nextInt(20), 5 + rng.nextInt(20), sp));

            teams.add(team);
        }
        return teams;
    }

    /** Creates one player per entry in the name lists (index-ordered, real names). */
    private void addPlayersOrdered(FootballTeam team, FootballPosition position,
                                   List<String> firstNames, List<String> lastNames,
                                   Random rng) {
        for (int j = 0; j < firstNames.size(); j++) {
            String first = firstNames.get(j);
            String last  = j < lastNames.size() ? lastNames.get(j) : lastNames.get(0);
            int age  = 18 + rng.nextInt(18);
            int base = 55 + rng.nextInt(20);
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

    private List<String> jsonArrayToList(JsonArray arr) {
        List<String> list = new ArrayList<>(arr.size());
        arr.forEach(e -> list.add(e.getAsString()));
        return list;
    }

    private int clamp(int v) { return Math.max(40, Math.min(95, v)); }

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
