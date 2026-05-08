package com.sportsmanager.core.model;

import java.util.List;
import java.util.Map;

public interface Tactic {

    /**
     * positionCode: player position that belongs here (e.g. "DEF", "MID", "WING").
     * count: how many players are in this layer.
     * relY: depth (0 = own goal, 1 = opponent goal).
     */
    record Line(String positionCode, int count, double relY) {}

    String getName();
    double getAttackMultiplier();
    double getDefenseMultiplier();
    String getDescription();
    Map<String, Integer> getAttributeBonuses();

    /**
     * Returns the ordered layers of this formation, from GK to top.
     */
    List<Line> getFormationLines();
}
