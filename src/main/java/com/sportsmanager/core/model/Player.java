package com.sportsmanager.core.model;

import java.util.Map;
import java.util.UUID;


public abstract class Player extends Person {

    private final String id;
    private Position position;
    private int injuredGamesRemaining;
    private int suspendedGamesRemaining;

    private int goals;
    private int yellowCards;
    private int redCards;
    private int appearances;

    private int initialOverall = -1;

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
        if (injuredGamesRemaining > 0) injuredGamesRemaining--;
    }

    // ── Suspension system ─────────────────────────────────────────────────────

    public boolean isSuspended()             { return suspendedGamesRemaining > 0; }
    public void suspend(int games)           { if (games > 0) suspendedGamesRemaining = games; }
    public void recoverOneSuspension()       { if (suspendedGamesRemaining > 0) suspendedGamesRemaining--; }
    public int  getSuspendedGamesRemaining() { return suspendedGamesRemaining; }
    public void restoreSuspension(int val)   { this.suspendedGamesRemaining = val; }

    //stats
    public void recordGoal()        { goals++; }
    public void recordYellowCard()  { yellowCards++; }
    public void recordRedCard()     { redCards++; }
    public void recordAppearance()  { appearances++; }

    public int getGoals()       { return goals; }
    public int getYellowCards() { return yellowCards; }
    public int getRedCards()    { return redCards; }
    public int getAppearances() { return appearances; }

    /** Restores match statistics from a save file. */
    public void restoreStats(int goals, int yellowCards, int appearances) {
        this.goals       = goals;
        this.yellowCards = yellowCards;
        this.appearances = appearances;
    }

    /** Restores the season-start snapshot from a save file. */
    public void restoreInitialOverall(int val) {
        this.initialOverall = val;
    }


    //Snapshots the current overall at game start
    public void snapshotInitialOverall() {
        if (initialOverall == -1) {
            initialOverall = getOverallRating();
        }
    }

    public int getInitialOverall() {
        return initialOverall == -1 ? getOverallRating() : initialOverall;
    }

    public int getOverallChange() {
        return getOverallRating() - getInitialOverall();
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
