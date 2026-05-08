package com.sportsmanager.league;

import com.sportsmanager.core.model.Team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MatchDay {

    private final int weekNumber;
    private final List<Fixture> fixtures;

    public MatchDay(int weekNumber) {
        this.weekNumber = weekNumber;
        this.fixtures = new ArrayList<>();
    }

    public void addFixture(Fixture f) { fixtures.add(f); }

    public boolean isComplete() {
        return fixtures.stream().allMatch(Fixture::isPlayed);
    }

    public Fixture getFixtureFor(Team team) {
        return fixtures.stream()
                .filter(f -> f.getHome().equals(team) || f.getAway().equals(team))
                .findFirst().orElse(null);
    }

    public int getWeekNumber()          { return weekNumber; }
    public List<Fixture> getFixtures()  { return Collections.unmodifiableList(fixtures); }

    @Override
    public String toString() { return "Week " + weekNumber + " (" + fixtures.size() + " matches)"; }
}
