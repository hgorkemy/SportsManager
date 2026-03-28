package com.sportsmanager.football;

import com.sportsmanager.core.model.Coach;

import java.util.Map;

/**
 * Football coach implementation.
 *
 * TODO (Berke): Implement calculateCoachingEffectiveness() and getTrainingBonus()
 * based on specialty (Attack, Defense, Fitness, Goalkeeping).
 */
public class FootballCoach extends Coach {

    public FootballCoach(String firstName, String lastName, int age,
                         int experience, String specialty) {
        super(firstName, lastName, age, experience, specialty);
    }

    @Override
    public Map<String, Integer> getTrainingBonus() {
        // TODO (Berke): Return bonus based on specialty
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
        // TODO (Berke): Implement based on experience
        return Math.min(100, 50 + getExperience() * 2);
    }
}
