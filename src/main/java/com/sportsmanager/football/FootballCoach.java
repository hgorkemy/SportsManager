package com.sportsmanager.football;

import com.sportsmanager.core.model.Coach;
import com.sportsmanager.core.model.TrainingProgram;

import java.util.List;
import java.util.Map;

/**
 * Football coach implementation.
 */
public class FootballCoach extends Coach {

    public FootballCoach(String firstName, String lastName, int age,
                         int experience, String specialty) {
        super(firstName, lastName, age, experience, specialty);
    }

    @Override
    public Map<String, Integer> getTrainingBonus() {
        return switch (getSpecialty()) {
            case "Attack"      -> Map.of("shooting", 3, "passing", 2);
            case "Defense"     -> Map.of("defending", 3, "physicality", 2);
            case "Fitness"     -> Map.of("speed", 2, "physicality", 3);
            case "Goalkeeping" -> Map.of("defending", 4, "speed", 1);
            default            -> Map.of();
        };
    }

    @Override
    public int calculateCoachingEffectiveness() {
        return Math.min(100, 50 + getExperience() * 2);
    }

    @Override
    public List<TrainingProgram> getTrainingPrograms() {
        return List.of(
            new TrainingProgram("Attack",      "⚽",
                "Shooting, passing & ball control",
                Map.of("shooting", 3, "passing", 2, "ballControl", 1)),
            new TrainingProgram("Defense",     "🛡",
                "Defending & physicality",
                Map.of("defending", 3, "physicality", 2)),
            new TrainingProgram("Fitness",     "🏃",
                "Speed & physical conditioning",
                Map.of("speed", 3, "physicality", 2)),
            new TrainingProgram("Goalkeeping", "🧤",
                "Reflexes, positioning & distribution",
                Map.of("defending", 4, "speed", 1))
        );
    }
}
