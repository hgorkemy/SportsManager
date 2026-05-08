package com.sportsmanager.core.model;

import java.util.Map;


public record TrainingProgram(
    String name,
    String emoji,
    String description,
    Map<String, Integer> bonuses   // attribute key → max gain per player per session
) {}
