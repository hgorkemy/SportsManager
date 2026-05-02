package com.sportsmanager.handball;

import com.sportsmanager.core.model.Coach;
import com.sportsmanager.core.model.Player;
import com.sportsmanager.core.model.Team;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Handball coach implementation.
 * Specialty areas: "Attack", "Defense", "Goalkeeping", "Fitness"
 * Implemented by: Irmak Önder
 */
public class HandballCoach extends Coach {

    private static final Random RNG = new Random();

    public HandballCoach(String firstName, String lastName, int age,
                         int experience, String specialty) {
        super(firstName, lastName, age, experience, specialty);
    }

    @Override
    public Map<String, Integer> getTrainingBonus() {
        return switch (getSpecialty()) {
            case "Attack"      -> Map.of("throwing", 3, "speed",     2);
            case "Defense"     -> Map.of("defending", 3, "jumping",  2);
            case "Goalkeeping" -> Map.of("defending", 4, "agility",  2);
            case "Fitness"     -> Map.of("stamina",   3, "agility",  2);
            default            -> Map.of();
        };
    }

    @Override
    public int calculateCoachingEffectiveness() {
        return Math.min(100, 50 + getExperience() * 2);
    }

    /**
     * Randomly increases 1-2 attributes of each HandballPlayer in the team by +1.
     */
    public void conductTraining(Team team) {
        List<String> attrKeys = List.of("throwing", "speed", "agility", "jumping", "defending", "stamina");
        for (Player player : team.getHealthyPlayers()) {
            if (!(player instanceof HandballPlayer hp)) continue;
            Map<String, Integer> attrs = hp.getAttributes();
            int count = 1 + RNG.nextInt(2); // 1 or 2 attributes
            for (int i = 0; i < count; i++) {
                String key = attrKeys.get(RNG.nextInt(attrKeys.size()));
                attrs.put(key, Math.min(90, attrs.get(key) + 1));
            }
        }
    }
}
