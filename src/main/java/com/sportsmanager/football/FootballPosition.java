package com.sportsmanager.football;

import com.sportsmanager.core.model.Position;

/**
 * Football playing positions.
 *
 * TODO (Berke): Verify primaryAttribute values match your rating formula.
 */
public enum FootballPosition implements Position {

    GOALKEEPER("GK", "Goalkeeper", "defending"),
    DEFENDER("DEF", "Defender", "defending"),
    MIDFIELDER("MID", "Midfielder", "passing"),
    FORWARD("FWD", "Forward", "shooting");

    private final String code;
    private final String displayName;
    private final String primaryAttribute;

    FootballPosition(String code, String displayName, String primaryAttribute) {
        this.code = code;
        this.displayName = displayName;
        this.primaryAttribute = primaryAttribute;
    }

    @Override public String getCode()             { return code; }
    @Override public String getDisplayName()      { return displayName; }
    @Override public String getPrimaryAttribute() { return primaryAttribute; }
}
