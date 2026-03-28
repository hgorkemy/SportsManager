package com.sportsmanager.core.model;

import java.util.Map;
import java.util.UUID;

/**
 * Abstract player class. All sport-specific players extend this.
 *
 * Encapsulation: injuredGamesRemaining is only modified through
 * injure() and recoverOneGame() methods.
 *
 * Implemented by: Halil Görkem Yiğit
 */
public abstract class Player extends Person {

    private final String id;
    private Position position;
    private int injuredGamesRemaining;

    protected Player(String firstName, String lastName, int age, Position position) {
        super(firstName, lastName, age);
        this.id = UUID.randomUUID().toString();
        this.position = position;
        this.injuredGamesRemaining = 0;
    }

    // ── Abstract methods (sport-specific subclass implements) ─────────────────

    /** Returns all sport-specific attributes. e.g. {"shooting": 80, "pace": 75} */
    public abstract Map<String, Integer> getAttributes();

    /** Computes overall rating 1-100 based on position weights. */
    public abstract int getOverallRating();

    // ── Injury system ─────────────────────────────────────────────────────────

    public boolean isInjured() {
        return injuredGamesRemaining > 0;
    }

    public void injure(int games) {
        if (games > 0) {
            this.injuredGamesRemaining = games;
        }
    }

    public void recoverOneGame() {
        if (injuredGamesRemaining > 0) {
            injuredGamesRemaining--;
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getId()                      { return id; }
    public Position getPosition()              { return position; }
    public int getInjuredGamesRemaining()      { return injuredGamesRemaining; }

    public void setPosition(Position position) { this.position = position; }

    @Override
    public String toString() {
        return getFullName() + " [" + (position != null ? position.getCode() : "?") + "]"
                + " OVR:" + getOverallRating()
                + (isInjured() ? " INJ:" + injuredGamesRemaining : "");
    }
}
