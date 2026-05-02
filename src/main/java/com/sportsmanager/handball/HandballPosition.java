package com.sportsmanager.handball;

import com.sportsmanager.core.model.Position;

public enum HandballPosition implements Position {

    GOALKEEPER("GK",   "Goalkeeper", "defending"),
    WING      ("WING", "Wing",       "speed"),
    BACK      ("BACK", "Back",       "throwing"),
    PIVOT     ("PIV",  "Pivot",      "defending");

    private final String code;
    private final String displayName;
    private final String primaryAttribute;

    HandballPosition(String code, String displayName, String primaryAttribute) {
        this.code = code;
        this.displayName = displayName;
        this.primaryAttribute = primaryAttribute;
    }

    @Override public String getCode()             { return code; }
    @Override public String getDisplayName()      { return displayName; }
    @Override public String getPrimaryAttribute() { return primaryAttribute; }
}
