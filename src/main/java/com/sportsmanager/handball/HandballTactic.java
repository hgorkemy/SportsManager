package com.sportsmanager.handball;

import com.sportsmanager.core.model.Tactic;

import java.util.List;
import java.util.Map;

/**
 * Handball formation/tactic.
 */
public class HandballTactic implements Tactic {

    private final String name;
    private final double attackMultiplier;
    private final double defenseMultiplier;
    private final String description;

    public HandballTactic(String name, double attackMultiplier, double defenseMultiplier, String description) {
        this.name = name;
        this.attackMultiplier = attackMultiplier;
        this.defenseMultiplier = defenseMultiplier;
        this.description = description;
    }

    public static HandballTactic attacking()  { return new HandballTactic("4-2", 1.20, 0.85, "Attacking"); }
    public static HandballTactic balanced()   { return new HandballTactic("3-2-1", 1.00, 1.00, "Balanced"); }
    public static HandballTactic defensive()  { return new HandballTactic("6-0", 0.85, 1.25, "Defensive"); }

    @Override public String getName()              { return name; }
    @Override public double getAttackMultiplier()  { return attackMultiplier; }
    @Override public double getDefenseMultiplier() { return defenseMultiplier; }
    @Override public String getDescription()       { return description; }
    @Override public Map<String, Integer> getAttributeBonuses() { return Map.of(); }

    @Override
    public List<Line> getFormationLines() {
        return switch (name) {
            // GK → backs (midfield) → wings (forward) → pivot (opponent 6m)
            case "3-2-1" -> List.of(new Line("GK",   1, 0.05),
                                    new Line("BACK",  3, 0.42),
                                    new Line("WING",  2, 0.62),
                                    new Line("PIV",   1, 0.82));
            case "4-2"   -> List.of(new Line("GK",   1, 0.05),
                                    new Line("BACK",  4, 0.45),
                                    new Line("WING",  2, 0.62));
            // 6-0: all 6 backs defend at own 6-9m arc, close to GK
            case "6-0"   -> List.of(new Line("GK",   1, 0.05),
                                    new Line("BACK",  6, 0.22));
            default      -> List.of();
        };
    }
}
