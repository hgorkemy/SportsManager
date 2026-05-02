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
                .anyMatch(p -> p.getPosition() instanceof HandballPosition pos
                               && pos == HandballPosition.GOALKEEPER);
    }

    @Override
    public int calculateAttackRating() {
        return getLineup().stream()
                .filter(p -> p instanceof HandballPlayer)
                .mapToInt(p -> ((HandballPlayer) p).getThrowing())
                .sum() / Math.max(1, getLineup().size());
    }

    @Override
    public int calculateDefenseRating() {
        return getLineup().stream()
                .filter(p -> p instanceof HandballPlayer)
                .mapToInt(p -> ((HandballPlayer) p).getDefending())
                .sum() / Math.max(1, getLineup().size());
    }

    @Override
    public int getMaxSquadSize() { return 16; }
}
