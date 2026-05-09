package com.sportsmanager.handball;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.sportsmanager.core.engine.MatchEngine;
import com.sportsmanager.core.factory.SportFactory;
import com.sportsmanager.core.model.League;
import com.sportsmanager.core.model.Sport;
import com.sportsmanager.core.model.Team;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class HandballFactory implements SportFactory {

    private static final String DATA_PATH = "/data/handball_teams.json";

    // ── Gson DTOs ────────────────────────────────────────────────────────────

    private static class RosterData {
        List<TeamEntry> teams;
    }

    private static class TeamEntry {
        String name;
        String logoPath;
        int tier = 2;
        List<CoachEntry>  coaches;
        List<PlayerEntry> players;
    }

    private static class PlayerEntry {
        String firstName;
        String lastName;
        int    age;
        String position;   // "GOALKEEPER" | "WING" | "BACK" | "PIVOT"
    }

    private static class CoachEntry {
        String firstName;
        String lastName;
        int    age;
        int    experience;
        String specialty;
    }

    // ── SportFactory ─────────────────────────────────────────────────────────

    @Override
    public Sport createSport() {
        return new HandballSport();
    }

    @Override
    public List<Team> generateTeams(int count) {
        RosterData data = loadRosterData();
        List<Team> teams = new ArrayList<>();
        Random rng = new Random();

        int limit = Math.min(count, data.teams.size());
        for (int i = 0; i < limit; i++) {
            TeamEntry entry = data.teams.get(i);
            HandballTeam team = new HandballTeam(entry.name, entry.logoPath);

            // Players
            for (PlayerEntry pe : entry.players) {
                HandballPosition pos = parsePosition(pe.position);
                int base = baseStatForTier(entry.tier, rng);
                team.addPlayer(new HandballPlayer(
                        pe.firstName, pe.lastName, pe.age, pos,
                        clamp(base + rng.nextInt(20) - 10),  // throwing
                        clamp(base + rng.nextInt(20) - 10),  // speed
                        clamp(base + rng.nextInt(20) - 10),  // agility
                        clamp(base + rng.nextInt(20) - 10),  // jumping
                        clamp(base + rng.nextInt(20) - 10),  // defending
                        clamp(base + rng.nextInt(20) - 10)   // stamina
                ));
            }

            // Default tactic
            team.setCurrentTactic(HandballTactic.balanced());

            // Coach(es) — loaded from JSON, one per team
            if (entry.coaches != null) {
                for (CoachEntry ce : entry.coaches) {
                    team.addCoach(new HandballCoach(
                            ce.firstName, ce.lastName, ce.age, ce.experience, ce.specialty));
                }
            }

            teams.add(team);
        }
        return teams;
    }

    @Override
    public League createLeague(List<Team> teams) {
        HandballLeague league = new HandballLeague("Handball Champions League", teams);
        league.generateSchedule();
        return league;
    }

    @Override
    public MatchEngine createMatchEngine() {
        return new HandballMatchEngine();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private RosterData loadRosterData() {
        InputStream is = getClass().getResourceAsStream(DATA_PATH);
        if (is == null) throw new RuntimeException("Cannot find " + DATA_PATH);
        try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, RosterData.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse " + DATA_PATH, e);
        }
    }

    /**
     * Tier-based stat base:
     *   tier 1 → elite  (65–84)
     *   tier 2 → strong (57–76)
     *   tier 3 → mid    (50–69)
     */
    private int baseStatForTier(int tier, Random rng) {
        return switch (tier) {
            case 1  -> 65 + rng.nextInt(20);
            case 3  -> 50 + rng.nextInt(20);
            default -> 57 + rng.nextInt(20);  // tier 2 (default)
        };
    }

    private HandballPosition parsePosition(String code) {
        if (code == null) return HandballPosition.BACK;
        return switch (code.toUpperCase()) {
            case "GOALKEEPER" -> HandballPosition.GOALKEEPER;
            case "WING"       -> HandballPosition.WING;
            case "PIVOT"      -> HandballPosition.PIVOT;
            default           -> HandballPosition.BACK;
        };
    }

    private int clamp(int v) { return Math.max(40, Math.min(90, v)); }
}
