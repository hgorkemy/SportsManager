package com.sportsmanager.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Abstract team class. All sport-specific teams extend this.
 *
 * TODO (Irmak ): This class is defined. FootballTeam extends this.
 */
public abstract class Team {

    private final String id;
    private String name;
    private String logoPath;
    private String colorHex;
    private boolean isUserTeam;

    private final List<Player> squad;
    private final List<Player> lineup;
    private final List<Coach> coaches;
    private Tactic currentTactic;

    protected Team(String name, String logoPath) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.logoPath = logoPath;
        this.squad = new ArrayList<>();
        this.lineup = new ArrayList<>();
        this.coaches = new ArrayList<>();
    }

    // ── Abstract methods ──────────────────────────────────────────────────────

    /** Sport-specific lineup validation. Football: 11 players + at least 1 GK. */
    public abstract boolean validateLineup();

    /** Attack strength of current lineup (used by MatchEngine). */
    public abstract int calculateAttackRating();

    /** Defense strength of current lineup (used by MatchEngine). */
    public abstract int calculateDefenseRating();

    /** Maximum squad size for this sport. */
    public abstract int getMaxSquadSize();

    // ── Squad management ──────────────────────────────────────────────────────

    public void addPlayer(Player player) {
        if (squad.size() < getMaxSquadSize()) squad.add(player);
    }

    public void removePlayer(Player player) {
        squad.remove(player);
        lineup.remove(player);
    }

    public void setLineup(List<Player> selected) {
        lineup.clear();
        lineup.addAll(selected);
        if (!validateLineup()) {
            lineup.clear();
            throw new IllegalArgumentException("Invalid lineup for " + name);
        }
    }

    public void substitute(Player out, Player in) {
        int idx = lineup.indexOf(out);
        if (idx == -1) throw new IllegalArgumentException(out.getFullName() + " not in lineup");
        if (!squad.contains(in)) throw new IllegalArgumentException(in.getFullName() + " not in squad");
        lineup.set(idx, in);
    }

    public List<Player> getAvailableSubstitutes() {
        return squad.stream()
                .filter(p -> !lineup.contains(p) && !p.isInjured())
                .collect(Collectors.toList());
    }

    public List<Player> getHealthyPlayers() {
        return squad.stream().filter(p -> !p.isInjured()).collect(Collectors.toList());
    }

    public void conductWeeklyTraining() {
        List<Player> available = getHealthyPlayers();
        for (Coach coach : coaches) {
            for (Player player : available) {
                coach.conductTraining(player);
            }
        }
    }

    public void addCoach(Coach coach)    { coaches.add(coach); }
    public void removeCoach(Coach coach) { coaches.remove(coach); }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getId()            { return id; }
    public String getName()          { return name; }
    public String getLogoPath()      { return logoPath; }
    public String getColorHex()      { return colorHex; }
    public boolean isUserTeam()      { return isUserTeam; }
    public Tactic getCurrentTactic() { return currentTactic; }

    public List<Player> getSquad()   { return Collections.unmodifiableList(squad); }
    public List<Player> getLineup()  { return Collections.unmodifiableList(lineup); }
    public List<Coach> getCoaches()  { return Collections.unmodifiableList(coaches); }

    public void setName(String name)           { this.name = name; }
    public void setLogoPath(String path)       { this.logoPath = path; }
    public void setColorHex(String hex)        { this.colorHex = hex; }
    public void setUserTeam(boolean val)       { this.isUserTeam = val; }
    public void setCurrentTactic(Tactic t)     { this.currentTactic = t; }

    @Override
    public String toString() { return name; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Team other)) return false;
        return this.id.equals(other.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
