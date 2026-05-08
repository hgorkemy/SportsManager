package com.sportsmanager.handball;

import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Team;

/**
 * Handball team — 16 max squad, lineup = 7 players with at least 1 GK.
 * Implemented by: Irmak Önder
 */
public class HandballTeam extends Team {

    public HandballTeam(String name, String logoPath) {
        super(name, logoPath);
    }

    @Override
    public boolean validateLineup() {
        if (getLineup().size() != 7) return false;
        if (getLineup().stream().anyMatch(Player::isInjured)) return false;
        return getLineup().stream()
                .anyMatch(p -> p.getPosition() != null
                               && "GK".equals(p.getPosition().getCode()));
    }

    @Override
    public int calculateAttackRating() {
        // BACK and WING primary attackers, PIV minor, GK ignored
        int total = 0, weight = 0;
        for (Player p : getLineup()) {
            if (p.getPosition() == null) continue;
            int w = switch (p.getPosition().getCode()) {
                case "BACK", "WING" -> 3;
                case "PIV"          -> 1;
                default             -> 0;
            };
            total += p.getOverallRating() * w;
            weight += w;
        }
        return weight == 0 ? 0 : total / weight;
    }

    @Override
    public int calculateDefenseRating() {
        // GK primary, all outfield contribute (collective defensive wall)
        int total = 0, weight = 0;
        for (Player p : getLineup()) {
            if (p.getPosition() == null) continue;
            int w = switch (p.getPosition().getCode()) {
                case "GK"           -> 3;
                case "PIV"          -> 2;
                case "BACK", "WING" -> 1;
                default             -> 0;
            };
            total += p.getOverallRating() * w;
            weight += w;
        }
        return weight == 0 ? 0 : total / weight;
    }

    @Override
    public int getMaxSquadSize() { return 16; }
}
