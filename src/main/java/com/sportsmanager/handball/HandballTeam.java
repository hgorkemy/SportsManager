package com.sportsmanager.handball;

import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Team;


public class HandballTeam extends Team {

    public HandballTeam(String name, String logoPath) {
        super(name, logoPath);
    }

    @Override
    public boolean validateLineup() {
        // Allow 5-7: up to 2 red cards (disqualifications) may reduce the playable squad
        int size = getLineup().size();
        if (size < 5 || size > 7) return false;
        if (getLineup().stream().anyMatch(p -> p.isInjured() || p.isSuspended())) return false;
        return getLineup().stream()
                .anyMatch(p -> p.getPosition() != null
                               && "GK".equals(p.getPosition().getCode()));
    }

    @Override
    public int calculateAttackRating() {
        // BACK and WING primary attackers, PIV minor, GK ignored
        int total = 0, weight = 0;
        for (Player p : getLineup()) {
            if (p.getPosition() == null || p.isSuspended()) continue;
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
            if (p.getPosition() == null || p.isSuspended()) continue;
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
