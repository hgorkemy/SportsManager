package com.sportsmanager.core.model;

import java.util.List;
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
     * Returns which attributes this coach's specialty improves and by how much.
     * Used for the automatic weekly training in advanceWeek().
     */
    public abstract Map<String, Integer> getTrainingBonus();

    /**
     * Returns coaching quality score (1-100).
     * Used to scale training effectiveness.
     */
    public abstract int calculateCoachingEffectiveness();

    /**
     * Returns ALL training programs available for this coach's sport.
     * The controller calls this — it never needs to know the sport type.
     * Each Coach subclass defines the programs for its own sport.
     */
    public abstract List<TrainingProgram> getTrainingPrograms();

    // ── Template Methods ──────────────────────────────────────────────────────

    /**
     * Applies this coach's specialty training to one player (used by advanceWeek).
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

    /**
     * Applies a user-chosen training program to one player.
     * If the program matches the coach's specialty, gains are ×1.5.
     */
    public void conductTraining(Player player, TrainingProgram program) {
        Map<String, Integer> attrs = player.getAttributes();
        double quality     = calculateCoachingEffectiveness() / 100.0;
        double multiplier  = program.name().equals(getSpecialty()) ? 1.5 : 1.0;

        program.bonuses().forEach((attr, maxGain) -> {
            if (attrs.containsKey(attr)) {
                int gain = (int) Math.round(Math.random() * maxGain * quality * multiplier);
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
