package com.sportsmanager.football;

import com.sportsmanager.core.model.Tactic;

import java.util.List;
import java.util.Map;

/**
 * Football formation/tactic.
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

    public static FootballTactic balanced()   { return new FootballTactic("4-4-2", 1.0, 1.0, "Balanced"); }
    public static FootballTactic offensive()  { return new FootballTactic("4-3-3", 1.2, 0.9, "Offensive"); }
    public static FootballTactic defensive()  { return new FootballTactic("5-3-2", 0.9, 1.2, "Defensive"); }
    public static FootballTactic control()    { return new FootballTactic("4-2-3-1", 1.1, 1.1, "Control"); }

    @Override public String getName()              { return name; }
    @Override public double getAttackMultiplier()  { return attackMultiplier; }
    @Override public double getDefenseMultiplier() { return defenseMultiplier; }
    @Override public String getDescription()       { return description; }
    @Override public Map<String, Integer> getAttributeBonuses() { return Map.of(); }

    @Override
    public List<Line> getFormationLines() {
        return switch (name) {
            case "4-4-2"   -> List.of(new Line("GK", 1, 0.05),
                                      new Line("DEF", 4, 0.22),
                                      new Line("MID", 4, 0.52),
                                      new Line("FWD", 2, 0.83));
            case "4-3-3"   -> List.of(new Line("GK", 1, 0.05),
                                      new Line("DEF", 4, 0.22),
                                      new Line("MID", 3, 0.52),
                                      new Line("FWD", 3, 0.83));
            case "4-2-3-1" -> List.of(new Line("GK", 1, 0.05),
                                      new Line("DEF", 4, 0.22),
                                      new Line("MID", 2, 0.44),
                                      new Line("MID", 3, 0.65),
                                      new Line("FWD", 1, 0.86));
            case "5-3-2"   -> List.of(new Line("GK", 1, 0.05),
                                      new Line("DEF", 5, 0.22),
                                      new Line("MID", 3, 0.55),
                                      new Line("FWD", 2, 0.83));
            default        -> List.of();
        };
    }
}
