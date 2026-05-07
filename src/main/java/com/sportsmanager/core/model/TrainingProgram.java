package com.sportsmanager.core.model;

import java.util.Map;

/**
 * Describes one week's training focus.
 * Sport-specific instances are defined in each Coach subclass via getTrainingPrograms().
 * The controller never needs to know which sport is active.
 *
 * Implemented by: Halil Görkem Yiğit
 */
public record TrainingProgram(
    String name,
    String emoji,
    String description,
    Map<String, Integer> bonuses   // attribute key → max gain per player per session
) {}
