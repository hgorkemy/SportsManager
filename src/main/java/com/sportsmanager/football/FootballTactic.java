package com.sportsmanager.football;

import com.sportsmanager.core.model.Tactic;

import java.util.Map;

/**
 * Football formation/tactic.
 *
 * TODO (Berke): Implement constructor and factory methods for 4-4-2, 4-3-3, etc.
 */
public class FootballTactic implements Tactic {

    private final String name;
    private final double attackMultiplier;
    private final double defenseMultiplier;
    private final String description;

    public FootballTactic(String name, double attackMultiplier, double defenseMultiplier, String description) {
        this.name = name;
        this.attackMultiplier = attackMultiplier;
        this.defenseMultiplier = defenseMultiplier;
        this.description = description;
    }

    // TODO (Berke): Add static factory methods
    public static FootballTactic balanced()   { return new FootballTactic("4-4-2", 1.0, 1.0, "Balanced"); }
    public static FootballTactic offensive()  { return new FootballTactic("4-3-3", 1.2, 0.9, "Offensive"); }
    public static FootballTactic defensive()  { return new FootballTactic("5-3-2", 0.9, 1.2, "Defensive"); }
    public static FootballTactic control()    { return new FootballTactic("4-2-3-1", 1.1, 1.1, "Control"); }

    @Override public String getName()              { return name; }
    @Override public double getAttackMultiplier()  { return attackMultiplier; }
    @Override public double getDefenseMultiplier() { return defenseMultiplier; }
    @Override public String getDescription()       { return description; }
    @Override public Map<String, Integer> getAttributeBonuses() { return Map.of(); }
}
