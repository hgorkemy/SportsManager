package com.sportsmanager.core.model;

import java.util.Map;
import java.util.UUID;

/**
 * Abstract coach class. Sport-specific coaches extend this.
 *
 * Template Method: conductTraining() defines the training flow.
 * Subclasses implement getTrainingBonus() and calculateCoachingEffectiveness().
 *
 * Implemented by: Halil Görkem Yiğit
 */
public abstract class Coach extends Person {

    private final String id;
    private int experience; // years
    private String specialty;

    protected Coach(String firstName, String lastName, int age, int experience, String specialty) {
        super(firstName, lastName, age);
        this.id = UUID.randomUUID().toString();
        this.experience = experience;
        this.specialty = specialty;
    }

    // ── Abstract methods ──────────────────────────────────────────────────────

    /**
     * Returns which attributes this coach improves and by how much.
     * e.g. {"shooting": 3, "passing": 2}
     */
    public abstract Map<String, Integer> getTrainingBonus();

    /**
     * Returns coaching quality score (1-100).
     * Used to scale training effectiveness.
     */
    public abstract int calculateCoachingEffectiveness();

    // ── Template Method ───────────────────────────────────────────────────────

    /**
     * Applies one week of training to the given player.
     * The flow is fixed; sport-specific bonuses come from getTrainingBonus().
     */
    public void conductTraining(Player player) {
        Map<String, Integer> bonus = getTrainingBonus();
        Map<String, Integer> attrs = player.getAttributes();
        double quality = calculateCoachingEffectiveness() / 100.0;

        bonus.forEach((attr, maxGain) -> {
            if (attrs.containsKey(attr)) {
                int gain = (int) Math.round(Math.random() * maxGain * quality);
                attrs.put(attr, Math.min(100, attrs.get(attr) + gain));
            }
        });
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getId()         { return id; }
    public int getExperience()    { return experience; }
    public String getSpecialty()  { return specialty; }

    public void setExperience(int experience) { this.experience = experience; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    @Override
    public String toString() {
        return getFullName() + " | " + specialty + " Coach | " + experience + " yrs";
    }
}
