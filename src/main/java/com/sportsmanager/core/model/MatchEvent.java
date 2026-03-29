package com.sportsmanager.core.model;

import java.util.Objects;

/**
 * Represents a single event during a match.
 * Uses Builder pattern for clean construction.
 */
public class MatchEvent {

    public enum EventType {
        GOAL, INJURY, SUBSTITUTION, YELLOW_CARD, RED_CARD,
        PERIOD_END, MATCH_END, CUSTOM
    }

    private final EventType type;
    private final int minute;
    private final Team team;
    private final Player player;
    private final String description;

    private MatchEvent(Builder builder) {
        this.type = builder.type;
        this.minute = builder.minute;
        this.team = builder.team;
        this.player = builder.player;
        this.description = builder.description;
    }

    public static class Builder {
        private final EventType type;
        private final int minute;
        private Team team;
        private Player player;
        private String description = "";

        public Builder(EventType type, int minute) {
            this.type = Objects.requireNonNull(type, "type cannot be null");
            if (minute < 0) {
                throw new IllegalArgumentException("minute cannot be negative");
            }
            this.minute = minute;
        }

        public Builder team(Team t)          { this.team = t; return this; }
        public Builder player(Player p)      { this.player = p; return this; }
        public Builder description(String d) { this.description = d == null ? "" : d; return this; }
        public MatchEvent build()            { return new MatchEvent(this); }
    }

    public EventType getType()     { return type; }
    public int getMinute()         { return minute; }
    public Team getTeam()          { return team; }
    public Player getPlayer()      { return player; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "[" + minute + "'] " + type + " - " + description;
    }
}
