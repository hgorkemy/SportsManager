package com.sportsmanager.football;

import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Team;

/**
 * Football team — validates 11 players with at least 1 GK.
 *
 * TODO (Irmak): Implement validateLineup(), calculateAttackRating(), calculateDefenseRating().
 */
public class FootballTeam extends Team {

    public FootballTeam(String name, String logoPath) {
        super(name, logoPath);
    }

    @Override
    public boolean validateLineup() {
        if (getLineup().size() != 11) return false;
        if (getLineup().stream().anyMatch(Player::isInjured)) return false;
        return getLineup().stream()
                .anyMatch(p -> p.getPosition() instanceof FootballPosition fp
                               && fp == FootballPosition.GOALKEEPER);
    }

    @Override
    public int calculateAttackRating() {
        // FWD primary, MID secondary, DEF minor (modern fullbacks/ball-playing CBs), GK ignored
        int total = 0, weight = 0;
        for (Player p : getLineup()) {
            if (p.getPosition() == null) continue;
            int w = switch (p.getPosition().getCode()) {
                case "FWD" -> 3;
                case "MID" -> 2;
                case "DEF" -> 1;
                default    -> 0;
            };
            total += p.getOverallRating() * w;
            weight += w;
        }
        return weight == 0 ? 0 : total / weight;
    }

    @Override
    public int calculateDefenseRating() {
        // GK and DEF primary, MID secondary, FWD minor (high press)
        int total = 0, weight = 0;
        for (Player p : getLineup()) {
            if (p.getPosition() == null) continue;
            int w = switch (p.getPosition().getCode()) {
                case "GK", "DEF" -> 3;
                case "MID"       -> 2;
                case "FWD"       -> 1;
                default          -> 0;
            };
            total += p.getOverallRating() * w;
            weight += w;
        }
        return weight == 0 ? 0 : total / weight;
    }

    @Override
    public int getMaxSquadSize() { return 25; }
}
