package com.sportsmanager.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sportsmanager.core.engine.MatchEngine;
import com.sportsmanager.core.factory.SportFactory;
import com.sportsmanager.core.factory.SportRegistry;
import com.sportsmanager.core.model.Coach;
import com.sportsmanager.core.model.GameSession;
import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.MatchResult;
import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Sport;
import com.sportsmanager.core.model.Tactic;
import com.sportsmanager.core.model.Team;
import com.sportsmanager.football.FootballCoach;
import com.sportsmanager.football.FootballPlayer;
import com.sportsmanager.football.FootballPosition;
import com.sportsmanager.football.FootballTeam;
import com.sportsmanager.handball.HandballCoach;
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

        var matchDay = league.getCurrentMatchDay();
        data.weekIndex = (matchDay != null) ? (matchDay.getWeekNumber() - 1) : -1;

        // Save each team's data
        for (Team team : league.getTeams()) {
            TeamData td = new TeamData();
            td.teamName = team.getName();

            // Current tactic name
            if (team.getCurrentTactic() != null) {
                td.currentTacticName = team.getCurrentTactic().getName();
            }

            // Players
            List<Player> squad  = team.getSquad();
            List<Player> lineup = team.getLineup();

            for (Player p : squad) {
                PlayerData pd = new PlayerData();
                pd.firstName             = p.getFirstName();
                pd.lastName              = p.getLastName();
                pd.age                   = p.getAge();
                pd.positionName          = (p.getPosition() instanceof Enum<?> e) ? e.name() : "UNKNOWN";
                pd.attributes            = p.getAttributes();
                pd.injuredGamesRemaining    = p.getInjuredGamesRemaining();
                pd.suspendedGamesRemaining  = p.getSuspendedGamesRemaining();
                pd.goals                 = p.getGoals();
                pd.yellowCards           = p.getYellowCards();
                pd.appearances           = p.getAppearances();
                pd.initialOverall        = p.getInitialOverall();
                td.players.add(pd);
            }

            // Lineup as indices into squad
            for (Player lp : lineup) {
                int idx = squad.indexOf(lp);
                if (idx >= 0) td.lineupIndices.add(idx);
            }

            // Coaches
            for (Coach c : team.getCoaches()) {
                CoachData cd = new CoachData();
                cd.firstName  = c.getFirstName();
                cd.lastName   = c.getLastName();
                cd.age        = c.getAge();
                cd.experience = c.getExperience();
                cd.specialty  = c.getSpecialty();
                td.coaches.add(cd);
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

        // Create sport first — needed for tactic lookup
        Sport sport = factory.createSport();

        // Reconstruct teams with players and coaches
        List<Team> teams = new ArrayList<>();
        for (TeamData td : data.teams) {
            Team team = createTeam(data.sportName, td.teamName);

            // Players
            for (PlayerData pd : td.players) {
                Player p = createPlayer(data.sportName, pd);
                if (p != null) team.addPlayer(p);
            }

            // Coaches
            if (td.coaches != null) {
                for (CoachData cd : td.coaches) {
                    Coach c = createCoach(data.sportName, cd);
                    if (c != null) team.addCoach(c);
                }
            }

            // Current tactic
            if (td.currentTacticName != null) {
                sport.getAvailableTactics().stream()
                     .filter(t -> t.getName().equals(td.currentTacticName))
                     .findFirst()
                     .ifPresent(team::setCurrentTactic);
            }
            // Fallback: first available tactic if none matched
            if (team.getCurrentTactic() == null && !sport.getAvailableTactics().isEmpty()) {
                team.setCurrentTactic(sport.getAvailableTactics().get(0));
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
            List<Player> squad  = team.getSquad();
            List<Player> lineup = new ArrayList<>();
            for (int idx : td.lineupIndices) {
                if (idx >= 0 && idx < squad.size()) lineup.add(squad.get(idx));
            }
            try {
                if (!lineup.isEmpty()) team.setLineup(lineup);
            } catch (Exception ignored) {
                // Invalid saved lineup (injured players etc.) — user will re-select
            }
        }

        // Build league and engine
        League      league      = factory.createLeague(teams);
        MatchEngine matchEngine = factory.createMatchEngine();

        // Import standings
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

    /** Deletes the save file with the given name. Throws if it doesn't exist. */
    public static void delete(String saveName) throws IOException {
        Path file = savesDir().resolve(saveName + ".json");
        if (!Files.exists(file)) throw new IOException("Save file not found: " + file);
        Files.delete(file);
    }

    /** Returns all save file names (without .json) in the saves directory. */
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

    /**
     * Returns save file names that belong to the given sport.
     * Reads only the "sportName" field from each JSON — fast, no full deserialization.
     */
    public static List<String> listSavesForSport(String sportName) {
        try {
            Files.createDirectories(savesDir());
            return Files.list(savesDir())
                        .filter(p -> p.toString().endsWith(".json"))
                        .filter(p -> sportNameMatches(p, sportName))
                        .map(p -> p.getFileName().toString().replace(".json", ""))
                        .sorted()
                        .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    /** Reads only the top-level "sportName" field from a save file. */
    private static boolean sportNameMatches(Path file, String expected) {
        try {
            String json = Files.readString(file);
            // Parse just enough to get sportName — reuse the same Gson
            SaveData partial = GSON.fromJson(json, SaveData.class);
            return expected.equals(partial.sportName);
        } catch (Exception e) {
            return false; // corrupt or unreadable file — exclude it
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
            // Restore injury and suspension
            if (pd.injuredGamesRemaining   > 0) p.injure(pd.injuredGamesRemaining);
            if (pd.suspendedGamesRemaining > 0) p.restoreSuspension(pd.suspendedGamesRemaining);
            // Restore match stats
            p.restoreStats(pd.goals, pd.yellowCards, pd.appearances);
            // Restore initial overall (season-start snapshot)
            if (pd.initialOverall > 0) p.restoreInitialOverall(pd.initialOverall);
            return p;
        } catch (Exception e) {
            return null; // unknown position or corrupt data — skip player
        }
    }

    private static Coach createCoach(String sportName, CoachData cd) {
        try {
            return switch (sportName) {
                case "Handball" -> new HandballCoach(cd.firstName, cd.lastName,
                                                     cd.age, cd.experience, cd.specialty);
                default         -> new FootballCoach(cd.firstName, cd.lastName,
                                                     cd.age, cd.experience, cd.specialty);
            };
        } catch (Exception e) {
            return null;
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
        String          currentTacticName;
        List<PlayerData>  players       = new ArrayList<>();
        List<Integer>     lineupIndices = new ArrayList<>();
        List<CoachData>   coaches       = new ArrayList<>();
    }

    private static class PlayerData {
        String               firstName;
        String               lastName;
        int                  age;
        String               positionName;
        Map<String, Integer> attributes;
        int                  injuredGamesRemaining;
        int                  suspendedGamesRemaining;
        int                  goals;
        int                  yellowCards;
        int                  appearances;
        int                  initialOverall;
    }

    private static class CoachData {
        String firstName;
        String lastName;
        int    age;
        int    experience;
        String specialty;
    }

    private static class StandingData {
        String teamName;
        int played, wins, draws, losses, goalsFor, goalsAgainst, points;
    }
}
