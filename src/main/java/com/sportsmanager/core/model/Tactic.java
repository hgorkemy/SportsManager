package com.sportsmanager.core.model;

import java.util.Map;

/**
 * Represents a formation or strategy.
 *
 * TODO (Egemen): This interface is defined. No changes needed here.
 */
public interface Tactic {
    String getName();
    double getAttackMultiplier();
    double getDefenseMultiplier();
    String getDescription();
    Map<String, Integer> getAttributeBonuses();
}
