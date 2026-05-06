package com.sportsmanager.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sportsmanager.core.engine.MatchEngine;
import com.sportsmanager.core.factory.SportFactory;
import com.sportsmanager.core.factory.SportRegistry;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.MatchResult;
import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Sport;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.FootballPosition;
import com.sportsmanager.football.FootballTeam;
import com.sportsmanager.handball.HandballPlayer;
import com.sportsmanager.handball.HandballPosition;
import com.sportsmanager.handball.HandballTeam;
import com.sportsmanager.league.StandingRow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles saving and loading game state to/from JSON files.
 * Save files are stored in ~/SportsManagerSaves/{name}.json
 *
 * Implemented by: Halil Görkem Yiğit
 */
public class GameSaveManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path savesDir() {
        return Paths.get(System.getProperty("user.home"), "SportsManagerSaves");
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public static void save(String saveName) throws IOException {
        Files.createDirectories(savesDir());

        GameSession session = GameSession.getInstance();
        League      league  = session.getLeague();

        SaveData data = new SaveData();
        data.sportName           = session.getSelectedSportName();
        data.season              = session.getCurrentSeason();
        data.userTeamName        = session.getUserTeam().getName();
        data.matchPlayedThisWeek = session.isMatchPlayedThisWeek();

        // Calculate weekIndex from current state
        // We derive it from the standings size (matches played by user team)
        // using getStandings to infer weeks. Simplest: count weeks advanced.
        // We store it via getCurrentMatchDay's week number.
        var matchDay = league.getCurrentMatchDay();
        data.weekIndex = (matchDay != null) ? (matchDay.getWeekNumber() - 1) : -1;

        // Save each team's data
        for (Team team : league.getTeams()) {
            TeamData td = new TeamData();
            td.teamName = team.getName();

            List<Player> squad   = team.getSquad();
            List<Player> lineup  = team.getLineup();

            for (Player p : squad) {
                PlayerData pd = new PlayerData();
                pd.firstName              = p.getFirstName();
                pd.lastName               = p.getLastName();
                pd.age                    = p.getAge();
                pd.positionName           = (p.getPosition() instanceof Enum<?> e) ? e.name() : "UNKNOWN";
                pd.attributes             = p.getAttributes();
                pd.injuredGamesRemaining  = p.getInjuredGamesRemaining();
                td.players.add(pd);
            }

            // Save lineup as indices into squad
            for (Player lp : lineup) {
                int idx = squad.indexOf(lp);
                if (idx >= 0) td.lineupIndices.add(idx);
            }

            data.teams.add(td);
        }

        // Save standings
        for (StandingRow row : league.getStandings()) {
            StandingData sd = new StandingData();
            sd.teamName     = row.getTeam().getName();
            sd.played       = row.getMatchesPlayed();
            sd.wins         = row.getWins();
            sd.draws        = row.getDraws();
            sd.losses       = row.getLosses();
            sd.goalsFor     = row.getGoalsFor();
            sd.goalsAgainst = row.getGoalsAgainst();
            sd.points       = row.getPoints();
            data.standings.add(sd);
        }

        Path file = savesDir().resolve(saveName + ".json");
        Files.writeString(file, GSON.toJson(data));
    }

