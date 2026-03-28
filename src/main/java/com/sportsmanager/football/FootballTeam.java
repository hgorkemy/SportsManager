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
        // TODO (Görkem): Check 11 players and at least 1 GK
        if (getLineup().size() != 11) return false;
        return getLineup().stream()
                .anyMatch(p -> p.getPosition() instanceof FootballPosition fp
                               && fp == FootballPosition.GOALKEEPER);
    }

    @Override
    public int calculateAttackRating() {
        // TODO : Average shooting+passing of MID and FWD in lineup
        return getLineup().stream()
                .filter(p -> p instanceof FootballPlayer)
                .mapToInt(p -> ((FootballPlayer) p).getShooting())
                .sum() / Math.max(1, getLineup().size());
    }

    @Override
    public int calculateDefenseRating() {
        // TODO : Average defending of DEF and GK in lineup
        return getLineup().stream()
                .filter(p -> p instanceof FootballPlayer)
                .mapToInt(p -> ((FootballPlayer) p).getDefending())
                .sum() / Math.max(1, getLineup().size());
    }

    @Override
    public int getMaxSquadSize() { return 25; }
}