    public static void load(String saveName) throws IOException {
        Path file = savesDir().resolve(saveName + ".json");
        if (!Files.exists(file)) throw new IOException("Save file not found: " + file);

        SaveData data = GSON.fromJson(Files.readString(file), SaveData.class);

        SportFactory factory = SportRegistry.getFactory(data.sportName);
        if (factory == null) throw new IOException("Unknown sport: " + data.sportName);

        // Reconstruct teams with players
        List<Team> teams = new ArrayList<>();
        for (TeamData td : data.teams) {
            Team team = createTeam(data.sportName, td.teamName);
            for (PlayerData pd : td.players) {
                Player p = createPlayer(data.sportName, pd);
                if (p != null) team.addPlayer(p);
            }
            teams.add(team);
        }

        // Mark user team
        teams.stream()
             .filter(t -> t.getName().equals(data.userTeamName))
             .findFirst()
             .ifPresent(t -> t.setUserTeam(true));

        // Restore lineups
        for (int i = 0; i < data.teams.size() && i < teams.size(); i++) {
            TeamData td   = data.teams.get(i);
            Team     team = teams.get(i);
            List<Player> squad = team.getSquad();
            List<Player> lineup = new ArrayList<>();
            for (int idx : td.lineupIndices) {
                if (idx >= 0 && idx < squad.size()) lineup.add(squad.get(idx));
            }
            try {
                if (!lineup.isEmpty()) team.setLineup(lineup);
            } catch (Exception ignored) {
                // invalid lineup (e.g., injured players) — leave empty, user will re-select
            }
        }

        // Create fresh league and generate schedule
        Sport       sport       = factory.createSport();
        League      league      = factory.createLeague(teams);   // generates fresh schedule
        MatchEngine matchEngine = factory.createMatchEngine();

        // Import standings directly (schedule is fresh but standings are restored)
        for (StandingData sd : data.standings) {
            league.importStanding(sd.teamName, sd.played, sd.wins, sd.draws,
                                  sd.losses, sd.goalsFor, sd.goalsAgainst, sd.points);
        }

        // Restore week position
        if (data.weekIndex >= 0) league.restoreWeekIndex(data.weekIndex);

        // Find user team object
        Team userTeam = teams.stream()
                             .filter(t -> t.getName().equals(data.userTeamName))
                             .findFirst()
                             .orElse(teams.isEmpty() ? null : teams.get(0));

        // Restore session
        GameSession session = GameSession.getInstance();
        session.startNewGame(sport, league, userTeam, matchEngine, saveName);
        session.setSelectedSportName(data.sportName);
        session.setCurrentSeason(data.season);
        session.setMatchPlayedThisWeek(data.matchPlayedThisWeek);
    }

    /** Returns save file names (without .json) in the saves directory. */
    public static List<String> listSaves() {
        try {
            Files.createDirectories(savesDir());
            return Files.list(savesDir())
                        .filter(p -> p.toString().endsWith(".json"))
                        .map(p -> p.getFileName().toString().replace(".json", ""))
                        .sorted()
                        .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    // ── Factory helpers ────────────────────────────────────────────────────────

    private static Team createTeam(String sportName, String teamName) {
        return switch (sportName) {
            case "Handball" -> new HandballTeam(teamName, null);
            default         -> new FootballTeam(teamName, null);
        };
    }

    private static Player createPlayer(String sportName, PlayerData pd) {
        try {
            Map<String, Integer> a = pd.attributes;
            Player p;
            if ("Handball".equals(sportName)) {
                HandballPosition pos = HandballPosition.valueOf(pd.positionName);
                p = new HandballPlayer(pd.firstName, pd.lastName, pd.age, pos,
                        a.getOrDefault("throwing",  65),
                        a.getOrDefault("speed",     65),
                        a.getOrDefault("agility",   65),
                        a.getOrDefault("jumping",   65),
                        a.getOrDefault("defending", 65),
                        a.getOrDefault("stamina",   65));
            } else {
                FootballPosition pos = FootballPosition.valueOf(pd.positionName);
                p = new FootballPlayer(pd.firstName, pd.lastName, pd.age, pos,
                        a.getOrDefault("speed",        65),
                        a.getOrDefault("shooting",     65),
                        a.getOrDefault("passing",      65),
                        a.getOrDefault("ballControl",  65),
                        a.getOrDefault("defending",    65),
                        a.getOrDefault("physicality",  65));
            }
            // Restore injury
            if (pd.injuredGamesRemaining > 0) p.injure(pd.injuredGamesRemaining);
            return p;
        } catch (Exception e) {
            return null; // unknown position — skip player
        }
    }

    // ── DTOs (serialized to JSON by Gson) ─────────────────────────────────────

    private static class SaveData {
        String         sportName;
        int            season;
        int            weekIndex;
        String         userTeamName;
        boolean        matchPlayedThisWeek;
        List<TeamData>     teams     = new ArrayList<>();
        List<StandingData> standings = new ArrayList<>();
    }

    private static class TeamData {
        String          teamName;
        List<PlayerData>  players       = new ArrayList<>();
        List<Integer>     lineupIndices = new ArrayList<>();
    }

    private static class PlayerData {
        String               firstName;
        String               lastName;
        int                  age;
        String               positionName;
        Map<String, Integer> attributes;
        int                  injuredGamesRemaining;
    }

    private static class StandingData {
        String teamName;
        int played, wins, draws, losses, goalsFor, goalsAgainst, points;
    }
}
